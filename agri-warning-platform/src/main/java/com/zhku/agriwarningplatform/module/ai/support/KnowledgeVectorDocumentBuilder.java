package com.zhku.agriwarningplatform.module.ai.support;

import com.zhku.agriwarningplatform.module.ai.mapper.AIMapper;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorDocumentBuilder {

    private final AIMapper aiMapper;

    public Document buildKnowledgeDocument(LightweightKnowledgeBaseEnhancedQaDO qaDO) {
        Long cropId = qaDO.getCropId();
        Long pestId = qaDO.getPestId();

        String cropName = getNullableString(aiMapper.getCropNameById(cropId));
        String pestName = getNullableString(aiMapper.getPestNameById(pestId));
        String symptoms = getNullableString(aiMapper.getPestSymptomsById(pestId));

        String content = """
                作物：%s
                病虫害：%s
                问题：%s
                症状：%s
                答案：%s
                """.formatted(
                cropName,
                pestName,
                getNullableString(qaDO.getQuestion()),
                symptoms,
                getNullableString(qaDO.getAnswer())
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cropId", String.valueOf(cropId == null ? 0L : cropId));
        metadata.put("pestId", String.valueOf(pestId == null ? 0L : pestId));

        return Document.builder()
                .id(String.valueOf(qaDO.getId()))
                .text(content)
                .metadata(metadata)
                .build();
    }

    private String getNullableString(String value) {
        return value == null ? "" : value;
    }
}