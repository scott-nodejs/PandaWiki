package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布版本实体
 */
@Data
@TableName("release_version")
public class Release {

    /**
     * 主键ID
     */
    @TableId
    private String id;

    /**
     * 知识库ID
     */
    private String kbId;

    /**
     * 版本标签
     */
    private String tag;

    /**
     * 发布信息
     */
    private String message;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发布状态 1:已发布 0:已撤回
     */
    private Integer status;
}
