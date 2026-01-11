// 钱包测试脚本
const request = require('./campus-user-app/miniprogram/utils/request.js');
const apiConfig = require('./campus-user-app/miniprogram/config/apiConfig.js');

// 测试获取钱包信息
async function testWalletInfo() {
  console.log('测试获取钱包信息...');
  try {
    const wallet = await request.get(apiConfig.wallet.info);
    console.log('钱包信息:', wallet);
    console.log('余额:', wallet.balance);
    console.log('冻结金额:', wallet.frozenAmount);
  } catch (error) {
    console.error('获取钱包信息失败:', error);
  }
}

// 测试支付流程
async function testPaymentFlow() {
  console.log('测试支付流程...');
  try {
    // 模拟订单ID
    const orderId = 12;
    // 模拟支付请求
    const payResult = await request.post(apiConfig.order.pay, {
      orderId: orderId,
      payType: 2,
      openid: 'o123456789abcdefghijklmnopqrstuvwxyz'
    });
    console.log('支付结果:', payResult);
    
    // 支付后再次获取钱包信息
    console.log('支付后获取钱包信息...');
    const wallet = await request.get(apiConfig.wallet.info);
    console.log('支付后钱包信息:', wallet);
    console.log('支付后余额:', wallet.balance);
    console.log('支付后冻结金额:', wallet.frozenAmount);
    
  } catch (error) {
    console.error('支付流程测试失败:', error);
  }
}

// 运行测试
async function runTests() {
  console.log('开始钱包测试...');
  await testWalletInfo();
  console.log('-----------------------------');
  await testPaymentFlow();
  console.log('测试完成');
}

runTests();