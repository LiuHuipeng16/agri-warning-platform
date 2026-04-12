package com.zhku.agriwarningplatform.common.interceptor;

import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 10:55
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 拦截鉴权
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("login")){
            log.info("登录成功，放行");
            return true;
        }
        String token = request.getHeader("token");
        if (token == null){
            log.warn("未登录，请登录");
            response.setStatus(401);
            return false;
        }
        try {
            JwtUtils.validateToken(token);
        } catch (Exception e) {
            log.warn("登录失败，请重新登录");
            response.setStatus(401);
            return false;
        }
        log.info("令牌合法，放行");
        return true;
    }
}
