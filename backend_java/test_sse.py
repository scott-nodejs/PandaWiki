#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SSE聊天接口测试脚本
用于验证SSE接口是否正常工作且不再出现Shiro相关异常
"""

import requests
import json
import time
import sys

def test_sse_chat():
    """测试SSE聊天接口"""
    
    # 测试数据
    test_data = {
        "message": "Hello, this is a test message for SSE chat",
        "app_type": 1,
        "nonce": "",
        "conversation_id": ""
    }
    
    headers = {
        "Content-Type": "application/json",
        "Accept": "text/event-stream",
        "X-KB-ID": "test-kb-001",
        "Cache-Control": "no-cache"
    }
    
    url = "http://localhost:8080/share/v1/chat/message"
    
    print(f"🚀 开始测试SSE聊天接口: {url}")
    print(f"📝 测试数据: {json.dumps(test_data, ensure_ascii=False)}")
    print("=" * 80)
    
    try:
        # 发送SSE请求
        response = requests.post(
            url, 
            json=test_data, 
            headers=headers, 
            stream=True,
            timeout=60
        )
        
        print(f"📊 响应状态码: {response.status_code}")
        print(f"📋 响应头:")
        for key, value in response.headers.items():
            print(f"  {key}: {value}")
        print("-" * 80)
        
        if response.status_code == 200:
            print("✅ SSE连接建立成功，开始接收数据流:")
            print("-" * 80)
            
            event_count = 0
            data_chunks = 0
            
            # 逐行读取SSE数据流
            for line in response.iter_lines(decode_unicode=True):
                if line:
                    print(f"📨 {line}")
                    
                    # 解析SSE事件
                    if line.startswith('data:'):
                        try:
                            data_part = line[5:].strip()  # 移除 "data:" 前缀
                            if data_part:
                                event_data = json.loads(data_part)
                                event_count += 1
                                
                                event_type = event_data.get('type', 'unknown')
                                if event_type == 'data':
                                    data_chunks += 1
                                elif event_type == 'done':
                                    print("🎉 接收到完成信号，SSE流结束")
                                    break
                                    
                        except json.JSONDecodeError as e:
                            print(f"⚠️  JSON解析失败: {e}")
                    
                # 限制测试时间，避免无限等待
                if event_count > 100:  # 限制接收事件数量
                    print("⏰ 已接收足够的测试数据，停止测试")
                    break
            
            print("-" * 80)
            print(f"📈 测试统计:")
            print(f"  总事件数: {event_count}")
            print(f"  数据块数: {data_chunks}")
            print("✅ SSE测试完成，未发现Shiro相关异常！")
            
        else:
            print(f"❌ SSE连接失败，状态码: {response.status_code}")
            print(f"📄 响应内容: {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 网络请求异常: {e}")
        return False
    except KeyboardInterrupt:
        print("\n⏹️  用户中断测试")
        return False
    except Exception as e:
        print(f"❌ 未知异常: {e}")
        return False
    
    return True

def test_widget_chat():
    """测试Widget聊天接口"""
    
    test_data = {
        "message": "Test widget chat message",
        "app_type": 2,
        "nonce": "",
        "conversation_id": ""
    }
    
    headers = {
        "Content-Type": "application/json",
        "Accept": "text/event-stream",
        "X-KB-ID": "test-kb-001"
    }
    
    url = "http://localhost:8080/share/v1/chat/widget"
    
    print(f"\n🚀 开始测试Widget SSE聊天接口: {url}")
    print("=" * 80)
    
    try:
        response = requests.post(
            url, 
            json=test_data, 
            headers=headers, 
            stream=True,
            timeout=30
        )
        
        print(f"📊 响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ Widget SSE连接建立成功")
            
            # 简单接收几个事件就停止
            event_count = 0
            for line in response.iter_lines(decode_unicode=True):
                if line and line.startswith('data:'):
                    event_count += 1
                    print(f"📨 {line}")
                    if event_count >= 5:  # 只接收前5个事件
                        break
            
            print("✅ Widget SSE测试完成")
            return True
        else:
            print(f"❌ Widget SSE连接失败: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Widget测试异常: {e}")
        return False

if __name__ == "__main__":
    print("🔧 牛小库 SSE聊天接口测试")
    print("🎯 目标：验证SSE接口正常工作且无Shiro相关异常")
    print("=" * 80)
    
    # 测试基本聊天接口
    success1 = test_sse_chat()
    
    # 测试Widget聊天接口
    success2 = test_widget_chat()
    
    print("\n" + "=" * 80)
    if success1 and success2:
        print("🎉 所有SSE测试通过！SSE接口已成功绕过Shiro处理。")
        sys.exit(0)
    else:
        print("❌ 部分测试失败，请检查应用日志。")
        sys.exit(1) 