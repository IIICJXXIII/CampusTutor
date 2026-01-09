/**
 * API 请求封装 (共享模块)
 * 基于 axios，统一处理请求拦截、响应拦截、错误处理
 */
import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加 Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一处理响应
request.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 业务成功 (code = 200)
    if (res.code === 200) {
      return res;
    }
    
    // 业务失败
    console.warn('业务异常:', res.message);
    return Promise.reject(new Error(res.message || '请求失败'));
  },
  error => {
    // HTTP 错误处理
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 401:
          // Token 失效，清除登录状态
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          window.location.href = '/login';
          break;
        case 403:
          console.error('没有权限访问');
          break;
        case 404:
          console.error('请求资源不存在');
          break;
        case 500:
          console.error('服务器内部错误');
          break;
        default:
          console.error(`HTTP错误 ${status}`);
      }
    } else if (error.request) {
      console.error('网络异常，请检查网络连接');
    }
    
    return Promise.reject(error);
  }
);

export default request;
