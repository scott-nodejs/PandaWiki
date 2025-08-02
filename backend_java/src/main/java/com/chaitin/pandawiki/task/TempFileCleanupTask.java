package com.chaitin.pandawiki.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 临时文件清理定时任务
 * 定期清理过期的临时文件，释放磁盘空间
 * 
 * @author chaitin
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "temp.storage.local.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class TempFileCleanupTask {
    
    @Value("${temp.storage.local.base-path:${java.io.tmpdir}/pandawiki-temp}")
    private String basePath;
    
    @Value("${temp.storage.local.cleanup.retention-hours:24}")
    private long retentionHours;
    
    @Value("${temp.storage.local.enabled:true}")
    private boolean storageEnabled;
    
    @PostConstruct
    public void init() {
        if (storageEnabled) {
            log.info("临时文件清理任务已启用，保留时间: {}小时，存储路径: {}", retentionHours, basePath);
        } else {
            log.info("临时文件存储服务未启用，清理任务将不执行");
        }
    }
    
    /**
     * 定时清理过期文件
     * 每小时执行一次
     */
    @Scheduled(fixedRateString = "${temp.storage.local.cleanup.interval-minutes:60}000")
    public void cleanupExpiredFiles() {
        if (!storageEnabled) {
            return;
        }
        
        try {
            Path storageDir = Paths.get(basePath);
            
            // 检查存储目录是否存在
            if (!Files.exists(storageDir)) {
                log.debug("临时存储目录不存在，跳过清理: {}", storageDir);
                return;
            }
            
            if (!Files.isDirectory(storageDir)) {
                log.warn("临时存储路径不是目录: {}", storageDir);
                return;
            }
            
            // 计算过期时间点
            Instant cutoffTime = Instant.now().minus(retentionHours, ChronoUnit.HOURS);
            AtomicInteger deletedCount = new AtomicInteger(0);
            AtomicInteger totalSize = new AtomicInteger(0);
            
            log.debug("开始清理临时文件，过期时间点: {}", cutoffTime);
            
            // 遍历并删除过期文件
            Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        // 检查文件修改时间
                        Instant fileTime = attrs.lastModifiedTime().toInstant();
                        
                        if (fileTime.isBefore(cutoffTime)) {
                            long fileSize = attrs.size();
                            Files.delete(file);
                            deletedCount.incrementAndGet();
                            totalSize.addAndGet((int) fileSize);
                            log.debug("删除过期临时文件: {} (大小: {} bytes, 修改时间: {})", 
                                file.getFileName(), fileSize, fileTime);
                        }
                    } catch (IOException e) {
                        log.warn("删除临时文件失败: {}", file, e);
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.warn("访问文件失败: {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        // 尝试删除空目录（除了根目录）
                        if (!dir.equals(storageDir) && isDirectoryEmpty(dir)) {
                            Files.delete(dir);
                            log.debug("删除空目录: {}", dir);
                        }
                    } catch (IOException e) {
                        log.debug("删除目录失败（可能不为空）: {}", dir);
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
            
            if (deletedCount.get() > 0) {
                log.info("临时文件清理完成，删除 {} 个文件，释放 {} KB 空间", 
                    deletedCount.get(), totalSize.get() / 1024);
            } else {
                log.debug("临时文件清理完成，没有过期文件需要删除");
            }
            
        } catch (Exception e) {
            log.error("临时文件清理任务执行失败", e);
        }
    }
    
    /**
     * 检查目录是否为空
     */
    private boolean isDirectoryEmpty(Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            return !stream.iterator().hasNext();
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 手动触发清理任务（用于测试或管理接口）
     */
    public void triggerCleanup() {
        log.info("手动触发临时文件清理任务");
        cleanupExpiredFiles();
    }
    
    /**
     * 获取存储目录统计信息
     */
    public StorageStats getStorageStats() {
        if (!storageEnabled) {
            return new StorageStats(0, 0, 0);
        }
        
        try {
            Path storageDir = Paths.get(basePath);
            
            if (!Files.exists(storageDir)) {
                return new StorageStats(0, 0, 0);
            }
            
            AtomicInteger fileCount = new AtomicInteger(0);
            AtomicInteger totalSize = new AtomicInteger(0);
            Instant cutoffTime = Instant.now().minus(retentionHours, ChronoUnit.HOURS);
            AtomicInteger expiredCount = new AtomicInteger(0);
            
            Files.walkFileTree(storageDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fileCount.incrementAndGet();
                    totalSize.addAndGet((int) attrs.size());
                    
                    if (attrs.lastModifiedTime().toInstant().isBefore(cutoffTime)) {
                        expiredCount.incrementAndGet();
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
            
            return new StorageStats(fileCount.get(), totalSize.get(), expiredCount.get());
            
        } catch (IOException e) {
            log.warn("获取存储统计信息失败", e);
            return new StorageStats(0, 0, 0);
        }
    }
    
    /**
     * 存储统计信息
     */
    public static class StorageStats {
        public final int fileCount;
        public final int totalSize;
        public final int expiredCount;
        
        public StorageStats(int fileCount, int totalSize, int expiredCount) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.expiredCount = expiredCount;
        }
        
        @Override
        public String toString() {
            return String.format("StorageStats{files=%d, size=%dKB, expired=%d}", 
                fileCount, totalSize / 1024, expiredCount);
        }
    }
} 