package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库实体
 */
@Data
@Slf4j
@TableName("knowledge_base")
public class KnowledgeBase {

    @TableId
    private String id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 数据集ID
     */
    @JsonProperty("dataset_id")
    @TableField("dataset_id")
    private String datasetId;

    /**
     * 访问设置 (JSON字符串存储)
     */
    @TableField("access_settings")
    private String accessSettings;

    /**
     * 所有者ID
     */
    @JsonProperty("owner_id")
    @TableField("owner_id")
    private String ownerId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("created_at")
    @TableField("create_time")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("updated_at")
    @TableField("update_time")
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @TableField("deleted")
    private Boolean deleted;

    // ========== JSON 字段的便利方法 ==========

    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 获取默认访问设置
     */
    private AccessSettings getDefaultAccessSettings() {
        AccessSettings settings = new AccessSettings();
        settings.setHosts(List.of("localhost"));
        settings.setPorts(List.of(8080));
        settings.setSslPorts(List.of());
        settings.setPrivateKey("");
        settings.setPublicKey("");
        settings.setBaseUrl("");
        settings.setTrustedProxies(null);

        SimpleAuth simpleAuth = new SimpleAuth();
        simpleAuth.setEnabled(false);
        simpleAuth.setPassword("");
        settings.setSimpleAuth(simpleAuth);

        return settings;
    }

    /**
     * 访问设置内部类
     */
    @Data
    public static class AccessSettings {
        private List<String> hosts;
        private List<Integer> ports;
        @JsonProperty("ssl_ports")
        private List<Integer> sslPorts;
        @JsonProperty("private_key")
        private String privateKey;
        @JsonProperty("public_key")
        private String publicKey;
        @JsonProperty("base_url")
        private String baseUrl;
        @JsonProperty("trusted_proxies")
        private List<String> trustedProxies;
        @JsonProperty("simple_auth")
        private SimpleAuth simpleAuth;
    }

    /**
     * 简单认证设置
     */
    @Data
    public static class SimpleAuth {
        private Boolean enabled;
        private String password;
    }

    /**
     * 统计信息内部类 (暂时返回固定值，后续可以动态计算)
     */
    @JsonProperty("stats")
    public Stats getStats() {
        Stats stats = new Stats();
        stats.setDocCount(0L);
        stats.setChunkCount(0L);
        stats.setWordCount(0L);
        return stats;
    }

    @Data
    public static class Stats {
        @JsonProperty("doc_count")
        private Long docCount;
        @JsonProperty("chunk_count")
        private Long chunkCount;
        @JsonProperty("word_count")
        private Long wordCount;
    }
}
