package com.sims;

import com.sims.mapper.CourseMapper;
import com.sims.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class SimsApplicationTests {

    @Resource
    TeacherMapper teacherMapper;
    @Resource
    CourseMapper courseMapper;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

}
