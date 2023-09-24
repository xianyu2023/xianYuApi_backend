package com.yupi.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author xianyu
 */
@Configuration
public class RedisTemplateConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {//从外界自动注入redis连接工厂
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //指定key的序列化器为redis的string序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //设置redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
