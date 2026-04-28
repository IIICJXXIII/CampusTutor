import request from './request';

export function getTutorList(params) {
  return request.get('/match/tutors', { params });
}

export function searchTutors(data) {
  return request.post('/match/tutors', data);
}
