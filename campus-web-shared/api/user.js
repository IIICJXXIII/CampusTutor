import request from './request';

export function getCurrentUser() {
  return request.get('/user/current');
}

export function getUserById(id) {
  return request.get(`/user/${id}`);
}

export function updateUserInfo(data) {
  return request.put('/user/info', data);
}

export function updatePassword(data) {
  return request.put('/user/password', data);
}
