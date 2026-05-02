package com.campus.config;

import com.campus.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 30;
    private static final String RATE_LIMIT_PREFIX = "login:rate:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp;

        try {
            String countStr = stringRedisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            if (count >= MAX_ATTEMPTS) {
                Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                writeRateLimitResponse(response, ttl != null ? ttl : WINDOW_SECONDS);
                return false;
            }

            stringRedisTemplate.opsForValue().increment(key);
            if (count == 0) {
                stringRedisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("Redis不可用，跳过登录限速检查");
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void writeRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        Result<Void> result = Result.fail(429, "登录尝试过于频繁，请" + retryAfterSeconds + "秒后再试");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
