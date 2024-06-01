package com.orgspeedcloud.speedcloud.core.conf;

import com.orgspeedcloud.speedcloud.core.aop.authentication.UserArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author Chen
 */
@Configuration
public class SpeedCloudMvcConfiguration implements WebMvcConfigurer {

    @Bean
    public UserArgumentResolver userArgumentResolver(){
        return new UserArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver());
    }
}
