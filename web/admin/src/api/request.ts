import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
} from "axios";
import { Message } from "ct-mui";

type BasicResponse<T> = {
  data: T;
  success: boolean;
  message: string;
};

type ErrorResponse = {
  data: unknown;
  success: boolean;
  message: string;
};

type Response<T> = BasicResponse<T> | ErrorResponse;

const request = <T>(options: AxiosRequestConfig): Promise<T> => {
  const token = localStorage.getItem('panda_wiki_token') || ''
  const config = {
    baseURL: "/",
    timeout: 0,
    withCredentials: true,
    headers: {
      Authorization: `Bearer ${token}`,
    },
    // 自定义参数序列化器，数组参数不使用方括号
    paramsSerializer: {
      serialize: (params: Record<string, any>) => {
        const searchParams = new URLSearchParams();
        for (const [key, value] of Object.entries(params)) {
          if (Array.isArray(value)) {
            // 数组参数：node_ids=xx&node_ids=ccc
            value.forEach(item => searchParams.append(key, item));
          } else if (value !== undefined && value !== null) {
            searchParams.append(key, value);
          }
        }
        return searchParams.toString();
      }
    }
  }
  const service: AxiosInstance = axios.create(config);
  service.interceptors.response.use(
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    (response: AxiosResponse<Response<T>>) => {
      if (response.status === 200) {
        const res = response.data;
        if (res.success) {
          return res.data;
        }
        Message.error(res.message || "网络异常");
        return Promise.reject(res);
      }
      Message.error(response.statusText);
      return Promise.reject(response);
    },
    (error: AxiosError) => {
      if (error.response?.status === 401) {
        window.location.href = '/login'
        localStorage.removeItem('panda_wiki_token')
      }
      Message.error(error.response?.statusText || "网络异常");
      return Promise.reject(error.response);
    }
  );

  return service(options);
};

export default request;
