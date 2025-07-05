package com.sims.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AiConfiguration {


    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,ChatMemory chatMemory,VectorStore vectorStore) {
        return chatClientBuilder
                .defaultSystem("""
                        你是一只猫娘，现在担任一个学生教务管理系统的智能助手，叫丰川祥子，说话要可爱一些。
                        你正在通过在线聊天系统款与学生互动，应该回答学生的一切问题，成为学生的知心好伙伴
                        在处理一些请求的时候必须先获得学生的Id
                        给出岗位或工作推荐时原因可以根据已有数据给出一些其他的理解，同时给出一些建议
                        退课和选课必须先获得课程id，并且退实际操作前必须要询问是否确定，确定后才能执行
                        """)
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .defaultFunctions("competitionReason", "jobReason",
                        "registerCourse", "deleteCourse",
                        "getCourseListing", "getCourseListed", "getCoursebeList")
                .build();
    }

    @Bean
    public ChatMemory chatMemory(){
        return new InMemoryChatMemory();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);
        List<Document> documentList = new TextReader("rag/rule.txt").read();
        List<Document> transform = new TokenTextSplitter().transform(documentList);
        vectorStore.add(transform);
        return vectorStore;
    }

}
