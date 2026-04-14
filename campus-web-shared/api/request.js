/**
 * API 请求封装 (共享模块)
 * 基于 axios，统一处理请求拦截、响应拦截、错误处理
 */
import axios from 'axios';

// 全局错误提示回调，由各 App 在 main.js 中注入
let _showError = (msg) => console.error('[API]', msg);

// 防止多个 401 同时触发重复跳转
let _isRedirecting = false;

/**
 * 注入全局错误提示函数（在 App 入口调用）
 * @param {(msg: string) => void} handler
 */
export function setRequestErrorHandler(handler) {
  _showError = handler;
}

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
    console.warn('业务异常:', res.msg);
    _showError(res.msg || '请求失败');
    return Promise.reject(new Error(res.msg || '请求失败'));
  },
  error => {
    // HTTP 错误处理
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 401:
          // Token 失效，清除登录状态，防止多个请求重复跳转
          if (!_isRedirecting) {
            _isRedirecting = true;
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            localStorage.removeItem('userRole');
            _showError('登录已过期，请重新登录');
            setTimeout(() => {
              window.location.href = '/login';
            }, 100);
          }
          break;
        case 403:
          _showError('没有权限访问');
          break;
        case 404:
          _showError('请求资源不存在');
          break;
        case 500:
          _showError('服务器内部错误');
          break;
        default:
          _showError(`请求失败 (${status})`);
      }
    } else if (error.request) {
      _showError('网络异常，请检查网络连接');
    }
    
    return Promise.reject(error);
  }
);

export default request;
