package com.chaitin.pandawiki.parse;

import com.chaitin.pandawiki.parse.FileParserFactory;
import com.chaitin.pandawiki.parse.impl.MinerUPdfParser;
import com.chaitin.pandawiki.parse.spi.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MinerU PDF解析器测试
 * 验证 MinerU 解析器是否正确注册和配置
 * 
 * @author chaitin
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class MinerUPdfParserTest {
    
    @Autowired
    private FileParserFactory fileParserFactory;
    
    @Autowired(required = false)
    private MinerUPdfParser minerUPdfParser;
    
    @Test
    public void testMinerUPdfParserRegistration() {
        log.info("=== 测试 MinerU PDF 解析器注册 ===");
        
        // 1. 验证 MinerUPdfParser Bean 是否存在
        assertNotNull(minerUPdfParser, "MinerUPdfParser Bean 应该存在");
        log.info("✅ MinerUPdfParser Bean 已注册");
        
        // 2. 验证解析器名称
        assertEquals("MinerU智能PDF解析器", minerUPdfParser.getParserName());
        log.info("✅ 解析器名称正确: {}", minerUPdfParser.getParserName());
        
        // 3. 验证优先级
        assertEquals(10, minerUPdfParser.getPriority());
        log.info("✅ 解析器优先级正确: {}", minerUPdfParser.getPriority());
        
        // 4. 验证支持的文件扩展名
        assertTrue(minerUPdfParser.getSupportedExtensions().contains("pdf"));
        log.info("✅ 支持 PDF 文件扩展名");
    }
    
    @Test
    public void testFileParserFactoryIntegration() {
        log.info("=== 测试文件解析器工厂集成 ===");
        
        // 1. 验证 PDF 文件支持
        assertTrue(fileParserFactory.isSupported("test.pdf"));
        log.info("✅ FileParserFactory 支持 PDF 文件");
        
        // 2. 获取 PDF 解析器并验证类型
        try {
            FileParser parser = fileParserFactory.getParser("test.pdf");
            assertNotNull(parser, "应该能获取到 PDF 解析器");
            
            log.info("📋 获取到的解析器: {} (优先级: {})", 
                parser.getParserName(), parser.getPriority());
            
            // 3. 验证是否是 MinerU 解析器（因为优先级更高）
            if (parser instanceof MinerUPdfParser) {
                log.info("🎯 正在使用 MinerU 解析器（高质量解析）");
            } else {
                log.info("📄 正在使用传统 PDF 解析器（回退方案）");
            }
            
        } catch (Exception e) {
            fail("获取 PDF 解析器失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testMinerUConfiguration() {
        log.info("=== 测试 MinerU 配置 ===");
        
        // 注意：在测试环境中，某些配置可能不可用
        // 这里主要验证解析器对象的基本属性
        
        assertNotNull(minerUPdfParser);
        
        // 验证解析器的基本方法不会抛出异常
        assertDoesNotThrow(() -> {
            minerUPdfParser.getParserName();
            minerUPdfParser.getPriority();
            minerUPdfParser.getSupportedExtensions();
        });
        
        log.info("✅ MinerU 解析器基本配置正常");
    }
} 