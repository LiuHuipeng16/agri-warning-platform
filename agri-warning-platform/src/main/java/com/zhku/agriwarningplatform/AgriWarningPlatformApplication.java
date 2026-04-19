package com.zhku.agriwarningplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AgriWarningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriWarningPlatformApplication.class, args);
    }

}
