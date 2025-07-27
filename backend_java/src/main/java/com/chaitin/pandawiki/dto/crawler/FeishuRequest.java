package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Feishu相关请求
 *
 * @author chaitin
 */
public class FeishuRequest {

    @Data
    @Schema(description = "Feishu基础请求")
    public static class BaseRequest {
        @Schema(description = "用户访问令牌")
        private String userAccessToken;

        @Schema(description = "应用ID")
        private String appId;

        @Schema(description = "应用密钥")
        private String appSecret;
    }

    @Data
    @Schema(description = "获取空间列表请求")
    public static class GetSpaceListRequest extends BaseRequest {
    }

    @Data
    @Schema(description = "搜索Wiki请求")
    public static class SearchWikiRequest extends BaseRequest {
        @Schema(description = "空间ID")
        private String spaceId;

        @Schema(description = "搜索关键词")
        private String query;
    }

    @Data
    @Schema(description = "搜索文档请求")
    public static class SearchDocxRequest extends BaseRequest {
    }
} 