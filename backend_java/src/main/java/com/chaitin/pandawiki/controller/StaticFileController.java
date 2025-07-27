package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.config.QiniuConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 静态文件控制器
 * 处理对七牛云存储文件的访问
 *
 * @author chaitin
 */
@RestController
@RequestMapping("/static-file")
@RequiredArgsConstructor
@Slf4j
public class StaticFileController {

    private final QiniuConfig qiniuConfig;

    /**
     * 处理静态文件访问请求
     * 重定向到七牛云存储
     */
    @GetMapping("/**")
    public void getStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 获取完整的请求路径
        String requestURI = request.getRequestURI();

        // 移除 /static-file 前缀，获取文件路径
        String filePath = requestURI.replaceFirst("/static-file/?", "");

        log.info("访问静态文件: {} -> {}", requestURI, filePath);

        if (filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件路径不能为空");
            return;
        }

        // 构建七牛云完整URL
        String qiniuUrl = buildQiniuUrl(filePath);

        log.info("重定向到七牛云: {}", qiniuUrl);

        // 302重定向到七牛云
        response.sendRedirect(qiniuUrl);
    }

    /**
     * 构建七牛云完整URL
     */
    private String buildQiniuUrl(String filePath) {
        String domain = qiniuConfig.getDomain();
        String bucket = qiniuConfig.getBucket();

        // 确保domain以/结尾
        if (!domain.endsWith("/")) {
            domain += "/";
        }

        // 移除开头的/如果存在
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        // 七牛云URL格式：domain + bucket + "/" + filePath
        // filePath 格式：{kbId}/{fileName}
        return domain + "/" + filePath;
    }
}
