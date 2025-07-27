package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用实体
 */
@Data
@TableName(value = "app", autoResultMap = true)
public class App {

    @TableId
    private String id;

    private String kbId;

    private String name;

    private Integer type;

    private String settings;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 应用类型
     */
    public enum AppType {
        WEB(1),
        WIDGET(2),
        DINGTALK_BOT(3),
        FEISHU_BOT(4),
        WECHAT_BOT(5),
        WECHAT_SERVICE_BOT(6),
        DISCORD_BOT(7);

        private final int value;

        AppType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 应用配置
     */
    @Data
    public static class AppSettings {
        // 导航配置
        private String title;
        private String icon;
        private List<Map<String, Object>> btns;

        // 欢迎配置
        private String welcomeStr;
        private String searchPlaceholder;
        private List<String> recommendQuestions;
        private List<String> recommendNodeIds;

        // SEO配置
        private String desc;
        private String keyword;
        private Boolean autoSitemap;

        // 注入代码
        private String headCode;
        private String bodyCode;

        // 钉钉机器人配置
        private String dingtalkBotClientId;
        private String dingtalkBotClientSecret;
        private String dingtalkBotTemplateId;

        // 飞书机器人配置
        private String feishuBotAppId;
        private String feishuBotAppSecret;

        // 微信机器人配置
        private String wechatAppToken;
        private String wechatAppEncodingAESKey;
        private String wechatAppCorpId;
        private String wechatAppSecret;
        private String wechatAppAgentId;
    }
}
