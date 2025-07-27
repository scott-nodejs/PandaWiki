package com.chaitin.pandawiki.model.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应用信息视图对象
 *
 * @author chaitin
 */
@Data
public class AppInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 知识库ID
     */
    @JsonProperty("kb_id")
    private String kbId;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    @JsonProperty("desc")
    private String description;

    /**
     * 应用设置
     */
    private Settings settings;

    private List<NodeWithRecommendationsVO> recommend_nodes;

    /**
     * 应用设置
     */
    @Data
    public static class Settings {

        /**
         * 标题
         */
        private String title;

        /**
         * 描述
         */
        private String welcome_str;

        /**
         * 图标
         */
        private String icon;

        /**
         * 搜索提示
         */
        private String search_placeholder;

        /**
         * 推荐问题
         */
        private JSONArray recommend_questions;


        private JSONObject theme_and_style;

        private JSONObject catalog_settings;

        private JSONObject footer_settings;

        /**
         * Widget机器人设置
         */
        @JsonProperty("widget_bot_settings")
        private WidgetBotSettings widgetBotSettings;

        private String theme_mode;

        /**
         * 页面head代码，用于加载CSS等
         */
        @JsonProperty("head_code")
        private String headCode;

        /**
         * 页面body代码，用于加载widget脚本等
         */
        @JsonProperty("body_code")
        private String bodyCode;

    }

    /**
     * Widget机器人设置
     */
    @Data
    public static class WidgetBotSettings {

        /**
         * 是否开启
         */
        @JsonProperty("is_open")
        private Boolean is_open = false;

        /**
         * 主题模式
         */
        @JsonProperty("theme_mode")
        private String theme_mode = "light";

        @JsonProperty("btn_text")
        private String btn_text;

        @JsonProperty("btn_logo")
        private String btn_logo;


    }
}
