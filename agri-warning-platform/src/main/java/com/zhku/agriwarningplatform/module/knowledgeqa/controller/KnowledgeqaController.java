package com.zhku.agriwarningplatform.module.knowledgeqa.controller;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaCreateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaUpdateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.service.KnowledgeqaService;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:32
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledgeQa")
@RequiredArgsConstructor
public class KnowledgeqaController {
    private final KnowledgeqaService knowledgeqaService;
    @GetMapping("/page")
    public PageResult<KnowledgeqaRespVO> page(@Validated KnowledgeqaReqVO reqVO){
        log.info("分页查询：{}", reqVO);

        return knowledgeqaService.page(reqVO);
    }
    @PostMapping("/create")
    public CommonResult<Long> create(@Validated @RequestBody KnowledgeqaCreateParam param){
        log.info("新增知识库：{}", param);
        return knowledgeqaService.create(param);
    }
    @PutMapping("/update")
    public CommonResult<Long> update(@Validated @RequestBody KnowledgeqaUpdateParam param){
        log.info("更新知识库：{}", param);
        return knowledgeqaService.update(param);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id){
        log.info("删除知识库：{}", id);
        return knowledgeqaService.delete(id);
    }

}
