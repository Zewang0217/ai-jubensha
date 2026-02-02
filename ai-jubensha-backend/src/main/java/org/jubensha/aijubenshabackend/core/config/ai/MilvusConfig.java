package org.jubensha.aijubenshabackend.core.config.ai;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库配置类
 * 配置Milvus服务客户端，用于与Milvus向量数据库交互
 * 
 * 注意：以下部分需要使用Milvus向量数据库：
 * 1. milvusClient方法：创建MilvusServiceClient实例，
 *    用于执行Milvus的各种操作（insert、search、delete等）
 */
@Configuration
public class MilvusConfig {
    
    @Value("${milvus.host}")
    private String host;
    
    @Value("${milvus.port}")
    private int port;
    
    @Value("${milvus.token:root:Milvus}")
    private String token;
    
    @Bean
    public MilvusClientV2 milvusClient() {
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri("http://" + host + ":" + port)
            .token(token)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);
        return client;
    }


}