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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


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
    private GradeMapper gradeMapper;


    /**
     * 教师登录方法
     * 根据传入的教师DTO信息（包含教师ID和密码）验证是否匹配，若匹配则返回该教师对象，否则抛出登录异常
     */
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

    /**
     * 修改教师信息方法
     * 验证原密码是否正确，若正确则加密新密码并更新教师信息
     */
    @Override
    public void teachangeInfo(TeacherChangeDTO teacherChangeDTO) {
        Teacher teacher = query()
                .eq("teacher_id", teacherChangeDTO.getTeacherId())
                .one();
        if(!(teacherChangeDTO.getOriginPassword() ==null)){
            if( !teacher.getPassword().equals(MD5Util.encrypt(teacherChangeDTO.getOriginPassword()))){
                throw new LoginException("原密码错误,无法修改");
            }
            teacherChangeDTO.setChangePassword(MD5Util.encrypt(teacherChangeDTO.getChangePassword()));
        }
        teacherMapper.updateInfo(teacherChangeDTO);
    }

    /**
     * 保存课程方法
     * 设置课程学期、教师ID、状态等信息，并进行注册时间校验，最后发送消息到RabbitMQ
     */
    @Override
    public void saveCourse(Course course) {
        int currentMonth = LocalDate.now().getMonthValue();
        if (currentMonth > 8) {
            int smallYear = LocalDate.now().getYear();
            int bigYear = LocalDate.now().getYear() + 1;
            course.setSemester(smallYear + "-" + bigYear + "-1");
        } else {
            int smallYear = LocalDate.now().getYear() - 1;
            int bigYear = LocalDate.now().getYear();
            if (LocalDate.now().getMonthValue() < 3) {
                course.setSemester(smallYear + "-" + bigYear + "-1");
            } else {
                course.setSemester(smallYear + "-" + bigYear + "-2");
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
        if (insert != 1)
            throw new RegisterException("添加失败");
        Long courseId = course.getCourseId();
        log.info("课程添加成功{}", courseId);

        if(startTime<1000*60*60*24){
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE, MQConstants.REGISTER_START_KEY, courseId, message -> {
            message.getMessageProperties().setDelayLong(startTime);
            return message;
        });
        }
        if(endTime<1000*60*60*24) {
            rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE, MQConstants.REGISTER_END_KEY, courseId, message -> {
                message.getMessageProperties().setDelayLong(endTime);
                return message;
            });
        }
    }

    /**
     * 获取教师所教授的课程列表
     */
    @Override
    public List<Course> getCourse(Long teacherId) {
        return courseService.lambdaQuery().eq(Course::getTeacherId, teacherId).list();
    }

    /**
     * 更新成绩方法
     * 根据最终成绩计算绩点，并调用GradeMapper批量更新成绩信息
     */
    @Override
    public void updateScores(List<ScoreDTO> scoreList) {
        scoreList.forEach(scoreDTO -> {
            if (scoreDTO.getFinalGrade() < 50) {
                scoreDTO.setGradePoint(0.0);
            } else {
                scoreDTO.setGradePoint((scoreDTO.getFinalGrade() - 50) / 10);
            }
        });

        gradeMapper.updateScores(scoreList);
    }

    /**
     * 获取指定课程的所有学生成绩
     */
    @Override
    public List<Grade> getGrade(Long courseId) {
        List<Grade> gradeList = gradeMapper.getGrade(courseId);
        return gradeList;
    }

    /**
     * 结束课程方法
     * 检查所有学生的成绩是否完整，若完整则将课程状态设置为结课
     */
    @Override
    public void endCourse(Long courseId) {
        List<Grade> gradeList = gradeMapper.getGrade(courseId);
        for (Grade grade : gradeList) {
            if (grade.getRegularGrade() == null || grade.getFinalGrade() == null || grade.getExamGrade() == null || grade.getComments() == null) {
                throw new CourseException("有学生成绩为空，无法结课");
            }
        }
        courseMapper.endCourse(courseId);
    }

}
