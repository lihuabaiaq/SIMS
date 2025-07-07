package com.sims.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.constants.RedisConstants;
import com.sims.handle.Exception.LoginException;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.StudentMapper;
import com.sims.pojo.dto.StudentChangeDTO;
import com.sims.pojo.dto.StudentDTO;
import com.sims.pojo.dto.StudentGradeDTO;
import com.sims.pojo.entity.AVGScore;
import com.sims.pojo.entity.Student;
import com.sims.pojo.vo.ScoreAVGVO;
import com.sims.pojo.vo.StudentGradeVO;
import com.sims.service.StudentService;
import com.sims.util.MD5Util;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {


    private final StudentMapper studentMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }
    @Resource
    private CourseMapper courseMapper;

    /**
     * 学生登录方法
     * 根据传入的StudentDTO中的studentId查询学生信息，并验证密码是否正确
     * @param studentDTO 包含学生ID和密码的传输对象
     * @return 如果验证通过，返回学生对象；否则抛出登录异常
     */
    @Override
    public Student studentLogin(StudentDTO studentDTO) {
        String password = studentDTO.getPassword();
        Long studentId = studentDTO.getStudentId();
        Student student = this.query().eq("student_id", studentId).one();
        if (student != null && student.getPassword().equals(password)) {
            return student;
        }
        throw new LoginException("用户名或密码错误");
    }

    /**
     * 修改学生信息方法
     * 查询学生信息，验证原密码是否正确，并更新学生信息
     * @param studentChangeDTO 包含修改信息的传输对象
     */
    @Override
    public void stuchangeInfo(StudentChangeDTO studentChangeDTO) {
        System.out.println(studentChangeDTO);
        Student student=query()
                .eq("student_id",studentChangeDTO.getStudentId())
                .one();
        if(!(studentChangeDTO.getOriginPassword() ==null)){
            if( !student.getPassword().equals(MD5Util.encrypt(studentChangeDTO.getOriginPassword()))){
                throw new LoginException("原密码错误,无法修改");
            }
            studentChangeDTO.setChangePassword(MD5Util.encrypt(studentChangeDTO.getChangePassword()));
        }
        studentMapper.updateInfo(studentChangeDTO);
    }

    /**
     * 获取学生成绩方法
     * 调用CourseMapper获取指定学生ID的成绩信息
     * @param studentId 学生的唯一标识符
     * @return 返回学生具体成绩的视图对象列表
     */
    @Override
    public List<StudentGradeVO> getGrade(Long studentId) {
        return courseMapper.getGrade(studentId);
    }

    /**
     * 按条件查询学生成绩方法
     * 使用传入的StudentGradeDTO参数进行成绩查询
     * @param studentGradeDTO 包含查询条件的成绩传输对象
     * @return 返回符合查询条件的学生具体成绩的视图对象列表
     */
    @Override
    public List<StudentGradeVO> select(StudentGradeDTO studentGradeDTO) {
        return courseMapper.select(studentGradeDTO);
    }

    /**
     * 学习分析方法
     * 分析学生在指定学期范围内的学习情况，并与年级平均水平进行比较
     * @param studentId 学生的唯一标识符
     * @param startsemester 分析起始学期
     * @param endsemester 分析结束学期
     * @return 返回包含学生和年级平均成绩的学习分析结果视图对象
     */
    @Override
    public ScoreAVGVO studyanalyze(Long studentId, String startsemester, String endsemester) {
        String grade = studentId.toString().substring(0, 4);
        //ScoreAVGVO scoreAVGVO = new ScoreAVGVO();
        List<String> semesters = new ArrayList<>();
        String[] start = startsemester.split("-");
        //String[] end = endsemester.split("-");
        semesters.add(startsemester);
        while (!startsemester.equals(endsemester)) {
            if (start[2].equals("1")) {
                start[2] = "2";
                startsemester = String.join("-", start);
                semesters.add(startsemester);
            } else if (start[2].equals("2")) {
                start[0] = String.valueOf(Integer.parseInt(start[0]) + 1);
                start[1] = String.valueOf(Integer.parseInt(start[0]) + 1);
                start[2] = "1";
                startsemester = String.join("-", start);
                semesters.add(startsemester);
            }
        }
        List<AVGScore> studentAllScoreList;
        if (semesters.isEmpty())
            studentAllScoreList = getStudentScore(studentId);
        else
            studentAllScoreList = courseMapper.getStudentScore(studentId, semesters);
        List<AVGScore> avgAllScoreList = courseMapper.getGradeScore(semesters, grade);
        ScoreAVGVO scoreAVGVO1 = new ScoreAVGVO();
        scoreAVGVO1.setStudentScoreList(studentAllScoreList);
        scoreAVGVO1.setAverageScoreList(avgAllScoreList);
        return scoreAVGVO1;
    }

    /**
     * 获取可用学期列表方法
     * 根据学生的入学日期计算出所有可用的学期
     * @param studentId 学生的唯一标识符
     * @return 返回可用学期的字符串列表
     */
    @Override
    public List<String> getAvailableSemester(Long studentId) {
        List<String> semesters = new ArrayList<>();
        String admissionDate=studentMapper.getInfo(studentId);
        int startYear = Integer.parseInt(admissionDate.substring(0, 4));
        //int endYear = startYear + 1;
        int semesterNumber =1;
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = LocalDate.of(startYear, 9, 1); // 第一学期开始日期
        while (!startDate.isAfter(currentDate)) {
            String semester = String.format("%d-%d-%d", startDate.getYear(), startDate.getYear() + 1, semesterNumber);
            semesters.add(semester);
            // 更新到下一学期
            if (semesterNumber == 1) {
                startDate = LocalDate.of(startDate.getYear(), 3, 1); // 第二学期开始日期
                semesterNumber = 2;
            } else {
                startDate = LocalDate.of(startDate.getYear() + 1, 9, 1); // 下一年第一学期开始日期
                semesterNumber = 1;
            }
        }
        return semesters;
    }

    /**
     * 获取学生平均成绩方法
     * 先尝试从Redis缓存中获取学生平均成绩，若不存在则从数据库查询并写入缓存
     * @param studentId 学生的唯一标识符
     * @return 返回包含学生平均成绩的实体对象列表
     */
    @Override
    public List<AVGScore> getStudentScore(Long studentId) {
        String key=RedisConstants.AVG_SCORES_KEY+studentId;
        List<AVGScore> avgscores = JSONObject.parseArray(stringRedisTemplate.opsForValue().get(key),AVGScore.class);
        if (avgscores!=null){
            return avgscores;
        }
        avgscores = courseMapper.getStudentScore(studentId,null);
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(avgscores),
                RedisConstants.AVG_SCORES_TTL, TimeUnit.DAYS);
        return avgscores;
    }

}
