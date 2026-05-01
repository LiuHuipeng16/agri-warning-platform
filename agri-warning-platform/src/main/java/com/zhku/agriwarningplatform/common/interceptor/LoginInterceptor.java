package com.zhku.agriwarningplatform.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();

        // 放行登录、注册接口
        if (requestURI.contains("login") || requestURI.contains("register")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        // 未携带 token
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        String token = authorization.substring(7);

        log.info("当前请求token={}", token);

        boolean valid = jwtUtils.validateToken(token);

        // token 无效或过期
        if (!valid) {
            log.warn("登录失效，请重新登录");
            writeUnauthorized(response, "登录失效，请重新登录");
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        return true;
    }

    /**
     * 统一返回未登录 / 登录失效 JSON
     */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        CommonResult<Void> result = CommonResult.error(401, msg);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}