package com.zhku.agriwarningplatform.module.ai.config;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-19
 * Time: 10:25
 */
import com.zhku.agriwarningplatform.module.ai.service.AIService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIKnowledgeVectorStoreInitializer {

    private final AIService aiService;

    @PostConstruct
    public void init() {
        try {
            log.info("开始初始化AI知识库向量数据");
            aiService.initKnowledgeBaseToVectorStore();
            log.info("AI知识库向量数据初始化完成");
        } catch (Exception e) {
            log.error("AI知识库向量数据初始化异常", e);
        }
    }
}