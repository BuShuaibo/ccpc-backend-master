package com.neuq.ccpcbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.neuq.ccpcbackend.mapper")
public class CcpcBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcpcBackendApplication.class, args);
    }

}
