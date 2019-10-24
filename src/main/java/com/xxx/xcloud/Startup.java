package com.xxx.xcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName: Startup
 * @Description: 工程启动类
 * @author huchao
 * @date 2019年10月24日
 *
 */
@SpringBootApplication
public class Startup {

    /**
     * @Title: main
     * @Description: 入口方法
     * @param @param args
     * @param @throws Exception 参数
     * @return void 返回类型
     * @throws
     */
    public static void main(String[] args) throws Exception {

        SpringApplication application = new SpringApplication(Startup.class);
        application.run(args);
    }
}
