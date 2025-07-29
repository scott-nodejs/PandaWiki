package com.chaitin.pandawiki.parse.impl;

import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML文件解析器
 * 
 * @author chaitin
 */
public class HtmlFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("html", "htm", "xhtml");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        String htmlContent = readInputStream(inputStream);
        
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        // 转换HTML为Markdown
        String convertedContent = htmlToMarkdown(htmlContent);
        markdown.append(convertedContent);
        
        return cleanText(markdown.toString());
    }
    
    /**
     * 将HTML内容转换为Markdown格式
     * 
     * @param html HTML内容
     * @return Markdown内容
     */
    private String htmlToMarkdown(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        
        String content = html;
        
        // 移除HTML注释
        content = content.replaceAll("<!--.*?-->", "");
        
        // 移除script和style标签及其内容
        content = content.replaceAll("(?s)<script[^>]*>.*?</script>", "");
        content = content.replaceAll("(?s)<style[^>]*>.*?</style>", "");
        
        // 转换标题标签
        content = convertHeadings(content);
        
        // 转换段落标签
        content = content.replaceAll("(?s)<p[^>]*>(.*?)</p>", "$1\n\n");
        
        // 转换换行标签
        content = content.replaceAll("(?i)<br[^>]*>", "\n");
        
        // 转换粗体和斜体
        content = content.replaceAll("(?s)<(strong|b)[^>]*>(.*?)</\\1>", "**$2**");
        content = content.replaceAll("(?s)<(em|i)[^>]*>(.*?)</\\1>", "*$2*");
        
        // 转换代码标签
        content = content.replaceAll("(?s)<code[^>]*>(.*?)</code>", "`$1`");
        content = content.replaceAll("(?s)<pre[^>]*>(.*?)</pre>", "```\n$1\n```\n");
        
        // 转换链接
        content = convertLinks(content);
        
        // 转换图片
        content = convertImages(content);
        
        // 转换列表
        content = convertLists(content);
        
        // 转换表格
        content = convertTables(content);
        
        // 转换引用
        content = content.replaceAll("(?s)<blockquote[^>]*>(.*?)</blockquote>", "> $1\n");
        
        // 转换水平线
        content = content.replaceAll("(?i)<hr[^>]*>", "\n---\n");
        
        // 移除剩余的HTML标签
        content = content.replaceAll("<[^>]+>", "");
        
        // 解码HTML实体
        content = decodeHtmlEntities(content);
        
        // 清理多余的空白
        content = content.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        
        return content.trim();
    }
    
    /**
     * 转换标题标签
     */
    private String convertHeadings(String content) {
        for (int i = 1; i <= 6; i++) {
            String hashtags = "#".repeat(i);
            Pattern pattern = Pattern.compile("(?s)<h" + i + "[^>]*>(.*?)</h" + i + ">", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            content = matcher.replaceAll(hashtags + " $1\n\n");
        }
        return content;
    }
    
    /**
     * 转换链接
     */
    private String convertLinks(String content) {
        Pattern linkPattern = Pattern.compile("(?s)<a[^>]*href=[\"'](.*?)[\"'][^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE);
        Matcher linkMatcher = linkPattern.matcher(content);
        return linkMatcher.replaceAll("[$2]($1)");
    }
    
    /**
     * 转换图片
     */
    private String convertImages(String content) {
        Pattern imgPattern = Pattern.compile("<img[^>]*src=[\"'](.*?)[\"'][^>]*alt=[\"'](.*?)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(content);
        content = imgMatcher.replaceAll("![$2]($1)");
        
        // 处理没有alt属性的图片
        Pattern imgPatternNoAlt = Pattern.compile("<img[^>]*src=[\"'](.*?)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcherNoAlt = imgPatternNoAlt.matcher(content);
        content = imgMatcherNoAlt.replaceAll("![]($1)");
        
        return content;
    }
    
    /**
     * 转换列表
     */
    private String convertLists(String content) {
        // 转换无序列表
        content = content.replaceAll("(?s)<ul[^>]*>(.*?)</ul>", "$1\n");
        content = content.replaceAll("(?s)<li[^>]*>(.*?)</li>", "- $1\n");
        
        // 转换有序列表（简化处理）
        content = content.replaceAll("(?s)<ol[^>]*>(.*?)</ol>", "$1\n");
        
        return content;
    }
    
    /**
     * 转换表格（简化处理）
     */
    private String convertTables(String content) {
        // 这是一个简化的表格转换，实际项目中可能需要更复杂的处理
        content = content.replaceAll("(?s)<table[^>]*>(.*?)</table>", "\n| 表格内容 |\n|----------|\n$1\n");
        content = content.replaceAll("(?s)<tr[^>]*>(.*?)</tr>", "| $1 |\n");
        content = content.replaceAll("(?s)<t[hd][^>]*>(.*?)</t[hd]>", " $1 |");
        
        return content;
    }
    
    /**
     * 解码HTML实体
     */
    private String decodeHtmlEntities(String content) {
        return content
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }
    
    @Override
    public String getParserName() {
        return "HTML文件解析器";
    }
    
    @Override
    public int getPriority() {
        return 60;
    }
} 