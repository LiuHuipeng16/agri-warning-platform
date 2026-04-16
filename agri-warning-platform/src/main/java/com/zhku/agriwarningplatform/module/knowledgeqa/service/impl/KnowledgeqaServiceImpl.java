package com.zhku.agriwarningplatform.module.knowledgeqa.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhku.agriwarningplatform.common.errorcode.KnowledgeqaCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.knowledgeqa.mapper.KnowledgeqaMapper;
import com.zhku.agriwarningplatform.module.knowledgeqa.service.KnowledgeqaService;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaRespVO;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeqaServiceImpl implements KnowledgeqaService {
    private final KnowledgeqaMapper knowledgeqaMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<KnowledgeqaRespVO> page(@Validated KnowledgeqaReqVO reqVO) {
        if (reqVO.getPageNum() == null || reqVO.getPageSize() == null){
            throw new ServiceException(KnowledgeqaCode.PAGE_PARAM_ERROR);
        }
        Page<KnowledgeqaRespVO> page = PageHelper.startPage(
                reqVO.getPageNum(),
                reqVO.getPageSize());
        List<KnowledgeqaRespVO> records =knowledgeqaMapper.page(reqVO);
        PageResult<KnowledgeqaRespVO> pageResult = new PageResult<>();
        pageResult.setTotal((int)page.getTotal());
        if (records==null){
        pageResult.setRecords(Collections.emptyList());
        }else {
            pageResult.setRecords(records);
        }
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> create(KnowledgeqaReqVO reqVO) {
        if (reqVO.getQuestion() == null || reqVO.getQuestion().isEmpty()){
            throw new ServiceException(KnowledgeqaCode.QUESTION_NOT_NULL);
        }
        if (reqVO.getAnswer() == null || reqVO.getAnswer().isEmpty()){
            throw new ServiceException(KnowledgeqaCode.ANSWER_NOT_NULL);
        }
        int rows = knowledgeqaMapper.add(reqVO);
        if (rows != 1){
            log.error("创建知识问答失败: ID {} 影响行数 {}", reqVO.getId() , rows);
            throw new ServiceException(KnowledgeqaCode.CREATE_KNOWLEDGEQA_FAILED);
        }
        return CommonResult.success(reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> update(KnowledgeqaReqVO reqVO) {
        if (reqVO.getId() == null){
            throw new ServiceException(KnowledgeqaCode.ID_NOT_NULL);
        }
        if (reqVO.getQuestion() == null || reqVO.getQuestion().isEmpty()){
            throw new ServiceException(KnowledgeqaCode.QUESTION_NOT_NULL);
        }
        if (reqVO.getAnswer() == null || reqVO.getAnswer().isEmpty()){
            throw new ServiceException(KnowledgeqaCode.ANSWER_NOT_NULL);
        }
        int rows = knowledgeqaMapper.update(reqVO);
        if (rows != 1){
            log.error("更新知识问答失败: ID {} 影响行数 {}", reqVO.getId() , rows);
            throw new ServiceException(KnowledgeqaCode.UPDATE_KNOWLEDGEQA_FAILED);
        }
        return CommonResult.success(reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> delete(@PathVariable @Min(value = 1, message = "ID必须大于0")Long id) {
        if (id == null){
            throw new ServiceException(KnowledgeqaCode.ID_NOT_NULL);
        }
        int rows = knowledgeqaMapper.delete(id);
        if (rows != 1){
            throw new ServiceException(KnowledgeqaCode.DELETE_KNOWLEDGEQA_FAILED);
        }
        return CommonResult.success( true);
    }
}
