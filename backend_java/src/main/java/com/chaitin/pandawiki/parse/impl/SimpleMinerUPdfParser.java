package com.chaitin.pandawiki.parse.impl;

import com.chaitin.pandawiki.config.QiniuConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 简化版 MinerU PDF解析器 - 直接使用七牛云URL
 *
 * @author chaitin
 */
@Slf4j
@Component
public class SimpleMinerUPdfParser extends AbstractFileParser {

    private String mineruBaseUrl = "https://mineru.net";

    private String mineruToken = "eyJ0eXBlIjoiSldUIiwiYWxnIjoiSFM1MTIifQ.eyJqdGkiOiI1NTcwNTYzOCIsInJvbCI6IlJPTEVfUkVHSVNURVIiLCJpc3MiOiJPcGVuWExhYiIsImlhdCI6MTc1Mzk0NjU3OCwiY2xpZW50SWQiOiJsa3pkeDU3bnZ5MjJqa3BxOXgydyIsInBob25lIjoiIiwib3BlbklkIjpudWxsLCJ1dWlkIjoiMmRkMGM1NzQtNGE0NS00MTFiLTk4MGMtNDk2YjM4ZGU5YTQ4IiwiZW1haWwiOiIiLCJleHAiOjE3NTUxNTYxNzh9._hE0eRpgD0S02YF-T_0mAK9gODUcafFA1PtrbBjYPZZofcBAXpKx-qqAGmkLMMS8QzvFr8nPT1gKDc7xWjDh-A";

    private boolean mineruEnabled = true;

    private int timeoutSeconds = 600;

    private int pollIntervalSeconds = 5;

    private boolean isOcr = false;

    private boolean enableFormula = true;

    private boolean enableTable = true;

    private String language = "auto";


    private String modelVersion = "v2";

    @Autowired
    private QiniuConfig qiniuConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PdfFileParser fallbackParser = new PdfFileParser();

    @Override
    public List<String> getSupportedExtensions() {
        return Collections.singletonList("pdf");
    }

    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        log.info("=== SimpleMinerU 配置调试 ===");
        log.info("enabled: {}, token: {}, baseUrl: {}",
            mineruEnabled,
            mineruToken != null ? "已配置(" + mineruToken.length() + "字符)" : "未配置",
            mineruBaseUrl);

        // 检查配置
        if (!mineruEnabled || mineruToken == null || mineruToken.trim().isEmpty()) {
            log.info("MinerU未配置，使用回退解析器");
            return fallbackParser.parseToMarkdown(inputStream, fileName);
        }

        // 检查是否为URL（实际使用中，fileName 可能是七牛云URL）
        if (isValidUrl(fileName)) {
            log.info("检测到URL，使用MinerU解析: {}", fileName);
            try {
                return parseWithMinerUUrl(fileName);
            } catch (Exception e) {
                log.warn("MinerU URL解析失败，使用回退: {}", e.getMessage());
                return fallbackParser.parseToMarkdown(inputStream, fileName);
            }
        } else {
            log.info("非URL格式，使用回退解析器: {}", fileName);
            return fallbackParser.parseToMarkdown(inputStream, fileName);
        }
    }

    /**
     * 使用MinerU解析URL
     */
    public String parseWithMinerUUrl(String fileUrl) throws Exception {
        log.info("MinerU解析开始: {}", fileUrl);

        // 1. 创建任务
        String taskId = createTask(fileUrl);
        log.info("任务已创建: {}", taskId);

        // 2. 轮询结果
        String resultUrl = pollResult(taskId);
        log.info("解析完成: {}", resultUrl);

        // 3. 下载结果
        String markdown = downloadResult(resultUrl);
        log.info("结果下载完成，长度: {}", markdown.length());

        return "<!-- MinerU解析 -->\n" + markdown;
    }

    private String createTask(String fileUrl) throws Exception {
        String apiUrl = mineruBaseUrl + "/api/v4/extract/task";

        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
            "url", fileUrl,
            "is_ocr", isOcr,
            "enable_formula", enableFormula,
            "enable_table", enableTable,
            "language", language,
            "model_version", modelVersion
        ));

        HttpURLConnection conn = createConnection(apiUrl, "POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + mineruToken);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);
        JsonNode json = objectMapper.readTree(response);

        if (json.get("code").asInt() != 0) {
            throw new Exception("创建任务失败: " + json.get("msg").asText());
        }

        return json.get("data").get("task_id").asText();
    }

    private String pollResult(String taskId) throws Exception {
        String apiUrl = mineruBaseUrl + "/api/v4/extract/task/" + taskId;
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            HttpURLConnection conn = createConnection(apiUrl, "GET");
            conn.setRequestProperty("Authorization", "Bearer " + mineruToken);

            String response = readResponse(conn);
            JsonNode json = objectMapper.readTree(response);
            JsonNode data = json.get("data");
            String state = data.get("state").asText();

            switch (state) {
                case "done":
                    return data.get("full_zip_url").asText();
                case "failed":
                    throw new Exception("解析失败: " + data.get("err_msg").asText());
                case "running":
                    log.debug("解析中...");
                    break;
                default:
                    log.debug("状态: {}", state);
            }

            TimeUnit.SECONDS.sleep(pollIntervalSeconds);
        }

        throw new Exception("解析超时");
    }

    private String downloadResult(String zipUrl) throws Exception {
        HttpURLConnection conn = createConnection(zipUrl, "GET");

        try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".md") && !entry.isDirectory()) {
                    return readZipContent(zis);
                }
            }
        }

        throw new Exception("未找到Markdown文件");
    }

    private String readZipContent(ZipInputStream zis) throws IOException {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            content.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        return content.toString();
    }

    private HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private boolean isValidUrl(String str) {
        return str != null && (str.startsWith("http://") || str.startsWith("https://"));
    }

    @Override
    public String getParserName() {
        return "简化MinerU解析器";
    }

    @Override
    public int getPriority() {
        return 5; // 比原始MinerU解析器优先级稍低
    }
}
