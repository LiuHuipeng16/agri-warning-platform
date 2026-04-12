package com.zhku.agriwarningplatform.module.pestenvironment.service;

import com.zhku.agriwarningplatform.module.pestenvironment.controller.param.PestEnvironmentSaveOrUpdateParam;
import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:24
 */


public interface PestEnvironmentService {

    PestEnvironmentDTO detailByPestId(Long pestId);

    Boolean saveOrUpdate(PestEnvironmentSaveOrUpdateParam param);
}