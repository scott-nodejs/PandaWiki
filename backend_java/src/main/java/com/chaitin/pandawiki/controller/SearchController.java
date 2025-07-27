//package com.chaitin.pandawiki.controller;
//
//import com.chaitin.pandawiki.common.Result;
//import com.chaitin.pandawiki.service.SearchService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.apache.shiro.authz.annotation.RequiresAuthentication;
//import org.springframework.web.bind.annotation.*;
//import com.chaitin.pandawiki.entity.Node;
//import java.util.List;
//
///**
// * 搜索控制器
// */
//@Tag(name = "搜索管理")
//@RestController
//@RequestMapping("/api/v1/search")
//@RequiredArgsConstructor
//public class SearchController {
//
//    private final SearchService searchService;
//
//    @Operation(summary = "搜索文档")
//    @GetMapping
//    @RequiresAuthentication
//    public Result<List<Node>> search(
//            @RequestParam String keyword,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size) {
//        return Result.success();
//    }
//
//    @Operation(summary = "获取相似文档")
//    @GetMapping("/similar/{nodeId}")
//    @RequiresAuthentication
//    public Result<List<Node>> getSimilar(
//            @PathVariable String nodeId,
//            @RequestParam(defaultValue = "5") Integer size) {
//        List<Node> results = null;
//        return Result.success(results);
//    }
//
//    @Operation(summary = "重建索引")
//    @PostMapping("/rebuild")
//    @RequiresAuthentication
//    public Result<Void> rebuildIndex() {
//        return Result.success();
//    }
//}
