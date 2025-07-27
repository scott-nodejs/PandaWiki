package com.chaitin.pandawiki.config;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 七牛云配置类
 */
@Data
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 私有密钥
     */
    private String secretKey;

    /**
     * 存储空间名
     */
    private String bucket;

    /**
     * 访问域名
     */
    private String domain = "https://img.hazer.top/";

    /**
     * 存储区域
     */
    private String region = "华东";

    /**
     * 创建七牛云Auth对象
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    /**
     * 创建七牛云上传管理器
     */
    @Bean
    public UploadManager uploadManager() {
        Region region = Region.autoRegion();
        Configuration c = new Configuration(region);

        return new UploadManager(c);
    }

    /**
     * 根据区域名称获取区域对象
     */
    private Region getRegionByName(String regionName) {
        switch (regionName) {
            case "华东":
                return Region.region0();
            case "华北":
                return Region.region1();
            case "华南":
                return Region.region2();
            case "北美":
                return Region.regionNa0();
            case "东南亚":
                return Region.regionAs0();
            default:
                return Region.autoRegion();
        }
    }
}
