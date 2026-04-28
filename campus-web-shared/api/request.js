import axios from 'axios';

let _showError = (msg) => console.error('[API]', msg);
let _isRedirecting = false;

export function setRequestErrorHandler(handler) {
  _showError = handler;
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

request.interceptors.request.use(
  config => {
    const role = localStorage.getItem('userRole') || ''
    const prefix = role ? `${role}_` : ''
    const token = localStorage.getItem(`${prefix}token`) || localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  response => {
    const res = response.data;
    
    if (res.code === 200) {
      return res;
    }
    
    _showError(res.msg || '请求失败');
    const error = new Error(res.msg || '请求失败');
    error._handled = true;
    return Promise.reject(error);
  },
  error => {
    if (error.response) {
      const status = error.response.status;
      
      switch (status) {
        case 401:
          if (!_isRedirecting) {
            _isRedirecting = true;
            const role = localStorage.getItem('userRole') || ''
            const prefix = role ? `${role}_` : ''
            localStorage.removeItem(`${prefix}token`);
            localStorage.removeItem(`${prefix}userInfo`);
            localStorage.removeItem(`${prefix}ai_chat_history`);
            localStorage.removeItem('userRole');
            localStorage.removeItem('token');
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
        case 429:
          _showError(error.response.data?.msg || '操作过于频繁，请稍后再试');
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
