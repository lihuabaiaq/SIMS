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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Resource
    TeacherMapper teacherMapper;
    @Resource
    CourseMapper courseMapper;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    StudentService studentService;

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
    public void teachangeInfo(TeacherChangeDTO teacherChangeDTO) {
        Teacher teacher=query()
                .eq("teacher_id",teacherChangeDTO.getTeacherId())
                .one();
        if(teacher==null || !teacher.getPassword().equals(MD5Util.encrypt(teacherChangeDTO.getOriginPassword())))
            throw new RuntimeException("原密码错误,无法修改");
        teacherChangeDTO.setChangePassword(MD5Util.encrypt(teacherChangeDTO.getChangePassword()));
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
        if(courseMapper.selectById(courseId).getStatus()!=0)
            return;
        courseMapper.updateStatus(courseId,1);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "course.register.end.queue",durable = "true"),
            exchange = @Exchange(value = "delayExchange",delayed = "true"),
            key = {"course.register.end"}
    ))
    public void courseRegisterEnd(Long courseId){
        if(courseMapper.selectById(courseId).getStatus()!=1)
            return;
        courseMapper.updateStatus(courseId,2);
    }
}
