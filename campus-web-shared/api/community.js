import request from './request';

export function getCommunityPosts(params) {
  return request.get('/community/posts', { params });
}

export function getCommunityPostDetail(id) {
  return request.get(`/community/posts/${id}`);
}

export function createCommunityPost(data) {
  return request.post('/community/posts', data);
}

export function likeCommunityPost(id) {
  return request.post(`/community/posts/${id}/like`);
}

export function getCommunityReplies(postId, params) {
  return request.get(`/community/posts/${postId}/replies`, { params });
}

export function createCommunityReply(postId, data) {
  return request.post(`/community/posts/${postId}/replies`, data);
}
