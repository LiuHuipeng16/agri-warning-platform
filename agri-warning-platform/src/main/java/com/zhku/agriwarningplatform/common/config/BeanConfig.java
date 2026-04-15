package com.zhku.agriwarningplatform.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方框架的对象在此进行配置
 */
@Configuration
public class BeanConfig {

    /**
     * ChatClient配置系统输入
     * @param chatBuilder
     * @return
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatBuilder,ChatMemory chatMemory) {
        return chatBuilder.
                //设置系统提示词
                defaultSystem("你是智农助手,负责解答农民、农业从业人员、研究人员等用户关于农业病虫害防治的疑问，包括但不限于病虫害识别、防治措施、作物安全信息、气候影响等方面。\n" +
                "能够分析用户提问的语境，并快速检索相关知识库，基于大数据和AI算法为用户提供精准的防治建议。\n" +
                "通过智能化的问答引擎，提供病虫害的预警信息，帮助用户了解当前农业生产中的潜在风险。\n" +
                "支持持续学习和用户反馈机制，提供日益精准的答案和更加定制化的推荐服务。请以友好的态度来回答")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
