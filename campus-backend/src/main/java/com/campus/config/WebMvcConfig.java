package com.campus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final OptionalJwtAuthenticationInterceptor optionalJwtInterceptor;
        private final AdminRoleInterceptor adminRoleInterceptor;
        private final RoleCheckInterceptor roleCheckInterceptor;
        private final LoginRateLimitInterceptor loginRateLimitInterceptor;

        @Value("${file.upload.path:./uploads}")
        private String uploadPath;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(loginRateLimitInterceptor)
                                .addPathPatterns("/api/auth/login");

                registry.addInterceptor(jwtAuthenticationFilter)
                                .addPathPatterns("/api/**")
                                .excludePathPatterns(
                                                "/api/auth/**",
                                                "/api/admin/auth/**",
                                                "/api/file/**",
                                                "/api/match/tutors",
                                                "/api/demand/list",
                                                "/api/demand/nearby",
                                                "/api/demand/list-with-match",
                                                "/api/tutor/public/**",
                                                "/api/llm/**",
                                                "/api/community/posts");

                registry.addInterceptor(optionalJwtInterceptor)
                                .addPathPatterns(
                                                "/api/match/tutors",
                                                "/api/demand/list",
                                                "/api/demand/nearby",
                                                "/api/demand/list-with-match",
                                                "/api/tutor/public/**",
                                                "/api/community/posts");

                registry.addInterceptor(roleCheckInterceptor)
                                .addPathPatterns("/api/tutor/**", "/api/parent/**")
                                .excludePathPatterns("/api/tutor/public/**");

                registry.addInterceptor(adminRoleInterceptor)
                                .addPathPatterns("/api/admin/**")
                                .excludePathPatterns("/api/admin/auth/**");
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                                .addResourceLocations("file:" + uploadPath + "/");

                registry.addResourceHandler("doc.html")
                                .addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
}
