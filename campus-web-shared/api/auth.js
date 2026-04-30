import request from './request';

export function login(data) {
  return request.post('/auth/login', {
    account: data.account,
    password: data.password
  });
}

export function register(data) {
  return request.post('/auth/register', data);
}

export function resetPassword(data) {
  return request.post('/auth/reset/password', data);
}
