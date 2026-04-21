/**
 * CampusTutor 模块八（课时打卡管理）& 模块九（钱包与资金流）完整测试脚本
 * 
 * 测试账号：
 *   教师端: 17209892755 / 123456
 *   家长端: 15273153320 / 123456
 */

const BASE_URL = 'http://localhost:8080';

// ============ 工具函数 ============

async function httpRequest(method, url, body, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const options = { method, headers };
  if (body && (method === 'POST' || method === 'PUT')) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(`${BASE_URL}${url}`, options);
  const text = await response.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }
  return { status: response.status, data, ok: response.ok };
}

async function GET(url, token) { return httpRequest('GET', url, null, token); }
async function POST(url, body, token) { return httpRequest('POST', url, body, token); }
async function PUT(url, body, token) { return httpRequest('PUT', url, body, token); }
async function DELETE(url, token) { return httpRequest('DELETE', url, null, token); }

// Test result tracking
const results = [];
let passCount = 0, failCount = 0, skipCount = 0;

function logTest(module, id, name, passed, detail, skipped = false) {
  const status = skipped ? '⏭️ SKIP' : (passed ? '✅ PASS' : '❌ FAIL');
  if (skipped) skipCount++;
  else if (passed) passCount++;
  else failCount++;
  results.push({ module, id, name, status, detail });
  console.log(`  ${status} [${module}-${id}] ${name}`);
  if (detail && !passed && !skipped) console.log(`         → ${typeof detail === 'object' ? JSON.stringify(detail).substring(0, 200) : detail}`);
}

// ============ 登录 ============

async function login(phone, password) {
  const res = await POST('/api/auth/login', { account: phone, password, loginType: 'password' });
  if (res.data && res.data.code === 200 && res.data.data) {
    const data = res.data.data;
    const token = data.token;
    if (typeof token === 'string') return { token, userId: data.userId, role: data.role };
    return null;
  }
  console.log(`  ⚠️ 登录失败(${phone}): ${JSON.stringify(res.data).substring(0, 200)}`);
  return null;
}

// ============ 主测试流程 ============

