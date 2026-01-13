package com.campus.config;

import com.campus.common.context.UserContext;
import com.campus.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选 JWT 认证拦截器
 * 用于那些既可以匿名访问，又可以针对登录用户提供个性化服务的接口
 * 如果携带有效Token，则设置UserContext；否则直接放行，不报错
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalJwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 尝试获取 Token
        String authHeader = request.getHeader(jwtUtils.getHeader());
        String token = jwtUtils.extractToken(authHeader);

        // 如果只有Token且有效，则设置上下文
        if (token != null && jwtUtils.validateToken(token)) {
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                Integer role = jwtUtils.getRoleFromToken(token);
                if (userId != null) {
                    UserContext.setUser(userId, role);
                    log.debug("Optional Auth Success: userId={}", userId);
                }
            } catch (Exception e) {
                // 解析失败也不阻断，当做匿名用户处理
                log.warn("Optional Auth Token Invalid: {}", e.getMessage());
            }
        }

        // 无论是否有Token，都放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        // 请求结束后清除上下文，防止线程复用导致的数据污染
        UserContext.clear();
    }
}
