type SSECallback<T> = (data: T) => void;
type SSEErrorCallback = (error: Error) => void;
type SSECompleteCallback = () => void;

interface SSEClientOptions {
  url: string;
  headers?: Record<string, string>;
  onOpen?: SSECompleteCallback;
  onError?: SSEErrorCallback;
  onComplete?: SSECompleteCallback;
}

class SSEClient<T> {
  private controller: AbortController;
  private reader: ReadableStreamDefaultReader<Uint8Array> | null;
  private textDecoder: TextDecoder;
  private buffer: string;

  constructor(private options: SSEClientOptions) {
    this.controller = new AbortController();
    this.reader = null;
    this.textDecoder = new TextDecoder();
    this.buffer = '';
  }

  public subscribe(body: BodyInit, onMessage: SSECallback<T>) {
    this.controller.abort();
    this.controller = new AbortController();
    const { url, headers, onOpen, onError, onComplete } = this.options;

    const timeoutDuration = 300000;
    const timeoutId = setTimeout(() => {
      this.unsubscribe();
      onError?.(new Error('Request timed out after 5 minutes'));
    }, timeoutDuration);

    fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        ...headers,
      },
      body,
      signal: this.controller.signal,
    })
      .then(async (response) => {
        if (!response.ok) {
          clearTimeout(timeoutId);
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        if (!response.body) {
          clearTimeout(timeoutId);
          onError?.(new Error('No response body'));
          return;
        }

        onOpen?.();
        this.reader = response.body.getReader();

        while (true) {
          const { done, value } = await this.reader.read();
          if (done) {
            clearTimeout(timeoutId);
            onComplete?.();
            break;
          }

          this.processChunk(value, onMessage);
        }
      })
      .catch((error) => {
        clearTimeout(timeoutId);
        if (error.name !== 'AbortError') {
          onError?.(error);
        }
      });
  }

  private processChunk(chunk: Uint8Array | undefined, callback: SSECallback<T>) {
    if (!chunk) return;

    this.buffer += this.textDecoder.decode(chunk, { stream: true });
    const lines = this.buffer.split('\n');

    // 如果最后一行不是完整的，保留在buffer中
    this.buffer = lines.pop() || '';

    let currentData = '';
    let isDataLine = false;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      console.log('SSE原始行数据:', line); // 添加调试日志
      
      if (line.startsWith('data: ')) {
        const dataContent = line.slice(6);
        if (isDataLine) {
          currentData += '\n';
        }
        currentData += dataContent;
        isDataLine = true;
        console.log('SSE data行内容:', currentData); // 添加调试日志
      } else if (line.startsWith('data:')) {
        // 处理data:格式（冒号后面可能没有空格）
        const dataContent = line.slice(5);
        if (isDataLine) {
          currentData += '\n';
        }
        currentData += dataContent;
        isDataLine = true;
        console.log('SSE data行内容(无空格):', currentData); // 添加调试日志
      } else if (line === '') {
        if (isDataLine && currentData.trim()) { // 确保有实际数据再解析
          const trimmedData = currentData.trim(); // 去除前后空白字符
          console.log('准备解析JSON数据:', trimmedData); // 添加调试日志
          try {
            const data = JSON.parse(trimmedData) as T;
            console.log('JSON解析成功:', data); // 添加调试日志
            callback(data);
          } catch (error) {
            console.error('JSON解析失败:');
            console.error('原始数据:', currentData);
            console.error('清理后数据:', trimmedData);
            console.error('解析错误:', error);
          }
          currentData = '';
          isDataLine = false;
        }
      }
    }
  }

  public unsubscribe() {
    this.controller.abort();
    if (this.reader) {
      this.reader.cancel();
    }
    this.options.onComplete?.();
  }
}

export default SSEClient;