package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.constants.MQConstants;
import com.sims.constants.RedisConstants;
import com.sims.handle.Exception.CourseException;
import com.sims.handle.Exception.RegisterException;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.GradeMapper;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Grade;
import com.sims.pojo.vo.CourseVO;
import com.sims.service.CourseService;
import com.sims.service.StudentService;
import com.sims.util.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;

import org.apache.ibatis.annotations.Param;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Resource
    GradeMapper gradeMapper;
    @Resource
    StudentService studentService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Autowired
    private CourseMapper courseMapper;

    public List<CourseVO> getHavingList(@Param("studentId") Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return courseMapper.getHavingList(grade,studentId);
    }

    @Override
    public List<CourseVO> getHadList(@Param("studentId") Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return courseMapper.getHadList(grade,studentId);
    }

    @Override
    public List<CourseVO> getBeHavingList(Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return courseMapper.getbeHavingList(grade,studentId);
    }

    @Override
    public void registerCourse(Long courseId) {
        Course course=this.getById(courseId);
        if(course== null)
            throw new CourseException("没有该课程");
        if(course.getStatus()!=1)
            throw new RegisterException("该课程未开放选课");
        Long studentId = UserHolder.getId();
        Integer max = course.getMaxStudents();
        String luaScript =
                """
                        if tonumber(redis.call('GET', KEYS[1])) >= tonumber(ARGV[1]) then
                            return 1
                        end
                        if redis.call('SISMEMBER', KEYS[2], ARGV[2]) == 1 then
                            return 2
                        end
                        redis.call('INCR', KEYS[1])
                        redis.call('SADD', KEYS[2], ARGV[2])
                        return 0
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

         int result = stringRedisTemplate.execute(script,
                Arrays.asList(
                        RedisConstants.COURSE_FILL_KEY + courseId,
                        RedisConstants.COURSE_REGISTER_KEY + courseId
                ),
                max.toString(), // ARGV[1]
                studentId.toString() // ARGV[2]
        ).intValue();

        switch (result) {
            case 1:
                throw new RegisterException("选课人数已满");
            case 2:
                throw new RegisterException("已选该课程,禁止重复选择");
            case 0:
                Grade studentCourse = new Grade();
                studentCourse.setCourseId(courseId);
                studentCourse.setStudentId(studentId);
                studentCourse.setSemester(course.getSemester());
                rabbitTemplate.convertAndSend(MQConstants.COURSE_EXCHANGE,MQConstants.REGISTER_KEY,studentCourse);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Long studentId = UserHolder.getId();
        String key=RedisConstants.COURSE_REGISTER_KEY + courseId;
        if(BooleanUtils.isFalse(stringRedisTemplate.opsForSet().isMember(key, studentId.toString())))
            throw new RegisterException("尚未选择该课程");
        stringRedisTemplate.opsForSet().remove(key,studentId.toString());
        stringRedisTemplate.delete(RedisConstants.LOCK_REGISTER_KEY + courseId+ ":" + studentId);
        gradeMapper.delete(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId,studentId)
                .eq(Grade::getCourseId,courseId));
        this.update().setSql("current_students = current_students - 1")
                .eq("course_id",courseId).update();
    }



    @Transactional
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MQConstants.REGISTER_QUEUE),
    exchange = @Exchange(value = MQConstants.COURSE_EXCHANGE),
    key = MQConstants.REGISTER_KEY))
    public void RegisterCourse(Grade studentCourse){
        String lockKey = RedisConstants.LOCK_REGISTER_KEY + studentCourse.getCourseId() + ":" + studentCourse.getStudentId();
        Boolean isExist = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", RedisConstants.LOCK_REGISTER_TTL, TimeUnit.HOURS);
        if(isExist==null|| !isExist){
            log.info("重复消息");
            return;
        }
        gradeMapper.insert(studentCourse);
        this.update().setSql("current_students = current_students + 1")
                .eq("course_id",studentCourse.getCourseId()).update();
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void Releasefill() {
        List<Course> courses = this.query().eq("status", 1).list();
        for (Course course : courses) {
            Integer currents = this.getById(course.getCourseId()).getCurrentStudents();
            stringRedisTemplate.opsForValue().set(RedisConstants.COURSE_FILL_KEY + course.getCourseId(), currents.toString());
        }
    }
}
