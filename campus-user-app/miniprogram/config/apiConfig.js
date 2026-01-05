// config/apiConfig.js

// 环境变量：'develop' | 'trial' | 'release'
const env = 'develop'; 

// 接口基地址配置
const baseUrls = {
  develop: 'http://127.0.0.1:8080', // 本地Spring Boot后端地址
  trial: 'https://test-api.yourdomain.com',
  release: 'https://api.yourdomain.com'
};

const config = {
  baseUrl: baseUrls[env],
  // 腾讯地图/高德地图 Key (用于地图找生功能 [cite: 301])
  mapKey: 'YOUR_MAP_SDK_KEY',
  // 默认分页大小
  pageSize: 10
};

module.exports = config;