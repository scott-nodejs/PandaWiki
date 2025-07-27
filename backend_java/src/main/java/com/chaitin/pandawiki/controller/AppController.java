package com.chaitin.pandawiki.controller;

import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.entity.App;
import com.chaitin.pandawiki.model.vo.AppInfo;
import com.chaitin.pandawiki.model.vo.AppInfoVO;
import com.chaitin.pandawiki.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用控制器
 */
@Tag(name = "应用管理")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @Operation(summary = "创建应用")
    @PostMapping
    public ApiResponse<Map<String, String>> createApp(@RequestBody App app) {
        String id = appService.createApp(app);
        Map<String, String> result = new HashMap<>();
        result.put("id", id);
        return ApiResponse.success(result);
    }

    @Operation(summary = "更新应用")
    @PutMapping
    public ApiResponse<Void> updateApp(@RequestParam String id, @RequestBody JSONObject setting) {
        appService.updateSetting(id, setting);
        return ApiResponse.success();
    }

    @Operation(summary = "删除应用")
    @DeleteMapping
    public ApiResponse<Void> deleteApp(@RequestParam String id) {
        appService.deleteApp(id);
        return ApiResponse.success();
    }

    @Operation(summary = "获取应用详情")
    @GetMapping("/detail")
    public ApiResponse<AppInfo> getApp(@RequestParam String kb_id, Integer type) {
        LambdaQueryWrapper<App> queryWrapper = Wrappers.<App>lambdaQuery().eq(App::getKbId, kb_id)
                .eq(App::getType, type);
        App app = appService.getOne(queryWrapper);
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(app, appInfo);
        JSONObject jsonObject = JSONObject.parseObject(app.getSettings());
        appInfo.setSettings(jsonObject);
        return ApiResponse.success(appInfo);
    }

    @Operation(summary = "获取知识库下的应用列表")
    @GetMapping
    public ApiResponse<List<App>> getAppList(
            @RequestParam String kbId,
            @RequestParam(required = false) Integer type) {
        List<App> apps = type != null ?
                appService.getAppListByType(kbId, type) :
                appService.getAppList(kbId);
        return ApiResponse.success(apps);
    }
}
