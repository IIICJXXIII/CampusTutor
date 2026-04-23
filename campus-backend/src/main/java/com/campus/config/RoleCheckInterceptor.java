package com.campus.config;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RoleCheckInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Integer role = UserContext.getRole();
        String uri = request.getRequestURI();

        if (role == null) {
            writeForbiddenResponse(response, "无访问权限");
            return false;
        }

        if (uri.startsWith("/api/tutor/") && !uri.startsWith("/api/tutor/public/")) {
            if (role != 1) {
                writeForbiddenResponse(response, "仅教员可访问");
                return false;
            }
        }

        if (uri.startsWith("/api/parent/")) {
            if (role != 2) {
                writeForbiddenResponse(response, "仅家长可访问");
                return false;
            }
        }

        return true;
    }

    private void writeForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.FORBIDDEN.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
