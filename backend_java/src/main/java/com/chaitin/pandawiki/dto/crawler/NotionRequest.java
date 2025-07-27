package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Notion相关请求
 *
 * @author chaitin
 */
public class NotionRequest {

    @Data
    @Schema(description = "Notion获取列表请求")
    public static class GetListRequest {
        @Schema(description = "集成token")
        private String integration;

        @Schema(description = "标题")
        private String captionTitle;
    }

    @Data
    @Schema(description = "Notion获取文档请求")
    public static class GetDocsRequest {
        @NotEmpty(message = "集成token不能为空")
        @Schema(description = "集成token")
        private String integration;

        @Schema(description = "页面列表")
        private List<PageInfo> pages;

        @Data
        @Schema(description = "页面信息")
        public static class PageInfo {
            @Schema(description = "页面ID")
            private String id;

            @Schema(description = "页面标题")
            private String title;
        }
    }
} 