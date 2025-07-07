package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.constants.MQConstants;
import com.sims.constants.RedisConstants;
import com.sims.handle.Exception.CourseException;
import com.sims.handle.Exception.LoginException;
import com.sims.handle.Exception.RegisterException;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.GradeMapper;
import com.sims.mapper.TeacherMapper;
import com.sims.pojo.dto.ScoreDTO;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Grade;
import com.sims.pojo.entity.Teacher;
import com.sims.service.CourseService;
import com.sims.service.TeacherService;
import com.sims.util.MD5Util;
import com.sims.util.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Resource
    TeacherMapper teacherMapper;
    @Resource
    CourseMapper courseMapper;
    @Resource
    CourseService courseService;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private GradeMapper gradeMapper;

    @Override
    public Teacher teacherLogin(TeacherDTO teacherDTO) {
        String password = teacherDTO.getPassword();
        Long teacherId = teacherDTO.getTeacherId();
        Teacher teacher = this.query().eq("teacher_id", teacherId).one();
        if (teacher != null && teacher.getPassword().equals(password)) {
            return teacher;
        }
        throw new LoginException("用户名或密码错误");
    }

    @Override
    public void teachangeInfo(TeacherChangeDTO teacherChangeDTO) {
        Teacher teacher=query()
                .eq("teacher_id",teacherChangeDTO.getTeacherId())
                .one();
        if(!(teacherChangeDTO.getOriginPassword() ==null)){
            if( !teacher.getPassword().equals(MD5Util.encrypt(teacherChangeDTO.getOriginPassword()))){
                throw new LoginException("原密码错误,无法修改");
            }
            teacherChangeDTO.setChangePassword(MD5Util.encrypt(teacherChangeDTO.getChangePassword()));
        }
        teacherMapper.updateInfo(teacherChangeDTO);
    }

    @Override
    @Transactional
    public void saveCourse(Course course) {
        int currentMonth = LocalDate.now().getMonthValue();
        if(currentMonth>8){
            int smallYear = LocalDate.now().getYear();
            int bigYear=LocalDate.now().getYear()+1;
            course.setSemester(smallYear+"-"+bigYear+"-1");
        }
        else{
            int smallYear = LocalDate.now().getYear()-1;
            int bigYear=LocalDate.now().getYear();
            if(LocalDate.now().getMonthValue()<3){
                course.setSemester(smallYear+"-"+bigYear+"-1");
            }
            else{
                course.setSemester(smallYear+"-"+bigYear+"-2");
            }
        }
        Long teacherId = UserHolder.getId();
        course.setTeacherId(teacherId);
        course.setStatus(0);
        LocalDateTime registerStart = course.getRegisterStart();
        LocalDateTime registerEnd = course.getRegisterEnd();
        long startTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), registerStart);
        if (startTime < 0)
            throw new RegisterException("开始时间不能早于当前时间");
        long endTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), registerEnd);
        if (endTime < 0)
            throw new RegisterException("结束时间不能早于当前时间");
        int insert = courseMapper.insert(course);
        if(insert!=1)
            throw new RegisterException("添加失败");
        Long courseId= course.getCourseId();
        log.info("课程添加成功{}", courseId);
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE,MQConstants.REGISTER_START_KEY,courseId, message -> {
            message.getMessageProperties().setDelayLong(startTime);
            return message;}
        );
        stringRedisTemplate.opsForValue().set(RedisConstants.COURSE_FILL_KEY +courseId, String.valueOf(0));
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE,MQConstants.REGISTER_END_KEY,courseId,message -> {
            message.getMessageProperties().setDelayLong(endTime);
        return message;}
        );
    }

    @Override
    public List<Course> getCourse(Long teacherId) {
        return courseService.lambdaQuery().eq(Course::getTeacherId,teacherId).list();
    }

    @Override
    public void updateScores(List<ScoreDTO> scoreList) {
        scoreList.forEach(scoreDTO ->{
            if(scoreDTO.getFinalGrade() < 50){
                scoreDTO.setGradePoint(0.0);
            } else {
                scoreDTO.setGradePoint((scoreDTO.getFinalGrade() - 50) / 10);}
            });

        gradeMapper.updateScores(scoreList);
    }

    @Override
    public List<Grade> getGrade(Long courseId) {
        List<Grade> gradeList = gradeMapper.getGrade(courseId);
        return gradeList;
    }

    @Override
    public void endCourse(Long courseId) {
        List<Grade> gradeList = gradeMapper.getGrade(courseId);
        for(Grade grade:gradeList){
            if(grade.getRegularGrade()==null || grade.getFinalGrade()==null || grade.getExamGrade()==null || grade.getComments()==null){
                throw new CourseException("有学生成绩为空，无法结课");
            }
        }
        courseMapper.endCourse(courseId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.REGISTER_START_QUEUE,durable = "true"),
            exchange = @Exchange(value =MQConstants.DELAY_EXCHANGE,delayed = "true"),
            key = {MQConstants.REGISTER_START_KEY}
    ))
    public void courseRegisterStart(Long courseId){
        Course course = courseMapper.selectById(courseId);
        if(course==null||course.getStatus()!=0)
            return;
        log.info("课程开始选课{}", courseId);
        courseService.update().set("status",1).eq("course_id",courseId).update();
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value =MQConstants.REGISTER_END_QUEUE,durable = "true"),
            exchange = @Exchange(value = MQConstants.DELAY_EXCHANGE,delayed = "true"),
            key = {MQConstants.REGISTER_END_KEY}
    ))
    public void courseRegisterEnd(Long courseId){
        stringRedisTemplate.delete(RedisConstants.COURSE_FILL_KEY+courseId);
        stringRedisTemplate.delete(RedisConstants.COURSE_REGISTER_KEY+courseId);
        Course course = courseMapper.selectById(courseId);
        if(course==null||course.getStatus()!=1)
            return;
        courseService.update().set("status",1).eq("course_id",courseId).update();
        log.info("课程结束选课{}", courseId);
    }
}
