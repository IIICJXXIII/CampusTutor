/**
 * 认证相关 API
 */
import request from './request';

/**
 * 用户登录
 * @param {Object} data - { account, password, loginType }
 */
export function login(data) {
  return request.post('/auth/login', {
    account: data.account,
    password: data.password,
    loginType: data.loginType || 'password'
  });
}

/**
 * 用户注册
 * @param {Object} data - { phone, password, code, role, nickname }
 */
export function register(data) {
  return request.post('/auth/register', data);
}

/**
 * 发送验证码
 * @param {string} phone - 手机号
 */
export function sendCode(phone) {
  return request.post('/auth/send-code', null, {
    params: { phone }
  });
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get('/user/current');
}
