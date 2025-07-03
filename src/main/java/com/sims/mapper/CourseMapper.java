package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Course;
import com.sims.pojo.vo.StudentGradeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    List<StudentGradeVO> getGrade(Long studentId);

    List<StudentGradeVO> select(StudentGradeDTO studentGradeDTO);

    List<AVGScore> getStudentScore(Long studentId, List<String> semesters);

    List<AVGScore> getGradeScore(List<String> semesters, String grade);

    @Update("update course set staus=#{status}")
    void updateStatus(Long courseId, Integer status);

    @Select("select * from course where teacher_id=#{teacherId}")
    List<Course> getCourse(Long teacherId);

}
