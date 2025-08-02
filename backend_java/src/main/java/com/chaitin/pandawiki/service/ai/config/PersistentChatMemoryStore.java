package com.chaitin.pandawiki.service.ai.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaitin.pandawiki.entity.ConversationMessage;
import com.chaitin.pandawiki.mapper.ConversationMessageMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 持久化聊天记忆存储
 * 解决 QwenHelper 消息处理错误的增强版本
 * 
 * @author: iohw
 * @date: 2025/4/13 10:35
 * @description: 增强版聊天记忆存储，修复消息重复和空列表问题
 */
@Slf4j
@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {
    @Autowired
    private ConversationMessageMapper conversationMessageMapper;
    @Autowired
    private Tokenizer tokenizer;

    private Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(100)
            .build();

    @Override
    public List<ChatMessage> getMessages(Object o) {
        String memoryId = (String) o;
        log.debug("正在获取会话 {} 的消息", memoryId);
        
        String json = cache.getIfPresent(memoryId);
        if (StringUtils.hasText(json)) {
            // 走缓存
            List<ChatMessage> cachedMessages = ChatMessageDeserializer.messagesFromJson(json);
            log.debug("从缓存获取会话 {} 的消息数量: {}", memoryId, cachedMessages.size());
            return ensureValidMessageStructure(cachedMessages, memoryId);
        }

        List<ChatMessage> messages = new ArrayList<>();
        LambdaQueryWrapper<ConversationMessage> queryWrapper = Wrappers.<ConversationMessage>lambdaQuery()
                .eq(ConversationMessage::getConversationId, memoryId)
                .orderByAsc(ConversationMessage::getCreateTime); // 确保按时间顺序排列
        
        List<ConversationMessage> chatMessageDOS = conversationMessageMapper.selectList(queryWrapper);
        
        // 添加调试日志
        log.debug("从数据库加载会话 {} 的消息数量: {}", memoryId, chatMessageDOS.size());
        
        for (ConversationMessage chatMessageDO : chatMessageDOS) {
            String role = chatMessageDO.getRole();
            String content = chatMessageDO.getContent();
            
            // 跳过空内容的消息
            if (content == null || content.trim().isEmpty()) {
                log.warn("跳过空内容消息 - role: {}, conversationId: {}", role, memoryId);
                continue;
            }
            
            try {
                ChatMessage message = switch (role.toLowerCase()) {
                    case "system" -> SystemMessage.from(content.trim());
                    case "user" -> UserMessage.from(content.trim());
                    case "assistant" -> AiMessage.from(content.trim());
                    case "tool" -> parseToolMessage(content);
                    default -> {
                        log.warn("未知角色类型: {}, 跳过消息", role);
                        yield null;
                    }
                };
                
                if (message != null) {
                    messages.add(message);
                }
            } catch (Exception e) {
                log.error("解析消息失败 - role: {}, content: {}, error: {}", role, content, e.getMessage());
            }
        }
        
        // 确保消息结构有效
        List<ChatMessage> validMessages = ensureValidMessageStructure(messages, memoryId);
        log.debug("会话 {} 最终有效消息数量: {}", memoryId, validMessages.size());
        return validMessages;
    }
    
    /**
     * 确保消息结构符合QwenHelper的期望
     * 1. 至少包含一条系统消息
     * 2. 消息顺序正确
     * 3. 避免重复的用户消息
     * 4. 确保列表不为空
     */
    private List<ChatMessage> ensureValidMessageStructure(List<ChatMessage> messages, String memoryId) {
        List<ChatMessage> validMessages = new ArrayList<>();
        
        log.debug("开始验证会话 {} 的消息结构，原消息数量: {}", memoryId, messages.size());
        
        // 1. 确保第一条是系统消息
        boolean hasSystemMessage = messages.stream().anyMatch(msg -> msg instanceof SystemMessage);
        if (!hasSystemMessage) {
            log.debug("会话 {} 缺少系统消息，添加默认系统消息", memoryId);
            validMessages.add(SystemMessage.from("你是一个专业的AI助手，请根据用户的问题提供准确、有用的回答。"));
        }
        
        // 2. 去重和排序消息，避免QwenHelper警告
        String lastUserMessage = null;
        String lastAiMessage = null;
        
        for (ChatMessage message : messages) {
            if (message instanceof SystemMessage && !hasSystemMessage) {
                validMessages.add(message);
                hasSystemMessage = true;
                log.debug("添加系统消息");
            } else if (message instanceof UserMessage) {
                String userText = ((UserMessage) message).singleText();
                // 避免重复的用户消息
                if (userText != null && !userText.equals(lastUserMessage)) {
                    validMessages.add(message);
                    lastUserMessage = userText;
                    log.debug("添加用户消息: {}", userText.length() > 50 ? userText.substring(0, 50) + "..." : userText);
                } else {
                    log.debug("跳过重复的用户消息: {}", userText);
                }
            } else if (message instanceof AiMessage) {
                String aiText = ((AiMessage) message).text();
                // 避免重复的AI消息
                if (aiText != null && !aiText.equals(lastAiMessage)) {
                    validMessages.add(message);
                    lastAiMessage = aiText;
                    log.debug("添加AI消息: {}", aiText.length() > 50 ? aiText.substring(0, 50) + "..." : aiText);
                } else {
                    log.debug("跳过重复的AI消息");
                }
            } else if (message instanceof ToolExecutionResultMessage) {
                // 工具消息直接添加，不去重
                validMessages.add(message);
                log.debug("添加工具执行结果消息");
            }
        }
        
        // 3. 如果仍然没有消息，添加默认系统消息
        if (validMessages.isEmpty()) {
            log.warn("会话 {} 的消息列表为空，添加默认系统消息", memoryId);
            validMessages.add(SystemMessage.from("你是一个专业的AI助手，请根据用户的问题提供准确、有用的回答。"));
        }
        
        // 4. 确保消息顺序正确（SystemMessage 在前）
        validMessages.sort((a, b) -> {
            if (a instanceof SystemMessage && !(b instanceof SystemMessage)) return -1;
            if (!(a instanceof SystemMessage) && b instanceof SystemMessage) return 1;
            return 0;
        });
        
        log.debug("会话 {} 消息结构验证完成，有效消息数量: {}", memoryId, validMessages.size());
        
        // 5. 最终安全检查
        if (validMessages.isEmpty()) {
            log.error("严重错误：会话 {} 的消息列表仍然为空，这可能导致 QwenHelper 错误", memoryId);
            validMessages.add(SystemMessage.from("系统初始化消息"));
        }
        
        return validMessages;
    }

    @Override
    public void updateMessages(Object o, List<ChatMessage> list) {
        String memoryId = o.toString();
        log.debug("正在更新会话 {} 的消息，数量: {}", memoryId, list.size());
        
        // 先进行消息结构验证
        List<ChatMessage> validatedMessages = ensureValidMessageStructure(list, memoryId);
        
        String json = ChatMessageSerializer.messagesToJson(validatedMessages);
        cache.put(memoryId, json);
        
        // 全量清空旧数据 + 全量增加新数据
        deleteMessages(memoryId);
        
        List<ConversationMessage> messageList = new ArrayList<>();
        for (ChatMessage chatMessage : validatedMessages) {
            try {
                String role = getRoleFromMessage(chatMessage);
                String content = getContentMessage(chatMessage);
                
                // 跳过空内容
                if (content == null || content.trim().isEmpty()) {
                    log.warn("跳过空内容消息 - role: {}", role);
                    continue;
                }
                
                // 若经过rag增强，分离出用户原始输入信息与被增加的输入信息
                String originContent = content;
                if (isUserMessageEnhanced(content)) {
                    originContent = content.substring(0, content.lastIndexOf("\n文档/文件/附件的内容如下，你可以基于下面的内容回答:\n"));
                }
                
                // 使用ConversationMessage实体
                ConversationMessage conversationMessage = new ConversationMessage();
                conversationMessage.setId(UUID.randomUUID().toString());
                conversationMessage.setConversationId(memoryId);
                conversationMessage.setRole(role);
                conversationMessage.setContent(originContent);
                conversationMessage.setCreateTime(java.time.LocalDateTime.now());
                
                messageList.add(conversationMessage);
            } catch (Exception e) {
                log.error("处理消息失败: {}", e.getMessage(), e);
            }
        }
        
        // 批量保存到数据库
        if (!messageList.isEmpty()) {
            try {
                for (ConversationMessage message : messageList) {
                    conversationMessageMapper.insert(message);
                }
                log.debug("保存了 {} 条消息到数据库", messageList.size());
            } catch (Exception e) {
                log.error("批量保存消息失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void deleteMessages(Object o) {
        String memoryId = o.toString();
        log.debug("正在删除会话 {} 的所有消息", memoryId);
        LambdaQueryWrapper<ConversationMessage> queryWrapper = Wrappers.<ConversationMessage>lambdaQuery()
                .eq(ConversationMessage::getConversationId, memoryId);
        conversationMessageMapper.delete(queryWrapper);
    }

    private String getRoleFromMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return "system";
        } else if (message instanceof UserMessage) {
            return "user";
        } else if (message instanceof AiMessage) {
            return "assistant";
        } else if (message instanceof ToolExecutionResultMessage) {
            return "tool";
        } else if (message instanceof CustomMessage) {
            return "custom";
        }
        throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
    }

    private String getContentMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return ((SystemMessage) message).text();
        } else if (message instanceof UserMessage) {
            return ((UserMessage) message).singleText();
        } else if (message instanceof AiMessage) {
            return ((AiMessage) message).text();
        } else if (message instanceof ToolExecutionResultMessage) {
            // 工具执行结果需要特殊处理
            ToolExecutionResultMessage toolMsg = (ToolExecutionResultMessage) message;
            return String.format("{id: %s, tool_name: %s, execution_result: %s}",
                    toolMsg.id(), toolMsg.toolName(), toolMsg.text());
        } else if (message instanceof CustomMessage) {
            // 自定义消息可能需要JSON序列化
            return ((CustomMessage) message).toString();
        }
        throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
    }
    
    private static boolean isUserMessageEnhanced(String userMessage) {
        return userMessage.contains("\n文档/文件/附件的内容如下，你可以基于下面的内容回答:\n");
    }
    
    private ToolExecutionResultMessage parseToolMessage(String content) {
        // 简单实现 - 实际应根据存储格式调整
        try {
            JSONObject json = new JSONObject(Boolean.parseBoolean(content.replace("{", "{\"").replace(":", "\":\"")));
            return new ToolExecutionResultMessage(
                    json.getString("message_id"),
                    json.getString("tool_name"),
                    json.getString("execution_result")
            );
        } catch (Exception e) {
            log.error("解析工具消息失败: {}", e.getMessage());
            // 返回一个默认的工具消息
            return new ToolExecutionResultMessage("unknown", "unknown", content);
        }
    }
}
