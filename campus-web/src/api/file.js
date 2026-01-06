/**
 * 文件上传 API
 */
import request from './request';

/**
 * 上传文件
 * @param {File} file - 文件对象
 * @param {string} folder - 存储文件夹 (cert/avatar/demand)
 */
export function uploadFile(file, folder = 'cert') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);
  
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 删除文件
 * @param {string} fileUrl - 文件URL
 */
export function deleteFile(fileUrl) {
  return request.delete('/file', {
    params: { fileUrl }
  });
}
