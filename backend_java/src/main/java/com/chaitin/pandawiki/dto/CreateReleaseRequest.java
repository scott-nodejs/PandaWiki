package com.chaitin.pandawiki.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建发布版本请求
 */
@Data
public class CreateReleaseRequest {

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String kb_id;

    /**
     * 版本标签
     */
    @NotBlank(message = "版本标签不能为空")
    private String tag;

    /**
     * 发布信息
     */
    @NotBlank(message = "发布信息不能为空")
    private String message;

    /**
     * 节点ID列表
     */
    @NotEmpty(message = "节点ID列表不能为空")
    private List<String> node_ids;
}
