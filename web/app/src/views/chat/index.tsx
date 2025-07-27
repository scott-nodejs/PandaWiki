'use client';

import { ChunkResultItem } from '@/assets/type';
import { useStore } from '@/provider';
import SSEClient from '@/utils/fetch';
import { Box, Stack } from '@mui/material';
import { message } from 'ct-mui';
import { useCallback, useEffect, useRef, useState } from 'react';
import ChatResult from './ChatResult';
import ChatTab from './ChatTab';
import SearchResult from './SearchResult';
import { AnswerStatus } from './constant';

const Chat = () => {
  const { mobile = false, kb_id, token, kbDetail, catalogShow } = useStore()

  const chatContainerRef = useRef<HTMLDivElement | null>(null);
  const sseClientRef = useRef<SSEClient<{
    type: string;
    content: string;
    chunk_result: ChunkResultItem[];
  }> | null>(null);

  const [conversation, setConversation] = useState<{ q: string, a: string }[]>([]);
  const [loading, setLoading] = useState(false);
  const [thinking, setThinking] = useState<keyof typeof AnswerStatus>(4);
  const [nonce, setNonce] = useState('');
  const [chunkResult, setChunkResult] = useState<any[]>([]);
  const [chunkLoading, setChunkLoading] = useState(false);
  const [conversationId, setConversationId] = useState('');
  const [answer, setAnswer] = useState('');
  const [isUserScrolling, setIsUserScrolling] = useState(false);

  const [showType, setShowType] = useState<'chat' | 'search'>('chat');

  const catalogSetting = kbDetail?.settings?.catalog_settings

  const chatAnswer = async (q: string) => {
    console.log('chatAnswer开始:', { question: q });
    setChunkLoading(true);
    setLoading(true);
    setThinking(1);
    setIsUserScrolling(false);

    const reqData = {
      message: q,
      nonce: '',
      conversation_id: '',
      app_type: 1,
    };
    if (conversationId) reqData.conversation_id = conversationId;
    if (nonce) reqData.nonce = nonce;

    if (sseClientRef.current) {
      sseClientRef.current.subscribe(
        JSON.stringify(reqData),
        ({ type, content, chunk_result }) => {
          console.log('SSE接收数据:', { type, content, chunk_result });
          if (type === 'conversation_id') {
            console.log('设置conversationId:', content);
            setConversationId((prev) => prev + content);
          } else if (type === 'nonce') {
            console.log('设置nonce:', content);
            setNonce((prev) => prev + content);
          } else if (type === 'error') {
            console.log('收到error信号:', content);
            setChunkLoading(false);
            setLoading(false);
            setThinking(4);
            setAnswer((prev) => {
              const newAnswer = content ? prev + `\n\n回答出现错误：<e>${content}</e>` : prev + '\n\n回答出现错误，请重试';
              console.log('error时更新answer:', { prev, content, newAnswer });
              return newAnswer;
            });
            if (content) message.error(content);
          } else if (type === 'done') {
            console.log('收到done信号，清理加载状态');
            setChunkLoading(false);
            setLoading(false);
            setThinking(4);
          } else if (type === 'data') {
            console.log('收到data信号:', { content, contentLength: content?.length });
            setChunkLoading(false);
            setAnswer((prev) => {
              const newAnswer = prev + content;
              console.log('data时更新answer状态:', { 
                prev: prev, 
                prevLength: prev.length,
                content: content, 
                contentLength: content?.length,
                newAnswer: newAnswer,
                newAnswerLength: newAnswer.length 
              });
              if (newAnswer.includes('</think>')) {
                setThinking(3);
                return newAnswer;
              }
              if (newAnswer.includes('<think>')) {
                setThinking(2);
                return newAnswer;
              }
              setThinking(3);
              return newAnswer;
            });
          } else if (type === 'chunk_result') {
            console.log('收到chunk_result:', chunk_result);
            setChunkResult((prev) => {
              return [...prev, chunk_result];
            });
          }
        },
      );
    }
  };

  const onSearch = (q: string, reset: boolean = false) => {
    console.log('onSearch开始:', { question: q, reset, loading, currentAnswer: answer });
    if (loading || !q.trim()) return;
    const newConversation = reset ? [] : [...conversation.slice(0, -1)];
    if (answer) {
      newConversation.push({ q: conversation[conversation.length - 1].q, a: answer });
    }
    newConversation.push({ q, a: '' });
    console.log('创建新对话:', { newConversation, question: q });
    setConversation(newConversation);
    console.log('重置answer状态为空');
    setAnswer('');
    setChunkResult([]);
    console.log('准备调用chatAnswer');
    setTimeout(() => {
      console.log('setTimeout执行chatAnswer');
      chatAnswer(q);
    }, 0);
  };

  const handleSearchAbort = () => {
    sseClientRef.current?.unsubscribe();
    setChunkLoading(false);
    setLoading(false);
    setThinking(4);
  };

  const handleScroll = useCallback(() => {
    if (chatContainerRef?.current) {
      const { scrollTop, scrollHeight, clientHeight } =
        chatContainerRef.current;
      setIsUserScrolling(scrollTop + clientHeight < scrollHeight);
    }
  }, [chatContainerRef]);

  useEffect(() => {
    const chatContainer = chatContainerRef?.current;
    chatContainer?.addEventListener('scroll', handleScroll);
    return () => {
      chatContainer?.removeEventListener('scroll', handleScroll);
    };
  }, [handleScroll]);

  useEffect(() => {
    if (!isUserScrolling && chatContainerRef?.current) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
    }
  }, [answer, isUserScrolling]);

  useEffect(() => {
    const searchQuery = sessionStorage.getItem('chat_search_query');
    if (searchQuery) {
      sessionStorage.removeItem('chat_search_query');
      onSearch(searchQuery, true);
    }
  }, []);

  useEffect(() => {
    if (kb_id) {
      sseClientRef.current = new SSEClient({
        url: `/client/v1/chat/message`,
        headers: {
          'Content-Type': 'application/json',
          'x-simple-auth-password': token || '',
        },
        onComplete: () => {
          // SSE连接完成时清理加载状态
          console.log('SSE连接已完成');
          setChunkLoading(false);
          setLoading(false);
          setThinking(4);
        },
        onError: (error) => {
          // SSE连接错误时清理加载状态
          console.error('SSE连接错误:', error);
          setChunkLoading(false);
          setLoading(false);
          setThinking(4);
        },
      });
    }
  }, []);

  if (mobile) {
    return <Box sx={{ pt: 12, minHeight: '100vh', position: 'relative' }}>
      <ChatTab showType={showType} setShowType={setShowType} />
      <Box sx={{ mx: 3 }}>
        {showType === 'chat' ? <ChatResult
          conversation={conversation}
          answer={answer}
          loading={loading}
          thinking={thinking}
          setThinking={setThinking}
          onSearch={onSearch}
          handleSearchAbort={handleSearchAbort}
        /> : <SearchResult list={chunkResult} loading={chunkLoading} />}
      </Box>
    </Box>
  }

  return (
    <Box sx={{
      pt: 12,
      ml: catalogShow ? `${catalogSetting?.catalog_width ?? 260}px` : '16px',
      px: 10,
      minHeight: '100vh',
    }}>
      <Stack alignItems="stretch" direction="row" gap={3} sx={{
        height: 'calc(100vh - 120px)',
        mb: 3,
        maxWidth: '1200px',
        mx: 'auto',
      }}>
        <Box sx={{ position: 'relative', flex: 1, width: 0 }}>
          <ChatResult
            conversation={conversation}
            answer={answer}
            loading={loading}
            thinking={thinking}
            setThinking={setThinking}
            onSearch={onSearch}
            handleSearchAbort={handleSearchAbort}
          />
        </Box>
        <Box sx={{
          flexShrink: 0,
          width: 388,
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: '10px',
          p: 3,
          bgcolor: 'background.paper',
        }}>
          <Box sx={{
            fontSize: '20px',
            fontWeight: '700',
            lineHeight: '28px',
            mb: 2,
          }}>搜索结果</Box>
          <SearchResult list={chunkResult} loading={chunkLoading} />
        </Box>
      </Stack>
    </Box>
  );
};

export default Chat;
