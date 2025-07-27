package com.chaitin.pandawiki.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class KnowledgeBaseVo {

    private String id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 数据集ID
     */
    private String datasetId;

    /**
     * 访问设置 (JSON字符串存储)
     */
    private JSONObject access_settings;
}
