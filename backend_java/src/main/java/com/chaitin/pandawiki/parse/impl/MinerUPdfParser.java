package com.chaitin.pandawiki.parse.impl;

import com.chaitin.pandawiki.service.TempFileStorageService;
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
 * MinerU PDF解析器 - 使用MinerU API进行PDF解析
 *
 * 功能特性：
 * - 优先使用MinerU API进行高质量PDF解析
 * - 支持公式识别、表格解析、OCR等功能
 * - 解析失败时自动回退到PDFBox解析
 * - 异步任务处理和轮询机制
 *
 * @author chaitin
 */
@Slf4j
@Component
public class MinerUPdfParser extends AbstractFileParser {

    // MinerU API配置
    @Value("${mineru.api.base-url:https://mineru.net}")
    private String mineruBaseUrl;

    @Value("${mineru.api.token:}")
    private String mineruToken;

    @Value("${mineru.api.timeout:300}")
    private int timeoutSeconds;

    @Value("${mineru.api.poll-interval:5}")
    private int pollIntervalSeconds;

    @Value("${mineru.api.enabled:true}")
    private boolean mineruEnabled;

    @Value("${mineru.api.is-ocr:true}")
    private boolean isOcr;

    @Value("${mineru.api.enable-formula:true}")
    private boolean enableFormula;

    @Value("${mineru.api.enable-table:true}")
    private boolean enableTable;

    @Value("${mineru.api.language:auto}")
    private String language;

    @Value("${mineru.api.model-version:v2}")
    private String modelVersion;

    @Autowired
    private TempFileStorageService tempFileStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PdfFileParser fallbackParser = new PdfFileParser();

    @Override
    public List<String> getSupportedExtensions() {
        return Collections.singletonList("pdf");
    }

    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        // 添加详细的调试信息
        this.mineruEnabled = true;

        this.mineruToken = "eyJ0eXBlIjoiSldUIiwiYWxnIjoiSFM1MTIifQ.eyJqdGkiOiI1NTcwNTYzOCIsInJvbCI6IlJPTEVfUkVHSVNURVIiLCJpc3MiOiJPcGVuWExhYiIsImlhdCI6MTc1Mzk0NjU3OCwiY2xpZW50SWQiOiJsa3pkeDU3bnZ5MjJqa3BxOXgydyIsInBob25lIjoiIiwib3BlbklkIjpudWxsLCJ1dWlkIjoiMmRkMGM1NzQtNGE0NS00MTFiLTk4MGMtNDk2YjM4ZGU5YTQ4IiwiZW1haWwiOiIiLCJleHAiOjE3NTUxNTYxNzh9._hE0eRpgD0S02YF-T_0mAK9gODUcafFA1PtrbBjYPZZofcBAXpKx-qqAGmkLMMS8QzvFr8nPT1gKDc7xWjDh-A";

        this.mineruBaseUrl = "https://mineru.net";


        // 检查MinerU是否启用和配置
        if (!mineruEnabled || mineruToken == null || mineruToken.trim().isEmpty()) {
            return fallbackParser.parseToMarkdown(inputStream, fileName);
        }

        // 检查临时文件存储服务是否可用
//        if (!tempFileStorageService.isAvailable()) {
//            log.warn("临时文件存储服务不可用，使用回退解析器: {}", fileName);
//            return fallbackParser.parseToMarkdown(inputStream, fileName);
//        }

