package com.zhku.agriwarningplatform.common.config;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 12:10
 */
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * JWT / 登录拦截器放行路径
     */
    private List<String> jwtIgnoreUrls = new ArrayList<>();
}