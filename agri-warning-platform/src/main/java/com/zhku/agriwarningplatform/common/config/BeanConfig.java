package com.zhku.agriwarningplatform.common.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方框架的对象在此进行配置
 */
@Configuration
public class BeanConfig {

    /**
     * DeepSeek ChatClient：用于普通文本问答、RAG问答、SSE流式回答
     */
    @Bean("deepSeekChatClient")
    public ChatClient deepSeekChatClient(
            @Qualifier("openAiChatModel") ChatModel chatModel
    ) {
        return ChatClient.builder(chatModel)
                .defaultSystem(
                        "你是智农助手,负责解答农民、农业从业人员、研究人员等用户关于农业病虫害防治的疑问，包括但不限于病虫害识别、防治措施、作物安全信息、气候影响等方面。\n" +
                                "能够分析用户提问的语境，并快速检索相关知识库，基于大数据和AI算法为用户提供精准的防治建议。\n" +
                                "通过智能化的问答引擎，提供病虫害的预警信息，帮助用户了解当前农业生产中的潜在风险。\n" +
                                "支持持续学习和用户反馈机制，提供日益精准的答案和更加定制化的推荐服务。请以友好的态度来回答"
                )
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * DashScope ChatClient：用于图片识别、多模态分析
     */
    @Bean("dashScopeChatClient")
    public ChatClient dashScopeChatClient(
            @Qualifier("dashscopeChatModel") ChatModel chatModel
    ) {
        return ChatClient.builder(chatModel)
                .defaultSystem(
                        "你是农业病虫害图片识别助手，只负责分析图片中可见的农业症状信息。\n" +
                                "你的职责是提取图片中的作物、叶片、病斑、虫体、颜色、形态、受害部位等客观信息。\n" +
                                "如果能初步判断病虫害类型，可以使用“疑似”表达。\n" +
                                "如果无法确定，必须说明无法仅凭图片确定。\n" +
                                "不要输出完整防治方案。\n" +
                                "不要推荐农药。\n" +
                                "不要写综合管理建议。\n" +
                                "不要把图片识别结果写成最终用户回答。"
                )
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
    /**
     * 向量库配置：默认使用DashScope EmbeddingModel
     */
    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }
}