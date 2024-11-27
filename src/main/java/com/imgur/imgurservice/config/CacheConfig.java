package com.imgur.imgurservice.config;

import com.imgur.imgurservice.model.UserResponse;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;


@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheConfiguration<String, UserResponse> usersCacheConfig,
                                     CacheConfiguration<String, List> imagesCacheConfig) {
        EhcacheCachingProvider provider = getCachingProvider();
        Map<String, CacheConfiguration<?,?>> caches = new HashMap<>();

        caches.put("users", usersCacheConfig);
        caches.put("imagesByUser", imagesCacheConfig);

        DefaultConfiguration configuration = new DefaultConfiguration(caches, this.getClass().getClassLoader());
        return new JCacheCacheManager(provider.getCacheManager(provider.getDefaultURI(), configuration));

    }

    @Bean
    public CacheConfiguration<String, UserResponse> usersCacheConfig(@Value("${cache.user.size}") int size, @Value("${cache.user.ttl}") long timeToLive){
        return buildConfiguration(String.class, UserResponse.class, size, timeToLive, null);
    }


    @Bean
    public CacheConfiguration<String, List> imagesCacheConfig(@Value("${cache.image.size}") int size, @Value("${cache.image.ttl}") long timeToLive){
        return buildConfiguration(String.class, List.class, size, timeToLive, null);
    }

    protected <K extends Object, V extends Object> CacheConfiguration<K,V> buildConfiguration(Class<K> key, Class<V> value, int heapSize, Long timeToLive, Long timeToIdle){
        ResourcePools pools = ResourcePoolsBuilder.heap(heapSize).build();
        CacheConfigurationBuilder<K, V> configBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(key, value, pools);

        if (nonNull(timeToLive)){
            configBuilder = configBuilder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMillis(timeToLive)));
        } else if (nonNull(timeToIdle)) {
            configBuilder = configBuilder.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMillis(timeToIdle)));
        }

        return configBuilder.build();
    }

    private EhcacheCachingProvider getCachingProvider(){
        return (EhcacheCachingProvider) Caching.getCachingProvider();
    }
}

