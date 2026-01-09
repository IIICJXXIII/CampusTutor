/**
 * 钱包相关 API (共享模块)
 */
import request from './request';

/**
 * 获取钱包信息
 */
export function getWalletInfo() {
  return request.get('/wallet');
}

/**
 * 获取交易流水
 * @param {Object} params - 分页参数
 */
export function getTransactions(params) {
  return request.get('/wallet/transactions', { params });
}

/**
 * 申请提现 (教师)
 * @param {Object} data - { amount, bankCard, bankName, realName }
 */
export function applyWithdraw(data) {
  return request.post('/wallet/withdraw', data);
}

/**
 * 获取提现记录 (教师)
 * @param {Object} params - 分页参数
 */
export function getWithdrawals(params) {
  return request.get('/wallet/withdrawals', { params });
}
/**
 * 充值
 * @param {Object} data - { amount, paymentMethod }
 */
export function recharge(data) {
  return request.post('/wallet/recharge', data);
}

/**
 * 获取钱包记录
 * @param {Object} params - 分页参数
 */
export function getWalletRecords(params) {
  return request.get('/wallet/records', { params });
}