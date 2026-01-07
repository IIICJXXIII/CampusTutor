import request from './request'

// 获取钱包信息
export function getWallet() {
  return request.get('/api/wallet')
}

// 获取交易流水
export function getTransactions(params) {
  return request.get('/api/wallet/transactions', { params })
}

// 申请提现
export function withdraw(data) {
  return request.post('/api/wallet/withdraw', data)
}

// 获取提现记录
export function getWithdrawals(params) {
  return request.get('/api/wallet/withdrawals', { params })
}
