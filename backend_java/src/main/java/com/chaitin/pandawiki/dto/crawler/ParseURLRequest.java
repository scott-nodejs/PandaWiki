package com.chaitin.pandawiki.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 解析URL请求
 *
 * @author chaitin
 */
@Data
@Schema(description = "解析URL请求")
public class ParseURLRequest {

    @NotEmpty(message = "URL不能为空")
    @Schema(description = "要解析的URL")
    private String url;
} 