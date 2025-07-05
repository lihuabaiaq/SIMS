package com.sims.controller;


import com.sims.pojo.dto.CompetitionDTO;
import com.sims.pojo.vo.CompetitionVO;
import com.sims.pojo.vo.CourseVO;
import com.sims.pojo.vo.JobVO;
import com.sims.service.CompetitionService;
import com.sims.service.CourseService;
import com.sims.service.JobService;
import com.sims.util.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;


@RestController
@RequestMapping("/ai")
public class OpenAiController {

    @Resource
    ChatClient chatClient;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private JobService jobService;
    @Autowired
    private CourseService courseService;


    public record StudentInput(Long studentId) {}
    public record CourseInput(Long courseId,Long studentId) {}

    @GetMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message){
        Flux<String> response = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY,20))
                .stream()
                .content();
        return response.concatWith(Flux.just("[DONE]"));
    }

    @Bean
    @Description("适合参加什么竞赛,为什么适合参加该竞赛")
    public Function<StudentInput, List<CompetitionVO>> competitionReason(){
        CompetitionDTO competitionDTO = new CompetitionDTO();
        return input ->{
            competitionDTO.setStudentId(input.studentId);
            return competitionService.competitionRecommend(competitionDTO);
        };
    }

    @Bean
    @Description("适合参加什么职位,为什么适合参加该岗位")
    public Function<StudentInput, List<JobVO>> jobReason(){
        return input -> jobService.JobRecommend(input.studentId);
    }

    @Bean
    @Description("选课")
    public Function<CourseInput,String> registerCourse(){
        return input->{
            UserHolder.saveId(input.studentId);
            courseService.registerCourse(input.courseId);
            return "选课成功";
        };
    }

    @Bean
    @Description("退课")
    public Function<CourseInput,String> deleteCourse(){
        return input->{
            UserHolder.saveId(input.studentId);
            courseService.deleteCourse(input.courseId);
            return "退课成功";
        };
    }

    @Bean
    @Description("查看可以选的课")
    public Function<StudentInput,List<CourseVO>> getCourseListing() {
        return input -> courseService.getHavingList(input.studentId);
    }

    @Bean
    @Description("查看已经选了的课")
    public Function<StudentInput,List<CourseVO>> getCourseListed() {
        return input -> courseService.getHadList(input.studentId);
    }

    @Bean
    @Description("查看还未开始的选课")
    public Function<StudentInput,List<CourseVO>> getCoursebeList() {
        return input -> courseService.getBeHavingList(input.studentId);
    }

}
