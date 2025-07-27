package com.chaitin.pandawiki.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

/**
 * 获取发布列表请求
 */
@Data
public class GetReleaseListRequest {
    
    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String kbId;
    
    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer perPage = 20;
} 