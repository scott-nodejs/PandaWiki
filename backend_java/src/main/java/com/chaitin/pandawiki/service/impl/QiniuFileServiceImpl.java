package com.chaitin.pandawiki.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.chaitin.pandawiki.config.QiniuConfig;
import com.chaitin.pandawiki.service.FileService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 七牛云文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QiniuFileServiceImpl implements FileService {

    private final UploadManager uploadManager;
    private final Auth auth;
    private final QiniuConfig qiniuConfig;

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String fileName = IdUtil.fastSimpleUUID() + "." + extension;

        try {
            return uploadFile(file.getInputStream(), fileName, file.getContentType());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String kbId) {
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        String fileName = "static-file/" +(kbId != null ? kbId + "/" : "") + IdUtil.fastSimpleUUID() + "." + extension;

        try {
            uploadFile(file.getInputStream(), fileName, file.getContentType());
            return fileName; // 返回文件key而不是URL
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        try {
            // 获取上传Token
            String upToken = auth.uploadToken(qiniuConfig.getBucket());

            // 上传文件
            Response response = uploadManager.put(inputStream, fileName, upToken, null, contentType);

            if (response.isOK()) {
                return getFileUrl(fileName);
            } else {
                throw new RuntimeException("文件上传失败：" + response.error);
            }
        } catch (QiniuException e) {
            log.error("七牛云文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName;
            if (fileUrl.startsWith("http")) {
                // 如果是完整URL，提取文件名
                String[] parts = fileUrl.split("/");
                fileName = parts[parts.length - 1];
                // URL解码
                fileName = java.net.URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            } else {
                // 如果是文件key，直接使用
                fileName = fileUrl;
            }

            // 创建删除管理器
            com.qiniu.storage.BucketManager bucketManager = new com.qiniu.storage.BucketManager(
                    auth, new com.qiniu.storage.Configuration(com.qiniu.storage.Region.autoRegion()));

            // 删除文件
            Response response = bucketManager.delete(qiniuConfig.getBucket(), fileName);
            if (!response.isOK()) {
                log.warn("文件删除失败：" + response.error);
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败");
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            // 如果配置了自定义域名，使用自定义域名
            if (qiniuConfig.getDomain() != null && !qiniuConfig.getDomain().isEmpty()) {
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                        .replace("+", "%20");

                // 构建文件访问URL
                String baseUrl = qiniuConfig.getDomain();
                if (!baseUrl.startsWith("http")) {
                    baseUrl = "https://" + baseUrl;
                }
                if (!baseUrl.endsWith("/")) {
                    baseUrl += "/";
                }

                String fileUrl = baseUrl + encodedFileName;

                // 生成私有链接（如果需要）
                // 这里假设是公开空间，如果是私有空间需要生成带签名的URL
                // long expireInSeconds = 3600; // 1小时过期
                // return auth.privateDownloadUrl(fileUrl, expireInSeconds);

                return fileUrl;
            } else {
                // 没有配置域名，返回文件key
                return fileName;
            }
        } catch (Exception e) {
            log.error("获取文件访问URL失败", e);
            throw new RuntimeException("获取文件访问URL失败");
        }
    }
}
