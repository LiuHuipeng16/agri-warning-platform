package com.zhku.agriwarningplatform.module.knowledgeqa.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhku.agriwarningplatform.common.errorcode.KnowledgeqaCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.ai.mapper.AIMapper;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaCreateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaUpdateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaReqParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaRespVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.mapper.KnowledgeqaMapper;
import com.zhku.agriwarningplatform.module.knowledgeqa.service.KnowledgeqaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeqaServiceImpl implements KnowledgeqaService {

    private final KnowledgeqaMapper knowledgeqaMapper;
    private final AIMapper aiMapper;
    private final SimpleVectorStore simpleVectorStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<KnowledgeqaRespVO> page(@Validated KnowledgeqaReqParam reqVO) {
        if (reqVO.getPageNum() == null || reqVO.getPageSize() == null) {
            throw new ServiceException(KnowledgeqaCode.PAGE_PARAM_ERROR);
        }

        Page<KnowledgeqaRespVO> page = PageHelper.startPage(reqVO.getPageNum(), reqVO.getPageSize());
        List<KnowledgeqaRespVO> records = knowledgeqaMapper.page(reqVO);

        PageResult<KnowledgeqaRespVO> pageResult = new PageResult<>();
        pageResult.setTotal((int) page.getTotal());
        pageResult.setRecords(records == null ? Collections.emptyList() : records);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> create(KnowledgeqaCreateParam param) {
        validateCreateParam(param);

        int rows = knowledgeqaMapper.add(param);
        if (rows != 1 || param.getId() == null) {
            log.error("创建知识问答失败: id={}, rows={}", param.getId(), rows);
            throw new ServiceException(KnowledgeqaCode.CREATE_KNOWLEDGEQA_FAILED);
        }

        LightweightKnowledgeBaseEnhancedQaDO qaDO = knowledgeqaMapper.selectById(param.getId());
        if (qaDO == null || !Objects.equals(qaDO.getDeleteFlag(), 0)) {
            log.error("创建知识问答后查询完整数据失败: id={}", param.getId());
            throw new ServiceException(KnowledgeqaCode.CREATE_KNOWLEDGEQA_FAILED);
        }

        try {
            addKnowledgeToVectorStore(qaDO);
        } catch (Exception e) {
            log.error("创建知识问答后同步向量库失败: id={}", param.getId(), e);
            throw new ServiceException(KnowledgeqaCode.CREATE_KNOWLEDGEQA_FAILED);
        }

        return CommonResult.success(param.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> update(KnowledgeqaUpdateParam param) {
        validateUpdateParam(param);

        LightweightKnowledgeBaseEnhancedQaDO oldDO = knowledgeqaMapper.selectById(param.getId());
        if (oldDO == null || !Objects.equals(oldDO.getDeleteFlag(), 0)) {
            log.error("更新知识问答失败，原数据不存在或已删除: id={}", param.getId());
            throw new ServiceException(KnowledgeqaCode.UPDATE_KNOWLEDGEQA_FAILED);
        }

        Document oldDocument = buildKnowledgeDocument(oldDO);

        int rows = knowledgeqaMapper.update(param);
        if (rows != 1) {
            log.error("更新知识问答失败: id={}, rows={}", param.getId(), rows);
            throw new ServiceException(KnowledgeqaCode.UPDATE_KNOWLEDGEQA_FAILED);
        }

        LightweightKnowledgeBaseEnhancedQaDO newDO = knowledgeqaMapper.selectById(param.getId());
        if (newDO == null || !Objects.equals(newDO.getDeleteFlag(), 0)) {
            log.error("更新知识问答后查询新数据失败: id={}", param.getId());
            throw new ServiceException(KnowledgeqaCode.UPDATE_KNOWLEDGEQA_FAILED);
        }

        try {
            updateKnowledgeInVectorStore(oldDocument, newDO);
        } catch (Exception e) {
            log.error("更新知识问答后同步向量库失败，数据库将回滚: id={}", param.getId(), e);
            throw new ServiceException(KnowledgeqaCode.UPDATE_KNOWLEDGEQA_FAILED);
        }

        return CommonResult.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> delete(Long id) {
        if (id == null) {
            throw new ServiceException(KnowledgeqaCode.ID_NOT_NULL);
        }

        LightweightKnowledgeBaseEnhancedQaDO existed = knowledgeqaMapper.selectById(id);
        if (existed == null || !Objects.equals(existed.getDeleteFlag(), 0)) {
            log.error("删除知识问答失败，数据不存在或已删除: id={}", id);
            throw new ServiceException(KnowledgeqaCode.DELETE_KNOWLEDGEQA_FAILED);
        }

        int rows = knowledgeqaMapper.updatedDeleteFlag(id);
        if (rows != 1) {
            log.error("逻辑删除知识问答失败: id={}, rows={}", id, rows);
            throw new ServiceException(KnowledgeqaCode.DELETE_KNOWLEDGEQA_FAILED);
        }

        try {
            deleteKnowledgeFromVectorStore(id);
        } catch (Exception e) {
            log.error("删除知识问答后同步删除向量失败，数据库将回滚: id={}", id, e);
            throw new ServiceException(KnowledgeqaCode.DELETE_KNOWLEDGEQA_FAILED);
        }

        return CommonResult.success(true);
    }

    /**
     * 新增知识到向量库
     */
    private void addKnowledgeToVectorStore(LightweightKnowledgeBaseEnhancedQaDO qaDO) {
        Document document = buildKnowledgeDocument(qaDO);
        simpleVectorStore.add(List.of(document));
    }

    /**
     * 更新知识到向量库
     *
     * 1. 先删旧向量
     * 2. 再加新向量
     * 3. 如果加新向量失败，则回补旧向量
     * 4. 然后继续抛异常，让数据库事务回滚
     */
    private void updateKnowledgeInVectorStore(Document oldDocument, LightweightKnowledgeBaseEnhancedQaDO newDO) {
        String id = String.valueOf(newDO.getId());

        simpleVectorStore.delete(List.of(id));

        try {
            Document newDocument = buildKnowledgeDocument(newDO);
            simpleVectorStore.add(List.of(newDocument));
        } catch (Exception e) {
            log.error("新增新向量失败，开始回补旧向量: id={}", id, e);
            try {
                simpleVectorStore.add(List.of(oldDocument));
            } catch (Exception rollbackEx) {
                log.error("回补旧向量也失败: id={}", id, rollbackEx);
            }
            throw e;
        }
    }

    /**
     * 删除知识对应向量
     */
    private void deleteKnowledgeFromVectorStore(Long id) {
        simpleVectorStore.delete(List.of(String.valueOf(id)));
    }

    /**
     * 构建知识库文档
     */
    private Document buildKnowledgeDocument(LightweightKnowledgeBaseEnhancedQaDO qaDO) {
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

        return Document.builder()
                .id(String.valueOf(qaDO.getId()))
                .text(content)
                .metadata(Map.of(
                        "cropId", String.valueOf(cropId == null ? 0L : cropId),
                        "pestId", String.valueOf(pestId == null ? 0L : pestId)
                ))
                .build();
    }

    private void validateCreateParam(KnowledgeqaCreateParam param) {
        if (param.getQuestion() == null || param.getQuestion().trim().isEmpty()) {
            throw new ServiceException(KnowledgeqaCode.QUESTION_NOT_NULL);
        }
        if (param.getAnswer() == null || param.getAnswer().trim().isEmpty()) {
            throw new ServiceException(KnowledgeqaCode.ANSWER_NOT_NULL);
        }
    }

    private void validateUpdateParam(KnowledgeqaUpdateParam param) {
        if (param.getId() == null) {
            throw new ServiceException(KnowledgeqaCode.ID_NOT_NULL);
        }
        if (param.getQuestion() == null || param.getQuestion().trim().isEmpty()) {
            throw new ServiceException(KnowledgeqaCode.QUESTION_NOT_NULL);
        }
        if (param.getAnswer() == null || param.getAnswer().trim().isEmpty()) {
            throw new ServiceException(KnowledgeqaCode.ANSWER_NOT_NULL);
        }
    }

    private String getNullableString(String value) {
        return value == null ? "" : value;
    }
}