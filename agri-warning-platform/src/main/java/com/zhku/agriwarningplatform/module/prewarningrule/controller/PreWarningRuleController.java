package com.zhku.agriwarningplatform.module.prewarningrule.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:33
 */

import com.zhku.agriwarningplatform.common.errorcode.PreWarningRuleErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleChangeStatusParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleCreateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleOptionParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRulePageParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleUpdateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.vo.PreWarningRuleDetailVO;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.vo.PreWarningRuleOptionVO;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.vo.PreWarningRulePageVO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.PreWarningRuleService;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleOptionDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRulePageDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/prewarningRules")
public class PreWarningRuleController {

    @Resource
    private PreWarningRuleService preWarningRuleService;

    @GetMapping("/page")
    public CommonResult<PageResult<PreWarningRulePageVO>> page(@Valid @ModelAttribute PreWarningRulePageParam param) {

        if (param.getPageNum() == null) {
            throw new ControllerException(PreWarningRuleErrorCode.PAGE_NUM_EMPTY);
        }
        if (param.getPageNum() < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.PAGE_NUM_INVALID);
        }
        if (param.getPageSize() == null) {
            throw new ControllerException(PreWarningRuleErrorCode.PAGE_SIZE_EMPTY);
        }
        if (param.getPageSize() < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.PAGE_SIZE_INVALID);
        }

        PreWarningRulePageDTO pageDTO = preWarningRuleService.page(param);

        PageResult<PreWarningRulePageVO> pageResult = new PageResult<>();
        pageResult.setTotal(pageDTO.getTotal());
        pageResult.setRecords(convertPageVOList(pageDTO.getRecords()));
        return CommonResult.success(pageResult);
    }

    @GetMapping("/detail/{ruleId}")
    public CommonResult<PreWarningRuleDetailVO> detail(@PathVariable("ruleId") @Min(value = 1, message = "规则ID不合法") Long ruleId) {
        if (ruleId == null) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_EMPTY);
        }
        if (ruleId < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_INVALID);
        }

        PreWarningRuleDTO dto = preWarningRuleService.detail(ruleId);
        return CommonResult.success(convertDetailVO(dto));
    }

    @PostMapping("/create")
    public CommonResult<Long> create(@Valid @RequestBody PreWarningRuleCreateParam param) {

        return CommonResult.success(preWarningRuleService.create(param));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody PreWarningRuleUpdateParam param) {
        if (param.getRuleId() == null) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_EMPTY);
        }
        if (param.getRuleId() < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_INVALID);
        }

        return CommonResult.success(preWarningRuleService.update(param));
    }

    @DeleteMapping("/delete/{ruleId}")
    public CommonResult<Boolean> delete(@PathVariable("ruleId") @Min(value = 1, message = "规则ID不合法") Long ruleId) {
        if (ruleId == null) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_EMPTY);
        }
        if (ruleId < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_INVALID);
        }

        return CommonResult.success(preWarningRuleService.delete(ruleId));
    }

    @PutMapping("/changeStatus")
    public CommonResult<Boolean> changeStatus(@Valid @RequestBody PreWarningRuleChangeStatusParam param) {

        if (param.getRuleId() == null) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_EMPTY);
        }
        if (param.getRuleId() < 1) {
            throw new ControllerException(PreWarningRuleErrorCode.RULE_ID_INVALID);
        }

        return CommonResult.success(preWarningRuleService.changeStatus(param));
    }

    @GetMapping("/options")
    public CommonResult<List<PreWarningRuleOptionVO>> options(@Valid @ModelAttribute PreWarningRuleOptionParam param){

        List<PreWarningRuleOptionDTO> dtoList = preWarningRuleService.options(param);
        return CommonResult.success(convertOptionVOList(dtoList));
    }

    private List<PreWarningRulePageVO> convertPageVOList(List<PreWarningRuleDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }

        List<PreWarningRulePageVO> voList = new ArrayList<>(dtoList.size());
        for (PreWarningRuleDTO dto : dtoList) {
            PreWarningRulePageVO vo = new PreWarningRulePageVO();
            vo.setRuleId(dto.getId());
            vo.setRuleName(dto.getRuleName());
            vo.setCropId(dto.getCropId());
            vo.setCropName(dto.getCropName());
            vo.setPestId(dto.getPestId());
            vo.setPestName(dto.getPestName());
            vo.setRiskLevel(dto.getRiskLevel());
            vo.setRuleStatus(dto.getRuleStatus());
            vo.setMinTemp(dto.getMinTemp());
            vo.setMaxTemp(dto.getMaxTemp());
            vo.setMinHumidity(dto.getMinHumidity());
            vo.setMaxHumidity(dto.getMaxHumidity());
            vo.setMinPrecipitation(dto.getMinPrecipitation());
            vo.setMaxPrecipitation(dto.getMaxPrecipitation());
            vo.setMinWindSpeed(dto.getMinWindSpeed());
            vo.setMaxWindSpeed(dto.getMaxWindSpeed());
            vo.setGmtCreate(dto.getGmtCreate());
            voList.add(vo);
        }
        return voList;
    }

    private PreWarningRuleDetailVO convertDetailVO(PreWarningRuleDTO dto) {
        if (dto == null) {
            return null;
        }

        PreWarningRuleDetailVO vo = new PreWarningRuleDetailVO();
        vo.setRuleId(dto.getId());
        vo.setRuleName(dto.getRuleName());
        vo.setCropId(dto.getCropId());
        vo.setCropName(dto.getCropName());
        vo.setPestId(dto.getPestId());
        vo.setPestName(dto.getPestName());
        vo.setMinTemp(dto.getMinTemp());
        vo.setMaxTemp(dto.getMaxTemp());
        vo.setMinHumidity(dto.getMinHumidity());
        vo.setMaxHumidity(dto.getMaxHumidity());
        vo.setMinPrecipitation(dto.getMinPrecipitation());
        vo.setMaxPrecipitation(dto.getMaxPrecipitation());
        vo.setMinWindSpeed(dto.getMinWindSpeed());
        vo.setMaxWindSpeed(dto.getMaxWindSpeed());
        vo.setRiskLevel(dto.getRiskLevel());
        vo.setSuggestion(dto.getSuggestion());
        vo.setRuleStatus(dto.getRuleStatus());
        vo.setGmtCreate(dto.getGmtCreate());
        vo.setGmtModified(dto.getGmtModified());
        return vo;
    }

    private List<PreWarningRuleOptionVO> convertOptionVOList(List<PreWarningRuleOptionDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }

        List<PreWarningRuleOptionVO> voList = new ArrayList<>(dtoList.size());
        for (PreWarningRuleOptionDTO dto : dtoList) {
            PreWarningRuleOptionVO vo = new PreWarningRuleOptionVO();
            vo.setLabel(dto.getLabel());
            vo.setValue(dto.getValue());
            voList.add(vo);
        }
        return voList;
    }
}