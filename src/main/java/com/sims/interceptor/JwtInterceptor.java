package com.sims.interceptor;

import com.sims.config.JwtConfiguration;
import com.sims.util.JwtUtil;
import com.sims.util.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    JwtConfiguration jwtConfiguration;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        String requestURI = request.getRequestURI();
        System.out.println(request);
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
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        if (token.startsWith("Bearer ")) {
            // 如果是从请求头获取的，就截取 "Bearer " 后面的部分
            System.out.println(token);
            System.out.println("34235234");
            token = token.substring(7);
            //模拟 Spring Security 这样的专业安全框架，进行token解析前的预处理
        } else {
            // 2. 如果请求头没有，就直接从URL参数获取原始token
        }

        if (token==null||token.isEmpty()){
            response.setStatus(401);
            System.out.println("token失效，不放行");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtConfiguration.getSecretKey(), token);
            Long id = claims.get("id", Long.class);
            UserHolder.saveId(id);
            System.out.println("token有效");
            return true;
        } catch (Exception e) {
            System.out.println("返回异常");
            response.setStatus(401);
            return false;
        }
    }
}
