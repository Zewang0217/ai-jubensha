package org.jubensha.aijubenshabackend.core.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String host;
    
    @Value("${spring.data.redis.port}")
    private int port;
    
    @Value("${spring.data.redis.password}")
    private String password;
    
    @Value("${spring.data.redis.database}")
    private int database;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 使用 RedisStandaloneConfiguration 替代过时的 setter 方法
        org.springframework.data.redis.connection.RedisStandaloneConfiguration redisConfig 
            = new org.springframework.data.redis.connection.RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setPassword(org.springframework.data.redis.connection.RedisPassword.of(password));
        redisConfig.setDatabase(database);
        
        return new LettuceConnectionFactory(redisConfig);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory());
        // 设置键的序列化器为字符串类型
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器为 JSON 格式（推荐用于 Spring Data Redis 4.0+）
        template.setValueSerializer(RedisSerializer.json());
        // 设置哈希键的序列化器为字符串类型
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置哈希值的序列化器为 JSON 格式（推荐用于 Spring Data Redis 4.0+）
        template.setHashValueSerializer(RedisSerializer.json());
        // 初始化 RedisTemplate
        template.afterPropertiesSet();
        return template;
    }
}