package com.kin.family;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 家谱应用启动类
 *
 * @author candong
 */
@SpringBootApplication
@MapperScan("com.kin.family.mapper")
public class FamilyTreeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyTreeApplication.class, args);
    }
}
