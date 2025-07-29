package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.CrawlerServiceResponse;
import com.chaitin.pandawiki.dto.ScrapeRequest;
import com.chaitin.pandawiki.dto.ScrapeResponse;
import com.chaitin.pandawiki.parse.FileParserFactory;
import com.chaitin.pandawiki.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫服务实现类
 *
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerServiceImpl implements CrawlerService {

    private final RestTemplate restTemplate;
    private final CrawlerConfig crawlerConfig;
    private final FileParserFactory fileParserFactory;

    @Override
    public ScrapeResponse scrapeUrl(ScrapeRequest request) {
        try {
            log.info("开始抓取URL: {}, kbId: {}", request.getUrl(), request.getKbId());

            String targetUrl = request.getUrl();

            // 处理上传文件的URL，参考Go服务的逻辑
            if (targetUrl.startsWith("/static-file")) {
                targetUrl = crawlerConfig.getStaticUrl() + targetUrl.replace("/static-file/static-file", "/static-file");
                log.info("转换static-file路径: {} -> {}", request.getUrl(), targetUrl);
                log.info("检测到静态文件，开始解析: {}", targetUrl);
                return parseStaticFile(targetUrl, request.getKbId());
            }

            // 构建请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("url", targetUrl);
            requestBody.put("kb_id", request.getKbId());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // 构建完整的API URL，添加/scrape路径
            String apiUrl = crawlerConfig.getServiceUrl() + "/scrape";
            log.info("调用爬虫服务: {}", apiUrl);

            // 调用爬虫服务
            ResponseEntity<CrawlerServiceResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                CrawlerServiceResponse.class
            );

            CrawlerServiceResponse crawlerResponse = response.getBody();

            if (crawlerResponse == null) {
                throw new RuntimeException("爬虫服务返回空响应");
            }

            if (crawlerResponse.getErr() != 0) {
                throw new RuntimeException("爬虫服务返回错误: " + crawlerResponse.getMsg());
            }

            if (crawlerResponse.getData() == null) {
                throw new RuntimeException("爬虫服务返回数据为空");
            }

            ScrapeResponse result = new ScrapeResponse();
            result.setTitle(crawlerResponse.getData().getTitle());
            result.setContent(crawlerResponse.getData().getMarkdown());

            log.info("成功抓取URL: {}, 标题: {}", request.getUrl(), result.getTitle());

            return result;

        } catch (Exception e) {
            log.error("抓取URL失败: {}, 错误: {}", request.getUrl(), e.getMessage(), e);
            throw new RuntimeException("抓取URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否是静态文件
     *
     * @param url 文件URL
     * @return true如果是支持的静态文件格式
     */
    private boolean isStaticFile(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 从URL中提取文件名
        String fileName = extractFileNameFromUrl(url);
        return fileParserFactory.isSupported(fileName);
    }

    /**
     * 从URL中提取文件名
     *
     * @param url URL地址
     * @return 文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            // 移除查询参数
            int queryIndex = url.indexOf('?');
            if (queryIndex != -1) {
                url = url.substring(0, queryIndex);
            }

            // 移除锚点
            int anchorIndex = url.indexOf('#');
            if (anchorIndex != -1) {
                url = url.substring(0, anchorIndex);
            }

            // 提取最后一段路径作为文件名
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
                return url.substring(lastSlashIndex + 1);
            }

            return url;
        } catch (Exception e) {
            log.warn("提取文件名失败: {}", e.getMessage());
            return url;
        }
    }

    /**
     * 解析静态文件
     *
     * @param fileUrl 文件URL
     * @param kbId 知识库ID
     * @return 解析结果
     */
    private ScrapeResponse parseStaticFile(String fileUrl, String kbId) {
        try {
            // 下载文件内容
            log.info("开始下载文件: {}", fileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                fileUrl,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("文件下载失败，响应内容为空");
            }

            byte[] fileContent = response.getBody();
            log.info("文件下载完成，大小: {} bytes", fileContent.length);

            // 解析文件
            String fileName = extractFileNameFromUrl(fileUrl);
            String markdownContent;

            try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
                markdownContent = fileParserFactory.parseToMarkdown(inputStream, fileName);
                log.info("文件解析完成: {}, 内容长度: {}", fileName, markdownContent.length());
            }

            // 构建响应
            ScrapeResponse result = new ScrapeResponse();

            // 设置标题（从文件名生成）
            String title = generateTitleFromFileName(fileName);
            result.setTitle(title);

            // 设置内容
            result.setContent(markdownContent);

            log.info("静态文件解析完成: {}", fileName);
            return result;

        } catch (Exception e) {
            log.error("解析静态文件失败: {}, 错误: {}", fileUrl, e.getMessage(), e);

            // 返回错误信息作为内容
            ScrapeResponse errorResult = new ScrapeResponse();
            errorResult.setTitle("文件解析失败");
            errorResult.setContent(createErrorMarkdown(fileUrl, e.getMessage()));

            return errorResult;
        }
    }

    /**
     * 从文件名生成标题
     *
     * @param fileName 文件名
     * @return 生成的标题
     */
    private String generateTitleFromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "未知文件";
        }

        // 移除扩展名
        int lastDotIndex = fileName.lastIndexOf('.');
        String nameWithoutExt = lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;

        // 替换常见的分隔符为空格
        return nameWithoutExt.replaceAll("[_-]+", " ").trim();
    }

    /**
     * 创建错误信息的Markdown
     *
     * @param fileUrl 文件URL
     * @param errorMessage 错误信息
     * @return 错误Markdown内容
     */
    private String createErrorMarkdown(String fileUrl, String errorMessage) {
        StringBuilder markdown = new StringBuilder();

        markdown.append("# 文件解析失败\n\n");
        markdown.append("**文件URL**: ").append(fileUrl).append("\n\n");
        markdown.append("**错误信息**: ").append(errorMessage).append("\n\n");
        markdown.append("---\n\n");
        markdown.append("## 可能的解决方案\n\n");
        markdown.append("1. 检查文件URL是否可访问\n");
        markdown.append("2. 确认文件格式是否受支持\n");
        markdown.append("3. 检查相关解析器依赖是否已安装\n");
        markdown.append("4. 查看日志获取更多详细信息\n\n");

        markdown.append("## 支持的文件格式\n\n");
        for (String ext : fileParserFactory.getSupportedExtensions()) {
            markdown.append("- .").append(ext).append("\n");
        }

        return markdown.toString();
    }
}
