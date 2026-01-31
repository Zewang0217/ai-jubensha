package org.jubensha.aijubenshabackend.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * 模块依赖配置类
 * 定义模块间的依赖关系
 */
@Configuration
public class ModuleDependencyConfig {
    
    /**
     * 模块依赖关系定义
     * 
     * 依赖层级：
     * 1. core模块：核心配置和工具类，被其他所有模块依赖
     * 2. domain模块：领域模型和仓库接口，被services模块依赖
     * 3. infrastructure模块：基础设施服务，被services模块依赖
     * 4. services模块：业务服务，被api和game模块依赖
     * 5. ai模块：AI相关功能，被services和game模块依赖
     * 6. game模块：游戏相关功能，被api模块依赖
     * 7. api模块：接口层，依赖其他所有模块
     */
    
    // 核心模块配置
    // 领域模块配置
    // 基础设施模块配置
    // 服务模块配置
    // AI模块配置
    // 游戏模块配置
    // API模块配置
}
