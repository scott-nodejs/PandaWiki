package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Feishu相关响应
 *
 * @author chaitin
 */
public class FeishuResponse {

    @Data
    @Schema(description = "空间列表响应")
    public static class GetSpaceListResponse {
        @Schema(description = "空间名称")
        private String name;

        @Schema(description = "空间ID")
        private String spaceId;
    }

    @Data
    @Schema(description = "Wiki搜索响应")
    public static class SearchWikiResponse {
        @Schema(description = "标题")
        private String title;

        @Schema(description = "URL")
        private String url;

        @Schema(description = "空间ID")
        private String spaceId;

        @Schema(description = "对象token")
        private String objToken;

        @Schema(description = "对象类型")
        private Integer objType;
    }

    @Data
    @Schema(description = "文档搜索响应")
    public static class SearchDocxResponse {
        @Schema(description = "文档名称")
        private String name;

        @Schema(description = "URL")
        private String url;

        @Schema(description = "对象token")
        private String objToken;

        @Schema(description = "对象类型")
        private Integer objType;
    }
} 