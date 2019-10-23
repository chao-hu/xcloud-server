package com.xxx.xcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author ruzz
 *
 */
@SpringBootApplication
public class Startup {

    public static void main(String[] args) throws Exception {

        SpringApplication application = new SpringApplication(Startup.class);
        application.run(args);
    }
}
