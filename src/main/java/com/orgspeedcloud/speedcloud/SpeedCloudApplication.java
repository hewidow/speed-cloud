package com.orgspeedcloud.speedcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Chen
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class SpeedCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeedCloudApplication.class, args);
    }

}
