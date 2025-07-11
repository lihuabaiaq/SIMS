package com.sims.interceptor;

import com.sims.config.JwtConfiguration;
import com.sims.util.JwtUtil;
import com.sims.util.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    JwtConfiguration jwtConfiguration;

    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        String requestURI = request.getRequestURI();
        // 放行 Knife4j 所有相关资源
        if (requestURI.startsWith("/doc.html") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/webjars/") ||   // 静态资源
                requestURI.startsWith("/swagger-ui/") || // Swagger UI 资源（可选）
                requestURI.startsWith("/swagger-resources")||
                requestURI.equals("/error")) {  // API 元信息
            return true;
        }

        String token = request.getHeader(jwtConfiguration.getTokenName());
        if (token.startsWith("Bearer ")) {
            // 如果是从请求头获取的，就截取 "Bearer " 后面的部分
            token = token.substring(7);
        }

        if (token==null||token.isEmpty()){
            response.setStatus(401);
            log.info("token为空");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtConfiguration.getSecretKey(), token);
            Long id = claims.get("id", Long.class);
            UserHolder.saveId(id);
            log.info("校验成功");
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.remove();
    }
}