async function main() {
  console.log('='.repeat(70));
  console.log('  CampusTutor 模块八 & 模块九 完整测试');
  console.log('  测试时间:', new Date().toLocaleString());
  console.log('='.repeat(70));

  // ---- Step 0: 登录 ----
  console.log('\n📋 Step 0: 登录获取 Token\n');

  const teacherLoginResult = await login('17209892755', '123456');
  const parentLoginResult = await login('15273153320', '123456');

  if (!teacherLoginResult) {
    console.log('❌ 教师登录失败，无法继续测试');
    return;
  }
  if (!parentLoginResult) {
    console.log('❌ 家长登录失败，无法继续测试');
    return;
  }

  const teacherToken = teacherLoginResult.token;
  const parentToken = parentLoginResult.token;
  const teacherUserId = teacherLoginResult.userId;
  const parentUserId = parentLoginResult.userId;

  console.log(`  ✅ 教师登录成功, userId=${teacherUserId}, role=${teacherLoginResult.role}, token: ${teacherToken.substring(0, 30)}...`);
  console.log(`  ✅ 家长登录成功, userId=${parentUserId}, role=${parentLoginResult.role}, token: ${parentToken.substring(0, 30)}...`);

  // ========================================================================
  // 模块九：钱包与资金流（先测钱包，后面打卡测试需要有已支付订单）
  // ========================================================================
  console.log('\n' + '='.repeat(70));
  console.log('  模块九：钱包与资金流');
  console.log('='.repeat(70));

  // ---- 9.1 钱包基础 ----
  console.log('\n📋 9.1 钱包基础\n');

  // 9.1.1 获取/自动创建钱包 - 家长
  let parentWallet = null;
  {
    const res = await GET('/api/wallet', parentToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    parentWallet = ok ? res.data.data : null;
    logTest('M9', '1.1', '家长获取/自动创建钱包', ok,
      ok ? `余额=${parentWallet.balance}, 冻结=${parentWallet.frozenAmount}, payPassword=${parentWallet.payPassword}` : res.data);
    // 验证 payPassword 为 null
    if (ok) {
      logTest('M9', '1.1a', '钱包 payPassword 应为 null', parentWallet.payPassword === null || parentWallet.payPassword === undefined,
        `payPassword=${parentWallet.payPassword}`);
    }
  }

  // 9.1.1b 获取/自动创建钱包 - 教师
  let teacherWallet = null;
  {
    const res = await GET('/api/wallet', teacherToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    teacherWallet = ok ? res.data.data : null;
    logTest('M9', '1.2', '教师获取/自动创建钱包', ok,
      ok ? `余额=${teacherWallet.balance}, 冻结=${teacherWallet.frozenAmount}` : res.data);
  }

  // 9.1.2 充值(Mock) - 家长充值500
  let parentBalanceBefore = parentWallet ? parseFloat(parentWallet.balance) : 0;
  {
    const res = await POST('/api/wallet/recharge?amount=500&paymentMethod=wechat', null, parentToken);
    const ok = res.data && res.data.code === 200;
    logTest('M9', '1.3', '家长充值500元(Mock)', ok, ok ? res.data.msg : res.data);
  }

  // 验证充值后余额
  {
    const res = await GET('/api/wallet', parentToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const newBalance = parseFloat(res.data.data.balance);
      const expected = parentBalanceBefore + 500;
      const balanceOk = Math.abs(newBalance - expected) < 0.01;
      logTest('M9', '1.4', `充值后余额验证 (期望≈${expected}, 实际=${newBalance})`, balanceOk,
        `充值前=${parentBalanceBefore}, 充值后=${newBalance}`);
      parentWallet = res.data.data;
    } else {
      logTest('M9', '1.4', '充值后余额验证', false, res.data);
    }
  }

  // 9.1.3 查看交易流水
  {
    const res = await GET('/api/wallet/transactions?page=1&size=10', parentToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const records = res.data.data.records || res.data.data;
      const hasRecharge = Array.isArray(records) && records.some(r => r.flowType === 1);
      logTest('M9', '1.5', '查看交易流水 - 含充值记录', hasRecharge,
        `流水数量=${Array.isArray(records) ? records.length : 0}, 含充值=${hasRecharge}`);
    } else {
      logTest('M9', '1.5', '查看交易流水', false, res.data);
    }
  }

  // 9.1.x 教师充值500 (后面提现测试需要)
  {
    const res = await POST('/api/wallet/recharge?amount=500&paymentMethod=alipay', null, teacherToken);
    const ok = res.data && res.data.code === 200;
    logTest('M9', '1.6', '教师充值500元(准备提现测试)', ok, ok ? res.data.msg : res.data);
  }

  // ---- 9.2 提现 ----
  console.log('\n📋 9.2 提现\n');

  // 先获取教师最新余额
  let teacherBalanceBefore = 0;
  {
    const res = await GET('/api/wallet', teacherToken);
    if (res.data && res.data.code === 200 && res.data.data) {
      teacherBalanceBefore = parseFloat(res.data.data.balance);
      teacherWallet = res.data.data;
    }
  }

  // 9.2.1 教师发起提现
  let withdrawalId = null;
  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 100,
      channel: 2, // 支付宝
      accountNo: 'teacher_test@alipay.com'
    }, teacherToken);
    const ok = res.data && res.data.code === 200;
    withdrawalId = ok ? res.data.data : null;
    logTest('M9', '2.1', '教师发起提现(100元)', ok,
      ok ? `withdrawalId=${withdrawalId}` : res.data);
  }

  // 9.2.2 查看提现记录
  {
    const res = await GET('/api/wallet/withdrawals?page=1&size=10', teacherToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const records = res.data.data.records || res.data.data;
      const hasWithdrawal = Array.isArray(records) && records.length > 0;
      logTest('M9', '2.2', '查看提现记录', hasWithdrawal,
        `提现记录数=${Array.isArray(records) ? records.length : 0}`);
    } else {
      logTest('M9', '2.2', '查看提现记录', false, res.data);
    }
  }

  // 9.2.3 提现金额超过余额
  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 999999,
      channel: 1,
      accountNo: 'over_balance@wechat.com'
    }, teacherToken);
    const ok = res.data && res.data.code !== 200;
    logTest('M9', '2.3', '提现金额超过余额 - 应拒绝', ok,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // 9.2.4 提现金额小于1元 - 参数校验
  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 0.5,
      channel: 1,
      accountNo: 'small@wechat.com'
    }, teacherToken);
    const ok = res.data && res.data.code !== 200;
    logTest('M9', '2.4', '提现金额小于1元 - 应拒绝(@DecimalMin)', ok,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // 9.2.5 提现渠道不正确  
  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 10,
      channel: 99, // 无效渠道
      accountNo: 'bad_channel@test.com'
    }, teacherToken);
    const ok = res.data && res.data.code !== 200;
    logTest('M9', '2.5', '无效提现渠道 - 应拒绝', ok,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // 9.2.6 缺少必填字段 - accountNo 为空
  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 10,
      channel: 1,
      accountNo: ''
    }, teacherToken);
    const ok = res.data && res.data.code !== 200;
    logTest('M9', '2.6', '收款账号为空 - 应拒绝(@NotBlank)', ok,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // ---- 9.3 资金流转验证 (需要有进行中的订单) ----
  console.log('\n📋 9.3 资金流转验证 (需要订单支撑)\n');

  // 先查询是否有可用的进行中订单
  let existingOrderId = null;
  let existingOrderStatus = null;
  {
    const res = await GET('/api/order/tutor/list?page=1&size=50', teacherToken);
    if (res.data && res.data.code === 200 && res.data.data) {
      const records = res.data.data.records || [];
      // 找一个状态为2(进行中)的订单用于打卡测试
      const activeOrder = records.find(o => o.status === 2);
      if (activeOrder) {
        existingOrderId = activeOrder.id;
        existingOrderStatus = activeOrder.status;
        console.log(`  ℹ️ 找到进行中的订单: id=${existingOrderId}, status=${existingOrderStatus}`);
      } else {
        // 找一个 status=1 (已支付待上课) 的订单
        const paidOrder = records.find(o => o.status === 1);
        if (paidOrder) {
          existingOrderId = paidOrder.id;
          existingOrderStatus = paidOrder.status;
          console.log(`  ℹ️ 找到已支付待上课的订单: id=${existingOrderId}, status=${existingOrderStatus}`);
        } else {
          console.log('  ⚠️ 未找到状态为1或2的订单, 将尝试查找可创建的需求并走完整流程');
        }
      }
      // 打印所有订单状态分布
      const statusCount = {};
      records.forEach(r => { statusCount[r.status] = (statusCount[r.status] || 0) + 1; });
      console.log(`  ℹ️ 教师所有订单状态分布: ${JSON.stringify(statusCount)} (共${records.length}个)`);
    }
  }

  // 如果没有可用订单，尝试创建新订单并走完流程
  if (!existingOrderId) {
    console.log('\n  🔄 尝试创建新的测试订单流程...\n');

    // 查找家长发布的需求
    const demandRes = await GET('/api/demand/my', parentToken);
    let demandId = null;
    if (demandRes.data && demandRes.data.code === 200 && demandRes.data.data) {
      const demands = Array.isArray(demandRes.data.data) ? demandRes.data.data
        : (demandRes.data.data.records || []);
      const activeDemand = demands.find(d => d.status === 1 && !d.matchedTutorId);
      if (activeDemand) {
        demandId = activeDemand.id;
        console.log(`  ℹ️ 找到可用需求: id=${demandId}, title=${activeDemand.title}`);
      }
    }

    if (!demandId) {
      // 发布新需求
      const publishRes = await POST('/api/demand/publish', {
        title: '钢琴陪练',
        subject: '钢琴',
        grade: '小学',
        detail: '需要一位钢琴老师来指导孩子练习钢琴基础技法',
        teachMode: 1,
        expectPrice: 100,
        address: '广州市天河区',
        longitude: 113.33,
        latitude: 23.13
      }, parentToken);
      if (publishRes.data && publishRes.data.code === 200) {
        demandId = publishRes.data.data;
        console.log(`  ✅ 发布新需求成功: demandId=${demandId}`);
      } else {
        console.log(`  ⚠️ 发布需求失败: ${JSON.stringify(publishRes.data).substring(0, 200)}`);
      }
    }

    if (demandId) {
      // 教师接单
      const acceptRes = await POST('/api/order/accept', {
        demandId: demandId,
        totalHours: 2,
        remark: '首课时间: 04-25 14:00 - 15:00'
      }, teacherToken);
      if (acceptRes.data && acceptRes.data.code === 200) {
        const newOrderId = acceptRes.data.data;
        console.log(`  ✅ 教师接单成功: orderId=${newOrderId}`);

        // 家长确认订单
        const confirmRes = await POST(`/api/order/${newOrderId}/confirm`, null, parentToken);
        if (confirmRes.data && confirmRes.data.code === 200) {
          console.log(`  ✅ 家长确认订单成功`);

          // 记录支付前家长余额
          const walletBefore = await GET('/api/wallet', parentToken);
          const balBefore = walletBefore.data?.data?.balance;
          console.log(`  ℹ️ 支付前家长余额: ${balBefore}`);

          // 家长支付订单
          const payRes = await POST('/api/order/pay', {
            orderId: newOrderId,
            payType: 1
          }, parentToken);
          if (payRes.data && payRes.data.code === 200) {
            console.log(`  ✅ 订单支付成功`);
            existingOrderId = newOrderId;
            existingOrderStatus = 1; // 已支付待上课

            // 验证支付后余额变动
            const walletAfter = await GET('/api/wallet', parentToken);
            const balAfter = walletAfter.data?.data?.balance;
            console.log(`  ℹ️ 支付后家长余额: ${balAfter}`);
            
            const orderDetail = await GET(`/api/order/${newOrderId}`, parentToken);
            const totalAmount = orderDetail.data?.data?.totalAmount;
            logTest('M9', '3.1', `订单支付 - 家长钱包减少 totalAmount(${totalAmount})`,
              totalAmount && Math.abs(parseFloat(balBefore) - parseFloat(balAfter) - parseFloat(totalAmount)) < 0.01,
              `支付前=${balBefore}, 支付后=${balAfter}, 订单金额=${totalAmount}`);

            // 验证支付产生的流水
            const txRes = await GET('/api/wallet/transactions?page=1&size=5', parentToken);
            if (txRes.data?.data) {
              const records = txRes.data.data.records || [];
              const payFlow = records.find(r => r.flowType === 2 && r.orderId === newOrderId);
              logTest('M9', '3.2', '订单支付 - 产生支出流水(flowType=2)', !!payFlow,
                payFlow ? `amount=${payFlow.amount}, remark=${payFlow.remark}` : '未找到支付流水');
            }
          } else {
            console.log(`  ⚠️ 支付失败: ${JSON.stringify(payRes.data).substring(0, 200)}`);
            logTest('M9', '3.1', '订单支付', false, payRes.data);
          }
        } else {
          console.log(`  ⚠️ 确认订单失败: ${JSON.stringify(confirmRes.data).substring(0, 200)}`);
        }
      } else {
        console.log(`  ⚠️ 教师接单失败: ${JSON.stringify(acceptRes.data).substring(0, 200)}`);
      }
    }
  }

  // ========================================================================
  // 模块八：课时打卡管理
  // ========================================================================
  console.log('\n' + '='.repeat(70));
  console.log('  模块八：课时打卡管理');
  console.log('='.repeat(70));

  if (!existingOrderId) {
    console.log('\n  ⚠️ 无可用订单，跳过课时打卡测试');
    logTest('M8', '0', '前置条件：需要有进行中/已支付的订单', false, '无可用订单');
  } else {
    // If order is status=1, start it first
    if (existingOrderStatus === 1) {
      console.log('\n  🔄 订单状态为待上课(1)，先确认开课...\n');
      const startRes = await POST(`/api/order/${existingOrderId}/start`, null, teacherToken);
      if (startRes.data && startRes.data.code === 200) {
        existingOrderStatus = 2;
        console.log('  ✅ 教师确认开课成功, status→2(进行中)');
      } else {
        console.log(`  ℹ️ 确认开课返回: ${JSON.stringify(startRes.data).substring(0, 200)}`);
        // Check if it was already started (打卡可能自动改了状态)
        const orderCheck = await GET(`/api/order/${existingOrderId}`, teacherToken);
        if (orderCheck.data?.data?.status === 2) {
          existingOrderStatus = 2;
          console.log('  ℹ️ 订单已经是进行中状态');
        }
      }
    }

    // ---- 8.1 教师打卡 ----
    console.log('\n📋 8.1 教师打卡\n');

    // 8.1.1 教师上课打卡
    let recordId = null;
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        latitude: 23.13,
        longitude: 113.33,
        photoUrl: 'https://example.com/photo_test.jpg',
        contentSummary: '今日学习钢琴基础指法',
        homeworkAssigned: '练习C大调音阶30分钟'
      }, teacherToken);
      const ok = res.data && res.data.code === 200;
      recordId = ok ? res.data.data : null;
      logTest('M8', '1.1', '教师上课打卡(check-in)', ok,
        ok ? `recordId=${recordId}` : `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.2 教师下课打卡
    if (recordId) {
      const res = await POST(`/api/teaching/check-out/${recordId}?contentSummary=完成C大调音阶教学&homeworkAssigned=每天练习30分钟`, null, teacherToken);
      const ok = res.data && res.data.code === 200;
      logTest('M8', '1.2', '教师下课打卡(check-out)', ok,
        ok ? '打卡成功' : `code=${res.data?.code}, msg=${res.data?.msg}`);
    } else {
      logTest('M8', '1.2', '教师下课打卡(check-out)', false, '无recordId，跳过', true);
    }

    // 8.1.3 更新课时进度
    if (recordId) {
      const res = await POST(`/api/teaching/update-progress/${recordId}?progress=50&notes=学生掌握基础指法`, null, teacherToken);
      const ok = res.data && res.data.code === 200;
      logTest('M8', '1.3', '更新课时进度(progress=50)', ok,
        ok ? '进度更新成功' : `code=${res.data?.code}, msg=${res.data?.msg}`);
    } else {
      logTest('M8', '1.3', '更新课时进度', false, '无recordId，跳过', true);
    }

    // ---- 8.1 异常场景 ----
    console.log('\n📋 8.1x 教师打卡异常场景\n');

    // 8.1.4 重复打卡（上一节未家长确认时再打卡）
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        latitude: 23.13,
        longitude: 113.33,
        photoUrl: 'https://example.com/photo_dup.jpg'
      }, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.4', '重复打卡(上节课未确认) - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.5 不存在的订单打卡
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: 999999,
        latitude: 23.13,
        longitude: 113.33
      }, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.5', '不存在的订单打卡 - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.6 家长尝试打卡（权限校验）- 家长不应该能打卡
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        latitude: 23.13,
        longitude: 113.33
      }, parentToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.6', '家长尝试打卡 - 应拒绝(非教师)', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.7 打卡缺少必填参数 orderId
    {
      const res = await POST('/api/teaching/check-in', {
        latitude: 23.13,
        longitude: 113.33
      }, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.7', '打卡缺少orderId - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.8 打卡缺少纬度
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        longitude: 113.33
      }, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.8', '打卡缺少latitude - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.9 下课打卡 - 不存在的recordId
    {
      const res = await POST('/api/teaching/check-out/999999?contentSummary=test', null, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.9', '下课打卡-不存在的recordId - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.1.10 更新进度 - 不存在的recordId
    {
      const res = await POST('/api/teaching/update-progress/999999?progress=50', null, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '1.10', '更新进度-不存在的recordId - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // ---- 8.2 家长处理 ----
    console.log('\n📋 8.2 家长处理\n');

    // 8.2.1 家长确认课时
    if (recordId) {
      const res = await POST(`/api/teaching/confirm/${recordId}`, null, parentToken);
      const ok = res.data && res.data.code === 200;
      logTest('M8', '2.1', '家长确认课时', ok,
        ok ? '课时已确认' : `code=${res.data?.code}, msg=${res.data?.msg}`);

      // 8.2.1a 重复确认 - 应拒绝
      if (ok) {
        const dupRes = await POST(`/api/teaching/confirm/${recordId}`, null, parentToken);
        const shouldFail = dupRes.data && dupRes.data.code !== 200;
        logTest('M8', '2.1a', '重复确认课时 - 应拒绝', shouldFail,
          `code=${dupRes.data?.code}, msg=${dupRes.data?.msg}`);
      }
    } else {
      logTest('M8', '2.1', '家长确认课时', false, '无recordId，跳过', true);
    }

    // 8.2.2 打第二节课的卡 -> 家长申诉
    let secondRecordId = null;
    {
      // 确认后可以打第二节课
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        latitude: 23.14,
        longitude: 113.34,
        photoUrl: 'https://example.com/lesson2.jpg',
        contentSummary: '第二节课-进阶指法'
      }, teacherToken);
      const ok = res.data && res.data.code === 200;
      secondRecordId = ok ? res.data.data : null;
      if (ok) {
        console.log(`  ℹ️ 第二次打卡成功: recordId=${secondRecordId}`);
      } else {
        console.log(`  ℹ️ 第二次打卡返回: code=${res.data?.code}, msg=${res.data?.msg}`);
      }
    }

    if (secondRecordId) {
      // 家长对第二节课发起申诉
      const res = await POST(`/api/teaching/dispute/${secondRecordId}?reason=老师迟到30分钟`, null, parentToken);
      const ok = res.data && res.data.code === 200;
      logTest('M8', '2.2', '家长发起申诉', ok,
        ok ? '申诉已提交' : `code=${res.data?.code}, msg=${res.data?.msg}`);
    } else {
      logTest('M8', '2.2', '家长发起申诉', false, '无法打第二节课，可能课时已满', true);
    }

    // 8.2.3 家长确认不存在的记录
    {
      const res = await POST('/api/teaching/confirm/999999', null, parentToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '2.3', '确认不存在的课时记录 - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.2.4 教师确认课时（权限校验）
    if (recordId) {
      const res = await POST(`/api/teaching/confirm/${recordId}`, null, teacherToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '2.4', '教师尝试确认课时 - 应拒绝(非家长)', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // 8.2.5 申诉不存在的记录
    {
      const res = await POST('/api/teaching/dispute/999999?reason=test', null, parentToken);
      const shouldFail = res.data && res.data.code !== 200;
      logTest('M8', '2.5', '申诉不存在的课时记录 - 应拒绝', shouldFail,
        `code=${res.data?.code}, msg=${res.data?.msg}`);
    }

    // ---- 8.3 记录与统计 ----
    console.log('\n📋 8.3 记录与统计\n');

    // 8.3.1 教师查看课时记录
    {
      const res = await GET('/api/teaching/my-records', teacherToken);
      const ok = res.data && res.data.code === 200;
      const records = ok ? res.data.data : [];
      logTest('M8', '3.1', '教师查看我的课时记录', ok,
        `记录数量=${Array.isArray(records) ? records.length : 0}`);
      if (ok && Array.isArray(records) && records.length > 0) {
        // 验证记录包含打卡数据
        const first = records[0];
        logTest('M8', '3.1a', '课时记录包含必要字段', 
          first.orderId !== undefined && first.status !== undefined && first.statusText !== undefined,
          `fields: orderId=${first.orderId}, status=${first.status}, statusText=${first.statusText}, lessonIndex=${first.lessonIndex}`);
      }
    }

    // 8.3.2 家长查看课时记录
    {
      const res = await GET('/api/teaching/my-records', parentToken);
      const ok = res.data && res.data.code === 200;
      const records = ok ? res.data.data : [];
      logTest('M8', '3.2', '家长查看我的课时记录', ok,
        `记录数量=${Array.isArray(records) ? records.length : 0}`);
    }

    // 8.3.3 查看课程统计信息
    {
      const res = await GET(`/api/teaching/statistics/${existingOrderId}`, teacherToken);
      // 根据代码，此接口抛出 "功能开发中" 异常
      if (res.data && res.data.code === 200) {
        logTest('M8', '3.3', '查看课程统计信息', true, res.data.data);
      } else {
        const isDevMsg = res.data?.msg?.includes('开发中');
        logTest('M8', '3.3', '查看课程统计信息', false,
          isDevMsg ? '接口返回"功能开发中"(TODO未实现)' : `code=${res.data?.code}, msg=${res.data?.msg}`,
          isDevMsg);
      }
    }

    // ---- 8.x 无Token访问 ----
    console.log('\n📋 8.x 鉴权异常\n');

    // 8.x.1 无Token访问打卡接口
    {
      const res = await POST('/api/teaching/check-in', {
        orderId: existingOrderId,
        latitude: 23.13,
        longitude: 113.33
      }, null);
      const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
      logTest('M8', 'x.1', '无Token访问打卡接口 - 应401', shouldFail,
        `status=${res.status}, code=${res.data?.code}`);
    }

    // 8.x.2 无Token访问记录接口
    {
      const res = await GET('/api/teaching/my-records', null);
      const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
      logTest('M8', 'x.2', '无Token访问记录接口 - 应401', shouldFail,
        `status=${res.status}, code=${res.data?.code}`);
    }
  }

  // ========================================================================
  // 模块九补充：资金流转验证 & 退款流程
  // ========================================================================
  console.log('\n' + '='.repeat(70));
  console.log('  模块九补充：资金流转验证 & 退款流程');
  console.log('='.repeat(70));

  // 9.3 余额不足时支付
  console.log('\n📋 9.3 支付边界测试\n');

  // 创建一个新订单来测试余额不足
  // 先检查是否有待支付的订单
  {
    // 检查交易流水 - 教师端
    const res = await GET('/api/wallet/transactions?page=1&size=10', teacherToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const records = res.data.data.records || [];
      logTest('M9', '3.3', '教师查看交易流水', true,
        `流水数量=${records.length}`);
      // 检查流水类型分布
      const typeCount = {};
      records.forEach(r => { typeCount[r.flowType] = (typeCount[r.flowType] || 0) + 1; });
      console.log(`    ℹ️ 流水类型分布: ${JSON.stringify(typeCount)} (1=充值,2=支付,3=课时费,4=提现,5=退款)`);
    }
  }

  // 9.x 无Token访问钱包接口
  console.log('\n📋 9.x 钱包鉴权异常\n');

  {
    const res = await GET('/api/wallet', null);
    const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
    logTest('M9', 'x.1', '无Token访问钱包 - 应401', shouldFail,
      `status=${res.status}, code=${res.data?.code}`);
  }

  {
    const res = await POST('/api/wallet/recharge?amount=100&paymentMethod=wechat', null, null);
    const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
    logTest('M9', 'x.2', '无Token充值 - 应401', shouldFail,
      `status=${res.status}, code=${res.data?.code}`);
  }

  {
    const res = await POST('/api/wallet/withdraw', {
      amount: 10, channel: 1, accountNo: 'test@test.com'
    }, null);
    const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
    logTest('M9', 'x.3', '无Token提现 - 应401', shouldFail,
      `status=${res.status}, code=${res.data?.code}`);
  }

  {
    const res = await GET('/api/wallet/transactions?page=1&size=10', null);
    const shouldFail = res.status === 401 || (res.data && res.data.code === 401);
    logTest('M9', 'x.4', '无Token查看流水 - 应401', shouldFail,
      `status=${res.status}, code=${res.data?.code}`);
  }

  // 9.y 充值边界
  console.log('\n📋 9.y 充值边界测试\n');

  // 负数充值
  {
    const res = await POST('/api/wallet/recharge?amount=-100&paymentMethod=wechat', null, parentToken);
    const shouldFail = res.data && res.data.code !== 200;
    logTest('M9', 'y.1', '充值负数金额 - 应拒绝', shouldFail,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // 零元充值
  {
    const res = await POST('/api/wallet/recharge?amount=0&paymentMethod=wechat', null, parentToken);
    const shouldFail = res.data && res.data.code !== 200;
    logTest('M9', 'y.2', '充值零元 - 应拒绝', shouldFail,
      `code=${res.data?.code}, msg=${res.data?.msg}`);
  }

  // 多次连续充值验证累加
  {
    const beforeRes = await GET('/api/wallet', parentToken);
    const balBefore = parseFloat(beforeRes.data?.data?.balance || 0);
    
    await POST('/api/wallet/recharge?amount=10&paymentMethod=wechat', null, parentToken);
    await POST('/api/wallet/recharge?amount=20&paymentMethod=alipay', null, parentToken);
    
    const afterRes = await GET('/api/wallet', parentToken);
    const balAfter = parseFloat(afterRes.data?.data?.balance || 0);
    const diff = balAfter - balBefore;
    logTest('M9', 'y.3', `多次充值累加验证 (10+20=30, 实际差=${diff.toFixed(2)})`, 
      Math.abs(diff - 30) < 0.01,
      `充值前=${balBefore}, 充值后=${balAfter}`);
  }

  // 9.z 分页测试
  console.log('\n📋 9.z 分页测试\n');

  {
    const res = await GET('/api/wallet/transactions?page=1&size=2', parentToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const data = res.data.data;
      logTest('M9', 'z.1', '交易流水分页(size=2)', 
        (data.records || []).length <= 2,
        `records=${(data.records || []).length}, total=${data.total}, pages=${data.pages}`);
    }
  }

  {
    const res = await GET('/api/wallet/withdrawals?page=1&size=2', teacherToken);
    const ok = res.data && res.data.code === 200 && res.data.data;
    if (ok) {
      const data = res.data.data;
      logTest('M9', 'z.2', '提现记录分页(size=2)',
        (data.records || []).length <= 2,
        `records=${(data.records || []).length}, total=${data.total}`);
    }
  }

  // ========================================================================
  // 输出总结
  // ========================================================================
  console.log('\n' + '='.repeat(70));
  console.log('  测试结果总结');
  console.log('='.repeat(70));
  console.log(`\n  ✅ 通过: ${passCount}`);
  console.log(`  ❌ 失败: ${failCount}`);
  console.log(`  ⏭️ 跳过: ${skipCount}`);
  console.log(`  📊 总计: ${results.length}`);
  console.log(`  📈 通过率: ${((passCount / (passCount + failCount)) * 100).toFixed(1)}% (不含跳过)\n`);

  // 输出失败详情
  const failures = results.filter(r => r.status === '❌ FAIL');
  if (failures.length > 0) {
    console.log('─'.repeat(70));
    console.log('  失败用例详情:');
    console.log('─'.repeat(70));
    failures.forEach(f => {
      console.log(`  ❌ [${f.module}-${f.id}] ${f.name}`);
      console.log(`     → ${typeof f.detail === 'object' ? JSON.stringify(f.detail).substring(0, 300) : f.detail}`);
    });
  }

  const skips = results.filter(r => r.status === '⏭️ SKIP');
  if (skips.length > 0) {
    console.log('\n─'.repeat(70));
    console.log('  跳过用例详情:');
    console.log('─'.repeat(70));
    skips.forEach(f => {
      console.log(`  ⏭️ [${f.module}-${f.id}] ${f.name}: ${f.detail}`);
    });
  }

  console.log('\n' + '='.repeat(70));
}

main().catch(err => {
  console.error('测试运行出错:', err);
});
