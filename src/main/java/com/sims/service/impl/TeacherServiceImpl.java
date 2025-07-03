package com.sims.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.TeacherMapper;
import com.sims.pojo.dto.TeacherChangeDTO;
import com.sims.pojo.dto.TeacherDTO;
import com.sims.pojo.entity.Course;
import com.sims.pojo.entity.Teacher;
import com.sims.service.StudentService;
import com.sims.service.TeacherService;
import com.sims.util.MD5Util;
import com.sims.util.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Resource
    TeacherMapper teacherMapper;
    @Resource
    CourseMapper courseMapper;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Teacher teacherLogin(TeacherDTO teacherDTO) {
        String password = teacherDTO.getPassword();
        Long teacherId = teacherDTO.getTeacherId();
        Teacher teacher = this.query().eq("teacher_id", teacherId).one();
        if (teacher != null && teacher.getPassword().equals(password)) {
            return teacher;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public void changeInfo(TeacherChangeDTO teacherChangeDTO) {
        Teacher teacher=query()
                .eq("teacher_id",teacherChangeDTO.getTeacherId())
                .one();
        if(teacher==null || !teacher.getPassword().equals(MD5Util.encrypt(teacherChangeDTO.getOriginalPassword())))
            throw new RuntimeException("原密码错误,无法修改");
        teacherMapper.updateInfo(teacherChangeDTO);
    }

    @Override
    @Transactional
    public void saveCourse(Course course) {
        Long teacherId = UserHolder.getId();
        course.setTeacherId(teacherId);
        course.setStatus(0);
        int insert = courseMapper.insert(course);
        if(insert!=1)
            throw new RuntimeException("添加失败");
        Long courseId= course.getCourseId();
        LocalDateTime registerStart = course.getRegisterStart();
        LocalDateTime registerEnd = course.getRegisterEnd();
        rabbitTemplate.convertAndSend("delayExchange","course.register.start",courseId, message -> {
            Long time=ChronoUnit.MILLIS.between(LocalDateTime.now(),registerStart);
            if(time<0)
                throw new RuntimeException("开始时间不能早于当前时间");
            message.getMessageProperties().setDelayLong(time);
            return message;}
        );
        stringRedisTemplate.opsForValue().set("course:fill:"+courseId, String.valueOf(0));
        rabbitTemplate.convertAndSend("delayExchange","course.register.end",courseId,message -> {
            Long time=ChronoUnit.MILLIS.between(LocalDateTime.now(),registerEnd);
            if(time<0)
                throw new RuntimeException("结束时间不能早于当前时间");
            message.getMessageProperties().setDelayLong(time);
        return message;}
        );
    }

    @Override
    public List<Course> getCourse(Long teacherId) {
        return courseMapper.getCourse(teacherId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "course.register.start.queue",durable = "true"),
            exchange = @Exchange(value = "delayExchange",delayed = "true"),
            key = {"course.register.start"}
    ))
    public void courseRegisterStart(Long courseId){
        Course course = courseMapper.selectById(courseId);
        if(course==null||course.getStatus()!=0)
            return;
        courseMapper.updateStatus(courseId,1);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "course.register.end.queue",durable = "true"),
            exchange = @Exchange(value = "delayExchange",delayed = "true"),
            key = {"course.register.end"}
    ))
    public void courseRegisterEnd(Long courseId){
        stringRedisTemplate.delete("course:fill:"+courseId);
        stringRedisTemplate.delete("course:register:"+courseId);
        Course course = courseMapper.selectById(courseId);
        if(course==null||course.getStatus()!=1)
            return;
        courseMapper.updateStatus(courseId,2);
    }
}
