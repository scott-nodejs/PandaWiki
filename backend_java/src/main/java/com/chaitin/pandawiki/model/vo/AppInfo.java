package com.chaitin.pandawiki.model.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppInfo {

    private String id;

    private String kbId;

    private String name;

    private Integer type;

    private JSONObject settings;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
