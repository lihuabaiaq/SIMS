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
import org.springframework.beans.factory.annotation.Value; // 1. 【新增】导入 @Value 注解
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // 2. 【新增】导入 ClassPathResource
import org.springframework.core.io.Resource; // 3. 【新增】导入 Resource 接口

import java.io.IOException;
import java.nio.charset.StandardCharsets; // 4. 【新增】导入字符集工具
import java.util.List;

@Configuration
public class AiConfiguration {

    // 5. 【新增】使用 @Value 注解从类路径注入 SystemPrompt.txt 文件
    @Value("classpath:SystemPrompt.txt")
    private Resource systemPromptResource;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore) throws IOException {
        // 6. 【修改】从注入的 Resource 对象中读取内容，而不是直接读取文件路径
        String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);

        return chatClientBuilder
                .defaultSystem(systemPrompt) // 使用读取到的内容
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
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);

        // 7. 【修改】使用 ClassPathResource 包装路径，以确保从类路径加载
        var textReader = new TextReader(new ClassPathResource("rag/rule.txt"));
        textReader.setCharset(StandardCharsets.UTF_8); // 建议明确指定文件编码

        List<Document> documentList = textReader.read();
        List<Document> transform = new TokenTextSplitter().transform(documentList);
        vectorStore.add(transform);
        return vectorStore;
    }
}