package com.campus.config;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.common.result.ResultCode;
import com.campus.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Token
        String authHeader = request.getHeader(jwtUtils.getHeader());
        String token = jwtUtils.extractToken(authHeader);

        if (token == null || !jwtUtils.validateToken(token)) {
            writeUnauthorizedResponse(response);
            return false;
        }

        // 解析用户信息并存入上下文
        Long userId = jwtUtils.getUserIdFromToken(token);
        Integer role = jwtUtils.getRoleFromToken(token);
        UserContext.setUser(userId, role);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除上下文
        UserContext.clear();
    }

    /**
     * 写入未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
