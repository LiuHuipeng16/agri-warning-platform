package com.zhku.agriwarningplatform.common.interceptor;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
            log.info("进入登录功能，放行");
            return true;
        }
        if (requestURI.contains("register")){
            log.info("进入注册功能，放行");
            return true;
        }
        String token = request.getHeader("token");
        if (token == null){
            log.warn("未登录，请登录");
            response.setStatus(401);
            return false;
        }
        if (requestURI.contains("users")){
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)){
                log.warn("你的权限不足");
                response.setStatus(403);
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }
        try {
            JwtUtils.validateToken(token);
        } catch (Exception e) {
            log.warn("登录失效，请重新登录");
            response.setStatus(401);
            return false;
        }
        log.info("令牌合法，放行");
        return true;
    }
}
