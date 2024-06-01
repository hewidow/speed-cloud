package com.orgspeedcloud.speedcloud.core.conf;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonConfiguration {
    @Bean
    RedissonClient redisson() throws IOException {
        Config config = Config.fromYAML(RedissonConfiguration.class.getClassLoader().getResource("redisson.yaml"));
        return Redisson.create(config);
    }
}
