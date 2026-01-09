/**
 * 文件相关 API (共享模块)
 */
import request from './request';

/**
 * 上传文件
 * @param {FormData} formData - 文件表单
 * @param {string} type - 文件类型 (common/cert/avatar)
 */
export function uploadFile(formData, type = 'common') {
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    params: { type }
  });
}

/**
 * 删除文件
 * @param {string} fileUrl - 文件URL
 */
export function deleteFile(fileUrl) {
  return request.delete('/file/', {
    params: { url: fileUrl }
  });
}
