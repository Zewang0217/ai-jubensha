package org.jubensha.aijubenshabackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用程序核心配置类
 */
@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
public class AppConfig {
    // 应用程序核心配置
}
