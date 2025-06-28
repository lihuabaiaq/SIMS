package com.sims.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.StudentMapper;
import com.sims.pojo.dto.StudentChangeDTO;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.Student;
import com.sims.pojo.vo.ScoreAVGVO;
import com.sims.pojo.vo.StudentGradeVO;
import com.sims.service.StudentService;
import com.sims.util.MD5Util;
import io.lettuce.core.ScriptOutputType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {


    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }
    @Resource
    private CourseMapper courseMapper;

    @Override
    public Student studentLogin(StudentDTO studentDTO) {
        String password = studentDTO.getPassword();
        Long studentId = studentDTO.getStudentId();
        Student student = this.query().eq("student_id", studentId).one();
        if (student != null && student.getPassword().equals(password)) {
            return student;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public void changeInfo(StudentChangeDTO studentChangeDTO) {
        System.out.println(studentChangeDTO);
        Student student=query()
                .eq("student_id",studentChangeDTO.getStudentId())
                .one();
        if(student==null || !student.getPassword().equals(MD5Util.encrypt(studentChangeDTO.getOriginPassword())))
            throw new RuntimeException("原密码错误,无法修改");
        studentChangeDTO.setChangePassword(MD5Util.encrypt(studentChangeDTO.getChangePassword()));
        studentMapper.updateInfo(studentChangeDTO);
    }

    @Override
    public List<StudentGradeVO> getGrade(Long studentId) {
        return courseMapper.getGrade(studentId);
    }

    @Override
    public List<StudentGradeVO> select(StudentGradeDTO studentGradeDTO) {
        System.out.println("1111");
        return courseMapper.select(studentGradeDTO);
    }

    @Override
    public ScoreAVGVO studyanalyze(Long studentId) {
        ScoreAVGVO scoreAVGVO = new ScoreAVGVO();
        scoreAVGVO.setStudentScoreList(studentMapper.getAVGScore(studentId));
        scoreAVGVO.setAverageScoreList(studentMapper.getAVGScore(null));
        return scoreAVGVO;
    }


}
