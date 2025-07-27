package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.entity.Model;
import com.chaitin.pandawiki.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型控制器
 */
@Tag(name = "模型管理")
@RestController
@RequestMapping("/api/v1/model")
@RequiredArgsConstructor
public class ModelController {
    
    private final ModelService modelService;
    
    @Operation(summary = "创建模型")
    @PostMapping
    public ApiResponse<Map<String, String>> createModel(@RequestBody Model model) {
        String id = modelService.createModel(model);
        Map<String, String> result = new HashMap<>();
        result.put("id", id);
        return ApiResponse.success(result);
    }
    
    @Operation(summary = "更新模型")
    @PutMapping
    public ApiResponse<Void> updateModel(@RequestParam String id, @RequestBody Model model) {
        model.setId(id);
        modelService.updateModel(model);
        return ApiResponse.success();
    }
    
    @Operation(summary = "删除模型")
    @DeleteMapping
    public ApiResponse<Void> deleteModel(@RequestParam String id) {
        modelService.deleteModel(id);
        return ApiResponse.success();
    }
    
    @Operation(summary = "获取模型详情")
    @GetMapping("/detail")
    public ApiResponse<Model> getModel(@RequestParam String id) {
        Model model = modelService.getById(id);
        return ApiResponse.success(model);
    }
    
    @Operation(summary = "获取模型列表")
    @GetMapping("/list")
    public ApiResponse<List<Model>> getModelList(@RequestParam(required = false) Model.ModelType type) {
        List<Model> models = modelService.getModelList(type);
        return ApiResponse.success(models);
    }
    
    @Operation(summary = "设置模型为活跃状态")
    @PostMapping("/active")
    public ApiResponse<Void> setModelActive(@RequestParam String id) {
        modelService.setModelActive(id);
        return ApiResponse.success();
    }
} 