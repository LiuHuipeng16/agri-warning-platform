package com.zhku.agriwarningplatform.module.crop.controller;

import com.zhku.agriwarningplatform.common.errorcode.CropErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.AliyunOSSOperator;
import com.zhku.agriwarningplatform.module.crop.service.CropService;
import com.zhku.agriwarningplatform.module.crop.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:31
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CropController {
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"} ;
    private final CropService cropservice;
    private final AliyunOSSOperator aliyunOSSOperator;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    @GetMapping("/crops/page")
    public PageResult<CropQueryRespVO> page(@Validated CropQueryReqVO cropQueryReqVO){
        log.info("查询作物：{}", cropQueryReqVO);
        return cropservice.pageQuery(cropQueryReqVO);
    }
    @GetMapping("/crops/detail/{id}")
    public CommonResult<DetailRespVO> detail(@Validated @PathVariable Long id){
        log.info("查询作物详情：{}", id);
        return CommonResult.success(cropservice.detail(id));
    }
    @PostMapping("/crops/create")
    public CommonResult<Long> create(@Validated @RequestBody CropQueryReqVO cropQueryReqVO){
        log.info("创建作物：{}", cropQueryReqVO);
        return cropservice.create(cropQueryReqVO);
    }
    @PutMapping("/crops/update")
    public CommonResult<Boolean> update(@Validated @RequestBody CropQueryReqVO cropQueryReqVO){
        log.info("更新作物：{}", cropQueryReqVO.getId());
        return CommonResult.success(cropservice.update(cropQueryReqVO));
    }
    @DeleteMapping("/crops/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id){
        log.info("删除作物：{}", id);
        return CommonResult.success(cropservice.delete(id));
    }
    @GetMapping("/crops/options")
    public CommonResult<List<CropOptionVO>> options(){
        log.info("获取作物选项");
        return CommonResult.success(cropservice.getCropOptions());
    }
    @PostMapping("/upload/image")
    public CommonResult<FileRespVO> uploadImage(@RequestParam("file") MultipartFile  file) throws Exception {
        log.info("上传图片：{}", file.getName());
        if (file.isEmpty()) {
            log.warn("上传文件为空");
            throw new ControllerException(CropErrorCode.FILE_NOT_EMPTY);
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            log.warn("不支持的文件类型：{}", contentType);
            throw  new ControllerException(CropErrorCode.FILE_TYPE_NOT_SUPPORT);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            log.warn("文件大小超限：{} bytes", file.getSize());
            throw new ControllerException(CropErrorCode.FILE_SIZE_TOO_LARGE);
        }
        if(file.getOriginalFilename()== null){
            log.warn("文件名不能为空");
            throw new ControllerException(CropErrorCode.FILE_NAME_NOT_EMPTY);
        }
        try {
        String fileUrl = aliyunOSSOperator.upload( file.getBytes(), file.getOriginalFilename());
        FileRespVO fileRespVO = new FileRespVO();
        fileRespVO.setFileUrl(fileUrl);
        fileRespVO.setOriginalName(file.getOriginalFilename());
        fileRespVO.setFileSize(file.getSize() + "");
        fileRespVO.setFileType(file.getContentType());
        return CommonResult.success(fileRespVO);
        } catch (Exception e) {
            log.error("上传图片失败,文件名：{}, 异常：{}", file.getOriginalFilename() , e.getMessage(), e);
            throw new ControllerException(CropErrorCode.FILE_UPLOAD_FAILED);
        }
    }
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
