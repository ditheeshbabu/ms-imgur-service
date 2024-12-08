//package com.imgur.imgurservice.config;
//
//import com.imgur.imgurservice.model.UserResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import selogger.net.bytebuddy.description.type.TypeList;
//
//import java.awt.*;
//import java.time.Duration;
//import java.util.List;
//
//@Slf4j
//@Configuration
//public class CacheConfig {
//
//    @Value("${imgur.redis.host}")
//    String redisCacheHostName;
//
//    @Value("${imgur.redis.auth-token}")
//    String redisCacheAuthToken;
//
//    @Bean
//    public RedisCacheConfiguration cacheConfiguration(){
//        log.info("establishing cache configuration...");
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//    }
//
//    @Primary
//    @Bean
//    public LettuceConnectionFactory redisConnectionFacatory(){
//        log.info("establishing redis Connection Factaory....");
//        Duration shutdownTimeout = Duration.ofSeconds(1);
//
//        RedisStandaloneConfiguration clusterConfiguration = new RedisStandaloneConfiguration();
//        clusterConfiguration.setHostName(redisCacheHostName);
//        clusterConfiguration.setPassword(redisCacheAuthToken);
//
//        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder().useSsl().and().shutdownTimeout(shutdownTimeout).build();
//        log.info("redis connection factory establishment complete");
//
//        return new LettuceConnectionFactory(clusterConfiguration, lettuceClientConfiguration);
//    }
//
//    @Bean
//    public RedisTemplate<String, List> redisTemplate(){
//
//        log.info("creating redis template");
//        final RedisTemplate<String, List> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFacatory());
//        log.info("redis template creation complete");
//        return redisTemplate;
//    }
//
//    @Bean
//    public RedisTemplate<String, UserResponse> redisUserTemplate(){
//        log.info("creating UserResponse redis template");
//        final RedisTemplate<String, UserResponse> redisUserTemplate = new RedisTemplate<>();
//        redisUserTemplate.setConnectionFactory(redisConnectionFacatory());
//        redisUserTemplate.setKeySerializer(new StringRedisSerializer());
//        redisUserTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//        log.info("redis UserResponse template creation complete");
//        return redisUserTemplate;
//    }
//
//}