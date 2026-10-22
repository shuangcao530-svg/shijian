package com.shijian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.shijian.mapper")
public class ShijianApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShijianApplication.class, args);
    }
}
