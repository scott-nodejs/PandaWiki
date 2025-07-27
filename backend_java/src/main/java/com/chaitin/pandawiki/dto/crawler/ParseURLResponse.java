package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 解析URL响应
 *
 * @author chaitin
 */
@Data
@Schema(description = "解析URL响应")
public class ParseURLResponse {

    @Schema(description = "解析的项目列表")
    private List<ParseURLItem> items;

    @Data
    @Schema(description = "解析的URL项目")
    public static class ParseURLItem {
        @Schema(description = "URL地址")
        private String url;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "描述")
        private String desc;

        @Schema(description = "发布时间")
        private String published;
    }
} 