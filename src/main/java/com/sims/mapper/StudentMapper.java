package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sims.pojo.dto.StudentChangeDTO;
import com.sims.pojo.entity.Student;
import com.sims.pojo.vo.ScoreAVGVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student>{
    void updateInfo(StudentChangeDTO studentChangeDTO);

    List<Double> getAVGScore(Long studentId,String grade,String year);

    @Select("select * from sism.student where student_id=#{studentId}")
    String getInfo(Long studentId);
}
