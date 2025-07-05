package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.pojo.dto.StudentChangeDTO;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Student;
import com.sims.pojo.vo.ScoreAVGVO;
import com.sims.pojo.vo.StudentGradeVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface StudentService extends IService<Student> {
    Student studentLogin(StudentDTO studentDTO);


    void stuchangeInfo(StudentChangeDTO studentChangeDTO);

    List<StudentGradeVO> getGrade(Long studentId);

    List<StudentGradeVO> select(StudentGradeDTO studentGradeDTO);

    ScoreAVGVO studyanalyze(Long studentId,String startsemester,String endsemester);

    List<String> getAvailableSemester(Long studentId);

    List<AVGScore> getStudentScore(Long studentId);

}
