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
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        if (requestURI.contains("login")) {
            log.info("进入登录功能，放行");
            return true;
        }

        if (requestURI.contains("register")) {
            log.info("进入注册功能，放行");
            return true;
        }

        if (requestURI.contains("/doc.html")
                || requestURI.contains("/swagger-ui.html")
                || requestURI.contains("/swagger-ui/")
                || requestURI.contains("/v3/api-docs")
                || requestURI.contains("/webjars/")) {
            log.info("进入接口文档，放行");
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            log.warn("未登录，请登录");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            String json = "{\n\"code\":401,\n\"msg\":\"未登录\",\n\"data\":null\n}";
            response.getWriter().write(json);
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        }

        String token = authorization.substring(7);
        if (token == null || token.isBlank()) {
            log.warn("未登录，请登录");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            String json = "{\n\"code\":401,\n\"msg\":\"未登录\",\n\"data\":null\n}";
            response.getWriter().write(json);
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        }

        try {
            JwtUtils.validateToken(token);
        } catch (Exception e) {
            log.warn("登录失效，请重新登录");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            String json = "{\n\"code\":401,\n\"msg\":\"登录失效\",\n\"data\":null\n}";
            response.getWriter().write(json);
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        }

        if (requestURI.contains("admin/users")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("crops/create")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("crops/update")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("crops/delete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("knowledgeQa/page")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("knowledgeQa/create")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("knowledgeQa/update")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("knowledgeQa/delete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("pests/create")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("pests/update")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("pests/delete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("pestEnvironment/saveOrUpdate")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("warnings/delete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("warnings/batchDelete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("warnings/generateToday")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("warnings/generateForecast")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/page")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/detail")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/create")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/update")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/delete")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/changeStatus")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        if (requestURI.contains("prewarningRules/options")) {
            String role = JwtUtils.getRoleFromToken(token);
            if ("USER".equals(role)) {
                log.warn("你的权限不足");
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                String json = "{\n\"code\":403,\n\"msg\":\"权限不足\",\n\"data\":null\n}";
                response.getWriter().write(json);
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
            log.info("用户权限验证通过");
            return true;
        }

        log.info("令牌合法，放行");
        return true;
    }
}