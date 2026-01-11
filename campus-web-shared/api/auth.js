/**
 * 认证相关 API (共享模块)
 */
import request from './request';

/**
 * 用户登录
 * @param {Object} data - { account, password, code, loginType }
 */
export function login(data) {
  return request.post('/auth/login', {
    account: data.account,
    password: data.password,
    code: data.code,
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
 * 微信小程序登录
 * @param {Object} data - { code, encryptedData, iv, userInfo }
 */
export function wxLogin(data) {
  return request.post('/auth/wx-login', data);
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get('/user/current');
}
