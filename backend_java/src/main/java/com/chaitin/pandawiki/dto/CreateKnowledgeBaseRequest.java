package com.chaitin.pandawiki.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建知识库请求DTO
 */
@Data
public class CreateKnowledgeBaseRequest {

    private String id;

    private String name;

    private String description;

    private JSONObject access_settings;
}
