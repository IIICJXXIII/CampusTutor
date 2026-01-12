/**
 * 文件上传 API
 * 路径: src/api/file.js
 */
// 注意：如果 request.js 在 src/utils 下，请改为 import request from '@/utils/request'
// 如果 request.js 在 src/api 下，则保持 import request from './request'
import request from './request'; 

/**
 * 上传文件
 * @param {File} file - 文件对象
 * @param {string} folder - 存储文件夹 (cert/avatar/demand)
 */
export function uploadFile(file, folder = 'cert') {
  const formData = new FormData();
  formData.append('file', file);
  // 注意：后端接收参数名如果是 'type' 请改为 'type'，如果是 'folder' 则保持不变
  formData.append('folder', folder); 
  
  // 使用通用请求格式，兼容性更好
  return request({
    url: '/file/upload', // 你的后端接口地址
    method: 'post',
    data: formData,
    headers: {
      // 手动指定 Content-Type，确保 boundary 正确生成
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 删除文件
 * @param {string} fileUrl - 文件URL
 */
export function deleteFile(fileUrl) {
  return request({
    url: '/file',
    method: 'delete',
    params: { fileUrl }
  });
}