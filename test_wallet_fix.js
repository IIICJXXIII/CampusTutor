// 测试钱包修复
const request = require('./campus-user-app/miniprogram/utils/request.js');
const apiConfig = require('./campus-user-app/miniprogram/config/apiConfig.js');

async function testWalletCreation() {
  console.log('测试钱包自动创建...');
  try {
    // 1. 首先获取钱包信息，应该自动创建
    const wallet1 = await request.get(apiConfig.wallet.info);
    console.log('钱包信息 1:', wallet1);
    console.log('余额:', wallet1.balance);
    console.log('冻结金额:', wallet1.frozenAmount);
    
    // 2. 模拟订单ID
    const orderId = 12;
    
    // 3. 模拟支付请求
    console.log('发起支付请求...');
    const payResult = await request.post(apiConfig.order.pay, {
      orderId: orderId,
      payType: 2,
      openid: 'o123456789abcdefghijklmnopqrstuvwxyz'
    });
    console.log('支付结果:', payResult);
    
    // 4. 支付后再次获取钱包信息
    console.log('支付后获取钱包信息...');
    const wallet2 = await request.get(apiConfig.wallet.info);
    console.log('支付后钱包信息:', wallet2);
    console.log('支付后余额:', wallet2.balance);
    console.log('支付后冻结金额:', wallet2.frozenAmount);
    
    // 5. 检查冻结金额是否增加
    const frozenAmount1 = parseFloat(wallet1.frozenAmount);
    const frozenAmount2 = parseFloat(wallet2.frozenAmount);
    
    if (frozenAmount2 > frozenAmount1) {
      console.log('✅ 测试通过：冻结金额已正确增加');
      console.log('增加的冻结金额:', frozenAmount2 - frozenAmount1);
    } else {
      console.log('❌ 测试失败：冻结金额未增加');
      console.log('冻结金额变化:', frozenAmount2 - frozenAmount1);
    }
    
  } catch (error) {
    console.error('测试失败:', error);
    if (error.msg) {
      console.error('错误信息:', error.msg);
    }
  }
}

async function testIncomeDetail() {
  console.log('测试收入明细...');
  try {
    const transactions = await request.get(apiConfig.wallet.transactions, { page: 1, size: 10 });
    console.log('收入明细:', transactions);
    console.log('交易记录数量:', transactions.records ? transactions.records.length : 0);
    
    if (transactions.records && transactions.records.length > 0) {
      console.log('✅ 测试通过：收入明细已生成');
      transactions.records.forEach((record, index) => {
        console.log(`记录 ${index + 1}:`, {
          type: record.flowType,
          amount: record.amount,
          balanceAfter: record.balanceAfter,
          remark: record.remark,
          time: record.createTime
        });
      });
    } else {
      console.log('❌ 测试失败：收入明细未生成');
    }
    
  } catch (error) {
    console.error('获取收入明细失败:', error);
  }
}

// 运行测试
async function runTests() {
  console.log('开始钱包修复测试...');
  console.log('='.repeat(60));
  await testWalletCreation();
  console.log('='.repeat(60));
  await testIncomeDetail();
  console.log('='.repeat(60));
  console.log('测试完成');
}

runTests();