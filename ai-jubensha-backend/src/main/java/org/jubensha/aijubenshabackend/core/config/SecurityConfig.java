package org.jubensha.aijubenshabackend.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security配置类
 * 配置API接口的安全访问策略
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 允许API接口无需认证访问，保护其他敏感路径
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF保护，因为这是API服务
            .authorizeHttpRequests(authz -> authz
                // 允许访问API接口
                .requestMatchers("/api/**").permitAll()
                // 允许WebSocket连接
                .requestMatchers("/ws/**").permitAll()
                // 允许访问API文档
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // 允许访问健康检查等监控端点
                .requestMatchers("/actuator/**").permitAll()
                // 允许访问静态资源
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/**/*.js", "/**/*.css", "/**/*.html").permitAll()
                // 其他所有请求都需要认证（可以根据需要调整）
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * CORS配置
     * 允许前端跨域访问
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 生产环境中应限制为具体的域名
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}