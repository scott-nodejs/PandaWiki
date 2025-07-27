package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.CreateKnowledgeBaseRequest;
import com.chaitin.pandawiki.dto.CreateReleaseRequest;
import com.chaitin.pandawiki.dto.GetReleaseListRequest;
import com.chaitin.pandawiki.dto.ReleaseListResponse;
import com.chaitin.pandawiki.entity.KnowledgeBase;
import com.chaitin.pandawiki.model.vo.KnowledgeBaseVo;
import com.chaitin.pandawiki.service.KnowledgeBaseService;
import com.chaitin.pandawiki.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 */
@Tag(name = "知识库管理", description = "知识库管理相关接口")
@RestController
@RequestMapping("/api/v1/knowledge_base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final ReleaseService releaseService;

    /**
     * 创建知识库
     */
    @Operation(summary = "创建知识库")
    @PostMapping("")
    public ApiResponse<Map<String, String>> createKnowledgeBase(@Validated @RequestBody CreateKnowledgeBaseRequest request) {
        String id = knowledgeBaseService.createKnowledgeBase(request);
        Map<String, String> result = new HashMap<>();
        result.put("id", id);
        return ApiResponse.success(result);
    }

    /**
     * 获取知识库列表
     */
    @Operation(summary = "获取知识库列表")
    @GetMapping("/list")
    public ApiResponse<List<KnowledgeBase>> getKnowledgeBaseList() {
        List<KnowledgeBase> list = knowledgeBaseService.list();
        return ApiResponse.success(list);
    }

    /**
     * 获取知识库详情
     */
    @Operation(summary = "获取知识库详情")
    @GetMapping("/detail")
    public ApiResponse<KnowledgeBaseVo> getKnowledgeBaseDetail(@RequestParam String id) {
        KnowledgeBaseVo knowledgeBase = knowledgeBaseService.getKnowledgeBaseDetail(id);
        return ApiResponse.success(knowledgeBase);
    }

    /**
     * 更新知识库
     */
    @Operation(summary = "更新知识库")
    @PutMapping("/detail")
    public ApiResponse<Void> updateKnowledgeBase(@RequestBody CreateKnowledgeBaseRequest request) {
        knowledgeBaseService.updateKnowledgeBase(request);
        return ApiResponse.success();
    }

    /**
     * 删除知识库
     */
    @Operation(summary = "删除知识库")
    @DeleteMapping("/detail")
    public ApiResponse<Void> deleteKnowledgeBase(@RequestParam String id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return ApiResponse.success();
    }

    /**
     * 发布知识库
     */
    @Operation(summary = "发布知识库")
    @PostMapping("/release")
    public ApiResponse<Map<String, String>> createRelease(@Validated @RequestBody CreateReleaseRequest request) {
        String id = releaseService.createRelease(request);
        Map<String, String> result = new HashMap<>();
        result.put("id", id);
        return ApiResponse.success(result);
    }

    /**
     * 获取发布列表
     */
    @Operation(summary = "获取发布列表")
    @GetMapping("/release/list")
    public ApiResponse<ReleaseListResponse> getReleaseList(
            @RequestParam String kb_id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer per_page) {
        GetReleaseListRequest request = new GetReleaseListRequest();
        request.setKbId(kb_id);
        request.setPage(page);
        request.setPerPage(per_page);

        ReleaseListResponse response = releaseService.getReleaseList(request);
        return ApiResponse.success(response);
    }
}
