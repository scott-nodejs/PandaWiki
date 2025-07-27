package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 文件处理请求
 *
 * @author chaitin
 */
public class FileProcessRequest {

    @Data
    @Schema(description = "EPUB转换请求")
    public static class EpubConvertRequest {
        @NotEmpty(message = "知识库ID不能为空")
        @Schema(description = "知识库ID")
        private String kbId;
    }

    @Data
    @Schema(description = "WikiJS文件分析请求")
    public static class WikiJSAnalysisRequest {
        @NotEmpty(message = "知识库ID不能为空")
        @Schema(description = "知识库ID")
        private String kbId;
    }

    @Data
    @Schema(description = "Confluence文件分析请求")
    public static class ConfluenceAnalysisRequest {
        @NotEmpty(message = "知识库ID不能为空")
        @Schema(description = "知识库ID")
        private String kbId;
    }
} 