        String fileUrl = null;
        try {
            // 标记流，以便回退时可以重置
            if (inputStream.markSupported()) {
                inputStream.mark(Integer.MAX_VALUE);
            } else {
                // 如果流不支持mark，先读取到字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = baos.toByteArray();
                inputStream = new ByteArrayInputStream(fileBytes);
                inputStream.mark(Integer.MAX_VALUE);
            }

            // 优先使用MinerU解析
            return parseWithMinerU(inputStream, fileName);

        } catch (Exception e) {
            log.warn("MinerU解析失败，使用回退解析器: {} - {}", fileName, e.getMessage());

            // 清理可能创建的临时文件
            if (fileUrl != null) {
                try {
                    tempFileStorageService.deleteFile(fileUrl);
                } catch (Exception cleanupException) {
                    log.warn("清理临时文件失败: {}", fileUrl, cleanupException);
                }
            }

            // 重置流位置并使用回退解析器
            try {
                inputStream.reset();
            } catch (IOException resetException) {
                log.warn("重置输入流失败", resetException);
                throw new Exception("MinerU解析失败且无法重置流进行回退解析: " + e.getMessage());
            }

            return fallbackParser.parseToMarkdown(inputStream, fileName);
        }
    }

    /**
     * 使用MinerU API解析PDF
     */
    private String parseWithMinerU(InputStream inputStream, String fileName) throws Exception {
        log.info("开始使用MinerU解析PDF文件: {}", fileName);

        String fileUrl = null;
        try {
            // 上传文件到临时存储
            fileUrl = tempFileStorageService.uploadFile(inputStream, fileName, "application/pdf");
            log.info("文件上传到临时存储: {}", fileUrl);

            // 创建解析任务
            String taskId = createMinerUTask(fileUrl, fileName);
            log.info("MinerU解析任务已创建: {}", taskId);

            // 轮询获取结果
            String resultZipUrl = pollTaskResult(taskId);
            log.info("MinerU解析完成，结果URL: {}", resultZipUrl);

            // 下载并解析结果
            String markdown = downloadAndParseResult(resultZipUrl, fileName);
            log.info("MinerU解析成功，内容长度: {}", markdown.length());

            return addMinerUMetadata(markdown, fileName);

        } finally {
            // 清理临时文件
            if (fileUrl != null) {
                try {
                    tempFileStorageService.deleteFile(fileUrl);
                    log.debug("临时文件已清理: {}", fileUrl);
                } catch (Exception e) {
                    log.warn("清理临时文件失败: {}", fileUrl, e);
                }
            }
        }
    }

    /**
     * 添加MinerU解析元数据
     */
    private String addMinerUMetadata(String markdown, String fileName) {
        StringBuilder result = new StringBuilder();

        // 添加解析器信息
        result.append("<!-- 解析器: MinerU API -->\n");
        result.append("<!-- 解析时间: ").append(new java.util.Date()).append(" -->\n");
        result.append("<!-- 原文件名: ").append(fileName).append(" -->\n\n");

        // 添加原始内容
        result.append(markdown);

        return result.toString();
    }

    /**
     * 创建MinerU解析任务
     */
    private String createMinerUTask(String fileUrl, String fileName) throws Exception {
        String apiUrl = mineruBaseUrl + "/api/v4/extract/task";

        // 构建请求体
        String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
            "url", fileUrl,
            "is_ocr", isOcr,
            "enable_formula", enableFormula,
            "enable_table", enableTable,
            "language", language,
            "model_version", modelVersion
        ));

        log.debug("MinerU请求体: {}", requestBody);

        // 发送请求
        HttpURLConnection conn = createConnection(apiUrl, "POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + mineruToken);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // 获取响应
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        if (responseCode != 200) {
            throw new Exception("创建MinerU任务失败 [" + responseCode + "]: " + response);
        }

        // 解析响应获取任务ID
        JsonNode jsonResponse = objectMapper.readTree(response);
        if (jsonResponse.get("code").asInt() != 0) {
            String errorMsg = jsonResponse.has("msg") ? jsonResponse.get("msg").asText() : "未知错误";
            throw new Exception("MinerU API错误: " + errorMsg);
        }

        return jsonResponse.get("data").get("task_id").asText();
    }

    /**
     * 轮询任务结果
     */
    private String pollTaskResult(String taskId) throws Exception {
        String apiUrl = mineruBaseUrl + "/api/v4/extract/task/" + taskId;
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        log.info("开始轮询MinerU任务结果，任务ID: {}, 超时时间: {}秒", taskId, timeoutSeconds);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            HttpURLConnection conn = createConnection(apiUrl, "GET");
            conn.setRequestProperty("Authorization", "Bearer " + mineruToken);

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode != 200) {
                throw new Exception("查询MinerU任务状态失败 [" + responseCode + "]: " + response);
            }

            JsonNode jsonResponse = objectMapper.readTree(response);
            if (jsonResponse.get("code").asInt() != 0) {
                String errorMsg = jsonResponse.has("msg") ? jsonResponse.get("msg").asText() : "未知错误";
                throw new Exception("MinerU API错误: " + errorMsg);
            }

            JsonNode data = jsonResponse.get("data");
            String state = data.get("state").asText();

            log.debug("MinerU任务状态: {}", state);

            switch (state) {
                case "done":
                    if (data.has("full_zip_url")) {
                        return data.get("full_zip_url").asText();
                    } else {
                        throw new Exception("任务完成但未返回结果URL");
                    }
                case "failed":
                    String errMsg = data.has("err_msg") ? data.get("err_msg").asText() : "未知错误";
                    throw new Exception("MinerU解析失败: " + errMsg);
                case "running":
                    if (data.has("extract_progress")) {
                        JsonNode progress = data.get("extract_progress");
                        int extracted = progress.has("extracted_pages") ? progress.get("extracted_pages").asInt() : 0;
                        int total = progress.has("total_pages") ? progress.get("total_pages").asInt() : 0;
                        log.info("MinerU解析进度: {}/{}", extracted, total);
                    }
                    break;
                case "pending":
                    log.debug("MinerU任务排队中...");
                    break;
                case "converting":
                    log.debug("MinerU格式转换中...");
                    break;
                default:
                    log.warn("未知的任务状态: {}", state);
                    break;
            }

            // 等待一段时间后继续轮询
            try {
                TimeUnit.SECONDS.sleep(pollIntervalSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("轮询被中断");
            }
        }

        throw new Exception("MinerU解析超时（" + timeoutSeconds + "秒）");
    }

    /**
     * 下载并解析结果文件
     */
    private String downloadAndParseResult(String zipUrl, String fileName) throws Exception {
        log.info("下载MinerU解析结果: {}", zipUrl);

        HttpURLConnection conn = createConnection(zipUrl, "GET");
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("下载解析结果失败 [" + responseCode + "]");
        }

        // 解析ZIP文件内容
        try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 查找markdown文件（优先查找主要的markdown文件）
                if (entryName.endsWith(".md") && !entry.isDirectory()) {
                    log.info("找到Markdown文件: {}", entryName);
                    String content = readZipEntryContent(zis);
                    if (content != null && !content.trim().isEmpty()) {
                        return content;
                    }
                }
            }
        }

        throw new Exception("在解析结果中未找到有效的Markdown文件");
    }

    /**
     * 读取ZIP条目内容
     */
    private String readZipEntryContent(ZipInputStream zis) throws IOException {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[8192];
        int len;

        while ((len = zis.read(buffer)) > 0) {
            content.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }

        return content.toString();
    }

    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(30000); // 30秒连接超时
        conn.setReadTimeout(60000);    // 60秒读取超时
        conn.setRequestProperty("User-Agent", "PandaWiki-MinerU-Client/1.0");
        return conn;
    }

    /**
     * 读取HTTP响应
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream = conn.getResponseCode() >= 400 ?
            conn.getErrorStream() : conn.getInputStream();

        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    @Override
    public String getParserName() {
        return "MinerU智能PDF解析器";
    }

    @Override
    public int getPriority() {
        return 10; // 设置更高的优先级，确保优先使用
    }
}
