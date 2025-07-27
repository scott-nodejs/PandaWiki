package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.crawler.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 扩展的爬虫服务接口
 *
 * @author chaitin
 */
public interface CrawlerExtendedService {

    /**
     * 解析RSS
     *
     * @param request 解析请求
     * @return 解析结果
     */
    ParseURLResponse parseRss(ParseURLRequest request);

    /**
     * 解析Sitemap
     *
     * @param request 解析请求
     * @return 解析结果
     */
    ParseURLResponse parseSitemap(ParseURLRequest request);

    /**
     * 获取Notion列表
     *
     * @param request 请求参数
     * @return 页面信息列表
     */
    List<Object> getNotionList(NotionRequest.GetListRequest request);

    /**
     * 获取Notion文档
     *
     * @param request 请求参数
     * @return 页面内容列表
     */
    List<Object> getNotionDocs(NotionRequest.GetDocsRequest request);

    /**
     * EPUB转换
     *
     * @param file 上传的文件
     * @param kbId 知识库ID
     * @return 转换结果
     */
    Object convertEpub(MultipartFile file, String kbId);

    /**
     * 分析WikiJS导出文件
     *
     * @param file 上传的文件
     * @param kbId 知识库ID
     * @return 分析结果
     */
    List<Object> analyzeWikiJSFile(MultipartFile file, String kbId);

    /**
     * 分析Confluence导出文件
     *
     * @param file 上传的文件
     * @param kbId 知识库ID
     * @return 分析结果
     */
    List<Object> analyzeConfluenceFile(MultipartFile file, String kbId);

    /**
     * 获取Feishu空间列表
     *
     * @param request 请求参数
     * @return 空间列表
     */
    List<FeishuResponse.GetSpaceListResponse> getFeishuSpaces(FeishuRequest.GetSpaceListRequest request);

    /**
     * 获取Feishu文档列表
     *
     * @param request 请求参数
     * @return 文档列表
     */
    List<FeishuResponse.SearchDocxResponse> getFeishuDocs(FeishuRequest.SearchDocxRequest request);

    /**
     * 搜索Feishu Wiki
     *
     * @param request 请求参数
     * @return 搜索结果
     */
    List<FeishuResponse.SearchWikiResponse> searchFeishuWiki(FeishuRequest.SearchWikiRequest request);
} 