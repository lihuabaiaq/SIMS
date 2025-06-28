package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Course;
import com.sims.pojo.vo.StudentGradeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    List<StudentGradeVO> getGrade(Long studentId);

    List<StudentGradeVO> select(StudentGradeDTO studentGradeDTO);

    List<AVGScore> getAVGScore(Long studentId);
}
