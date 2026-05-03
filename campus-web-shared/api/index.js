/**
 * API 模块统一导出 (共享模块)
 */

// 基础请求
export { default as request } from './request';

// 认证
export * from './auth';

// 用户
export * from './user';

// 教员
export * from './tutor';

// 家长
export * from './parent';

// 需求
export * from './demand';

// 匹配
export * from './match';

// 订单
export * from './order';

// 课时
export * from './teaching';

// 钱包
export * from './wallet';

// 聊天
export * from './chat';

// 文件
export * from './file';

// OCR
export * from './ocr';

// 地图
export * from './map';

// LLM
export * from './llm';

// 评价
export * from './review';

// 错题本
export * from './wrongbook';

// 社区
export * from './community';
