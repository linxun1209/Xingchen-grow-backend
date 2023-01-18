package com.xingchen;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * @author xing'chen
 */
@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients
public class ContentApplication {

    @Value("${server.port}")
    String a;

    @Bean
    public String getTest(){
        System.out.println("a"+a);
        return a;
    }
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
