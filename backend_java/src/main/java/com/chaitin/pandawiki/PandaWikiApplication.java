package com.chaitin.pandawiki;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * PandaWiki 应用程序入口
 * 
 * @author chaitin
 */
@SpringBootApplication
@MapperScan("com.chaitin.pandawiki.mapper")
@EnableCaching
public class PandaWikiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PandaWikiApplication.class, args);
    }
} 