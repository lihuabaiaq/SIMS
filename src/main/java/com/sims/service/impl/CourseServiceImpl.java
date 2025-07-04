package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.GradeMapper;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Grade;
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

    public List<Course> getHavingList(@Param("studentId") Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return this.query().eq("grade_requirement",grade)
                .eq("status",1)
                .notExists("select 1 from grade where grade.course_id = course.course_id and grade.student_id ="+studentId)
                .list();
    }

    @Override
    public List<Course> getHadList(@Param("studentId") Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return this.query().eq("grade_requirement",grade)
                .eq("status",1)
                .exists("select 1 from grade where grade.course_id = course.course_id and grade.student_id ="+studentId)
                .list();
    }

    @Override
    public List<Course> getBeHavingList(Long studentId) {
        String grade = studentService.getById(studentId).getGrade();
        return this.query().eq("grade_requirement",grade)
                .eq("status",0)
                .list();
    }

    @Override
    public void registerCourse(Long courseId) {
        Course course=this.getById(courseId);
        if(course== null)
            throw new RuntimeException("没有该课程");
        if(course.getStatus()!=1)
            throw new RuntimeException("该课程尚未开放选课");
        Long studentId = UserHolder.getId();
        Integer max = course.getMaxStudents();
        String luaScript =
                "if tonumber(redis.call('GET', KEYS[1])) >= tonumber(ARGV[1]) then\n" +
                "    return 1\n" +
                "end\n" +
                "if redis.call('SISMEMBER', KEYS[2], ARGV[2]) == 1 then\n" +
                "    return 2\n" +
                "end\n" +
                        "redis.call('INCR', KEYS[1])\n" +
                        "redis.call('SADD', KEYS[2], ARGV[2])\n" +
                        "return 0";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        String result = stringRedisTemplate.execute(script,
                Arrays.asList(
                        "course:fill:" + courseId.toString(),
                        "course:register:" + courseId.toString()
                ),
                max.toString(), // ARGV[1]
                studentId.toString() // ARGV[2]
        ).toString();
        Integer i = Integer.valueOf(result);

        switch (i) {
            case 1:
                throw new RuntimeException("选课人数已满");
            case 2:
                throw new RuntimeException("已选该课程,禁止重复选择");
            case 0:
                Grade studentCourse = new Grade();
                studentCourse.setCourseId(courseId);
                studentCourse.setStudentId(studentId);
                studentCourse.setSemester(course.getSemester());
                rabbitTemplate.convertAndSend("courseExchange","course.register",studentCourse);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Long studentId = UserHolder.getId();
        if(BooleanUtils.isFalse(stringRedisTemplate.opsForSet().isMember("course:register:" + courseId, studentId.toString())))
            throw new RuntimeException("尚未选择该课程");
        stringRedisTemplate.opsForSet().remove("course:register:"+courseId,studentId.toString());
        stringRedisTemplate.delete("course:registered:" + courseId+ ":" + studentId);
        gradeMapper.delete(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId,studentId)
                .eq(Grade::getCourseId,courseId));
        this.update().setSql("current_students = current_students - 1")
                .eq("course_id",courseId).update();
    }



    @Transactional
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "course.regiest.queue"),
    exchange = @Exchange(value = "courseExchange"),
    key = "course.register"))
    public void RegisterCourse(Grade studentCourse){
        String lockKey = "course:registered:" + studentCourse.getCourseId() + ":" + studentCourse.getStudentId();
        Boolean isExist = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 1, TimeUnit.DAYS);
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
            stringRedisTemplate.opsForValue().set("course:fill:" + course.getCourseId(), currents.toString());
        }
    }
}
