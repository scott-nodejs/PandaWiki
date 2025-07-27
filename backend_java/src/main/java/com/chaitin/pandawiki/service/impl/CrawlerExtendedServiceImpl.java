package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.crawler.*;
import com.chaitin.pandawiki.service.CrawlerExtendedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 扩展的爬虫服务实现
 *
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerExtendedServiceImpl implements CrawlerExtendedService {

    private final RestTemplate restTemplate;
    private final CrawlerConfig crawlerConfig;

    @Override
    public ParseURLResponse parseRss(ParseURLRequest request) {
        log.info("解析RSS: {}", request.getUrl());

        try {
            String url = crawlerConfig.getServiceUrl() + "/parse_rss";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ParseURLRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ParseURLResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ParseURLResponse.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("解析RSS失败", e);
            throw new RuntimeException("解析RSS失败: " + e.getMessage());
        }
    }

    @Override
    public ParseURLResponse parseSitemap(ParseURLRequest request) {
        log.info("解析Sitemap: {}", request.getUrl());

        try {
            String url = crawlerConfig.getServiceUrl() + "/parse_sitemap";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ParseURLRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ParseURLResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ParseURLResponse.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("解析Sitemap失败", e);
            throw new RuntimeException("解析Sitemap失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> getNotionList(NotionRequest.GetListRequest request) {
        log.info("获取Notion列表: integration={}", request.getIntegration());

        try {
            String url = crawlerConfig.getServiceUrl() + "/notion/get_list";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotionRequest.GetListRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("获取Notion列表失败", e);
            throw new RuntimeException("获取Notion列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> getNotionDocs(NotionRequest.GetDocsRequest request) {
        log.info("获取Notion文档: integration={}", request.getIntegration());

        try {
            String url = crawlerConfig.getServiceUrl() + "/notion/get_doc";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotionRequest.GetDocsRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("获取Notion文档失败", e);
            throw new RuntimeException("获取Notion文档失败: " + e.getMessage());
        }
    }

    @Override
    public Object convertEpub(MultipartFile file, String kbId) {
        log.info("转换EPUB文件: filename={}, kbId={}", file.getOriginalFilename(), kbId);

        try {
            String url = crawlerConfig.getServiceUrl() + "/epub/convert";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("kb_id", kbId);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Object.class);

            return response.getBody();
        } catch (IOException e) {
            log.error("读取EPUB文件失败", e);
            throw new RuntimeException("读取EPUB文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("转换EPUB文件失败", e);
            throw new RuntimeException("转换EPUB文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> analyzeWikiJSFile(MultipartFile file, String kbId) {
        log.info("分析WikiJS文件: filename={}, kbId={}", file.getOriginalFilename(), kbId);

        try {
            String url = crawlerConfig.getServiceUrl() + "/wikijs/analysis_export_file";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("kb_id", kbId);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return response.getBody();
        } catch (IOException e) {
            log.error("读取WikiJS文件失败", e);
            throw new RuntimeException("读取WikiJS文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("分析WikiJS文件失败", e);
            throw new RuntimeException("分析WikiJS文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<Object> analyzeConfluenceFile(MultipartFile file, String kbId) {
        log.info("分析Confluence文件: filename={}, kbId={}", file.getOriginalFilename(), kbId);

        try {
            String url = crawlerConfig.getServiceUrl() + "/confluence/analysis_export_file";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("kb_id", kbId);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return response.getBody();
        } catch (IOException e) {
            log.error("读取Confluence文件失败", e);
            throw new RuntimeException("读取Confluence文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("分析Confluence文件失败", e);
            throw new RuntimeException("分析Confluence文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<FeishuResponse.GetSpaceListResponse> getFeishuSpaces(FeishuRequest.GetSpaceListRequest request) {
        log.info("获取Feishu空间列表: appId={}", request.getAppId());

        try {
            String url = crawlerConfig.getServiceUrl() + "/feishu/list_spaces";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FeishuRequest.GetSpaceListRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return (List<FeishuResponse.GetSpaceListResponse>) response.getBody();
        } catch (Exception e) {
            log.error("获取Feishu空间列表失败", e);
            throw new RuntimeException("获取Feishu空间列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<FeishuResponse.SearchDocxResponse> getFeishuDocs(FeishuRequest.SearchDocxRequest request) {
        log.info("获取Feishu文档列表: appId={}", request.getAppId());

        try {
            String url = crawlerConfig.getServiceUrl() + "/feishu/list_doc";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FeishuRequest.SearchDocxRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return (List<FeishuResponse.SearchDocxResponse>) response.getBody();
        } catch (Exception e) {
            log.error("获取Feishu文档列表失败", e);
            throw new RuntimeException("获取Feishu文档列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<FeishuResponse.SearchWikiResponse> searchFeishuWiki(FeishuRequest.SearchWikiRequest request) {
        log.info("搜索Feishu Wiki: spaceId={}, query={}", request.getSpaceId(), request.getQuery());

        try {
            String url = crawlerConfig.getServiceUrl() + "/feishu/search_wiki";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FeishuRequest.SearchWikiRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, List.class);

            return (List<FeishuResponse.SearchWikiResponse>) response.getBody();
        } catch (Exception e) {
            log.error("搜索Feishu Wiki失败", e);
            throw new RuntimeException("搜索Feishu Wiki失败: " + e.getMessage());
        }
    }
} 