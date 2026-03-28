package com.campus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final OptionalJwtAuthenticationInterceptor optionalJwtInterceptor;
        private final AdminRoleInterceptor adminRoleInterceptor;

        @Value("${file.upload.path:./uploads}")
        private String uploadPath;

        /**
         * 注册拦截器
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                // 1. 严格认证拦截器 (排除公共接口)
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
                                                "/api/llm/**");

                // 2. 可选认证拦截器 (针对上述排除的、但可能需要用户上下文的接口)
                registry.addInterceptor(optionalJwtInterceptor)
                                .addPathPatterns(
                                                "/api/match/tutors",
                                                "/api/demand/list",
                                                "/api/demand/nearby",
                                                "/api/demand/list-with-match",
                                                "/api/tutor/public/**");

                // 3. 管理员角色鉴权
                registry.addInterceptor(adminRoleInterceptor)
                                .addPathPatterns("/api/admin/**")
                                .excludePathPatterns("/api/admin/auth/**");
        }

        /**
         * 配置静态资源映射
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 上传文件映射
                registry.addResourceHandler("/uploads/**")
                                .addResourceLocations("file:" + uploadPath + "/");

                // Knife4j API文档静态资源
                registry.addResourceHandler("doc.html")
                                .addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
}
