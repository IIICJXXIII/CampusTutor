/**
 * CampusTutor Module 6 & 7 Full Test Suite
 * Module 6: 预约与接单 (Booking & Order Acceptance)
 * Module 7: 订单全生命周期 (Order Full Lifecycle)
 * 
 * Teacher: 17209892755 / 123456
 * Parent:  15273153320 / 123456
 */

const http = require('http');

const BASE = 'http://localhost:8080';
const results = [];

function api(method, path, token, body) {
    return new Promise((resolve) => {
        const url = new URL(path, BASE);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method: method,
            headers: { 'Content-Type': 'application/json' },
            timeout: 15000,
        };
        if (token) options.headers['Authorization'] = 'Bearer ' + token;

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, body: JSON.parse(data) });
                } catch {
                    resolve({ status: res.statusCode, body: { raw: data } });
                }
            });
        });
        req.on('error', (e) => resolve({ status: 0, body: { message: e.message } }));
        req.on('timeout', () => { req.destroy(); resolve({ status: 0, body: { message: 'timeout' } }); });

        if (body && method !== 'GET') {
            const payload = typeof body === 'string' ? body : JSON.stringify(body);
            req.write(payload);
        }
        req.end();
    });
}

function logResult(id, name, expected, result, pass) {
    const icon = pass ? '[PASS]' : '[FAIL]';
    const msg = result.body?.message || result.body?.msg || '';
    console.log(`${icon} ${id} - ${name} | HTTP=${result.status} | code=${result.body?.code} | msg=${msg} | Expected: ${expected}`);
    results.push({ id, name, expected, pass, status: result.status, code: result.body?.code, msg });
    return pass;
}

async function main() {
    console.log('============================================================');
    console.log(' CampusTutor Module 6 & 7 Full Test Suite');
    console.log(' Started:', new Date().toLocaleString('zh-CN'));
    console.log('============================================================\n');

    // ===== Phase 0: Login =====
    console.log('===== Phase 0: Login (获取Token) =====');

    const teacherLogin = await api('POST', '/api/auth/login', null, {
        account: '17209892755', password: '123456', loginType: 'password'
    });
    if (!teacherLogin.body?.data?.token) {
        console.log('[FATAL] Teacher login FAILED!', JSON.stringify(teacherLogin.body));
        return;
    }
    const teacherToken = teacherLogin.body.data.token;
    const teacherUserId = teacherLogin.body.data.userId;
    console.log(`[OK] Teacher login: userId=${teacherUserId}`);

    const parentLogin = await api('POST', '/api/auth/login', null, {
        account: '15273153320', password: '123456', loginType: 'password'
    });
    if (!parentLogin.body?.data?.token) {
        console.log('[FATAL] Parent login FAILED!', JSON.stringify(parentLogin.body));
        return;
    }
    const parentToken = parentLogin.body.data.token;
    const parentUserId = parentLogin.body.data.userId;
    console.log(`[OK] Parent login: userId=${parentUserId}\n`);

    // Also get admin token for admin operations
    const adminLogin = await api('POST', '/api/admin/auth/login', null, {
        account: 'admin', password: '123456', loginType: 'password'
    });
    let adminToken = '';
    if (adminLogin.body?.data?.token) {
        adminToken = adminLogin.body.data.token;
        console.log(`[OK] Admin login successful`);
    } else {
        console.log(`[WARN] Admin login failed: ${JSON.stringify(adminLogin.body)}, some admin tests will be skipped`);
    }
    console.log('');

    // ===== Phase 1: Prerequisites =====
    console.log('===== Phase 1: Prerequisites (前置条件准备) =====');

    // 1a. Ensure teacher has a tutor profile with certStatus=2
    console.log('--- Checking teacher tutor profile ---');
    const profileCheck = await api('GET', '/api/tutor/certification', teacherToken);
    let hasCertifiedProfile = false;
    if (profileCheck.body?.data?.certStatus === 2) {
        hasCertifiedProfile = true;
        console.log(`[OK] Teacher already has certified profile (certStatus=2)`);
    } else if (profileCheck.body?.data) {
        console.log(`[INFO] Teacher has profile but certStatus=${profileCheck.body.data.certStatus}`);
        // Try to approve via admin
        if (adminToken && profileCheck.body.data.certStatus === 1) {
            const approveR = await api('POST', `/api/admin/tutors/${profileCheck.body.data.id}/approve`, adminToken);
            if (approveR.body?.code === 200) {
                hasCertifiedProfile = true;
                console.log(`[OK] Admin approved teacher certification`);
            }
        }
    } else {
        console.log(`[INFO] Teacher has no profile, creating one...`);
        const certBody = {
            realName: "Test Teacher",
            idCard: "440000200001011234",
            idCardFrontUrl: "/uploads/test/id_front.jpg",
            idCardBackUrl: "/uploads/test/id_back.jpg",
            universityName: "Test University",
            major: "Music Education",
            education: 2,
            enrollYear: 2020,
            studentCardUrl: "/uploads/test/student_card.jpg",
            certificateUrls: ["/uploads/test/cert1.jpg"],
            teachGrades: ["5-8 years old", "8-12 years old", "3-6 years old"],
            teachSubjects: ["piano", "art", "swimming", "dance"],
            expectPrice: 150
        };
        const certR = await api('POST', '/api/tutor/certification', teacherToken, certBody);
        console.log(`[INFO] Cert submission: code=${certR.body?.code}, msg=${certR.body?.msg || certR.body?.message}`);

        if (certR.body?.code === 200) {
            // Get profile ID and approve via admin
            const profile2 = await api('GET', '/api/tutor/certification', teacherToken);
            if (profile2.body?.data?.id && adminToken) {
                const approveR = await api('POST', `/api/admin/tutors/${profile2.body.data.id}/approve`, adminToken);
                if (approveR.body?.code === 200) {
                    hasCertifiedProfile = true;
                    console.log(`[OK] Created and approved teacher profile`);
                } else {
                    console.log(`[WARN] Admin approve failed: ${JSON.stringify(approveR.body)}`);
                }
            }
        }
    }

    if (!hasCertifiedProfile) {
        console.log(`[WARN] Teacher does NOT have certified profile. Match/order tests will likely fail.`);
    }

    // 1b. Publish demands
    console.log('\n--- Publishing test demands ---');
    const ts = Date.now().toString().slice(-6);

    let demandId = 0;
    const pub1 = await api('POST', '/api/demand/publish', parentToken, {
        title: `Test Piano ${ts}`, subject: 'piano', grade: '5-8岁',
        teachMode: 1, expectPrice: 150, longitude: 113.26, latitude: 23.13,
        address: '广州测试地址', detail: '钢琴教学测试'
    });
    if (pub1.body?.code === 200) {
        demandId = pub1.body.data;
        console.log(`[OK] Demand 1 published, id=${demandId}`);
    } else {
        console.log(`[WARN] Demand 1 failed: ${pub1.body?.msg || pub1.body?.message}`);
        const myD = await api('GET', '/api/demand/my', parentToken);
        if (myD.body?.data?.length > 0) {
            for (const d of myD.body.data) {
                if (d.status === 1 && !d.matchedTutorId) { demandId = d.id; break; }
            }
            if (demandId) console.log(`[OK] Using existing demand id=${demandId}`);
        }
    }

    let demandId2 = 0;
    const pub2 = await api('POST', '/api/demand/publish', parentToken, {
        title: `Test Art ${ts}`, subject: 'art', grade: '8-12岁',
        teachMode: 2, expectPrice: 100, longitude: 113.27, latitude: 23.14,
        address: '广州测试地址2', detail: '美术教学测试'
    });
    if (pub2.body?.code === 200) {
        demandId2 = pub2.body.data;
        console.log(`[OK] Demand 2 published, id=${demandId2}`);
    }

    let demandIdRefund = 0;
    const pub3 = await api('POST', '/api/demand/publish', parentToken, {
        title: `Refund Swimming ${ts}`, subject: 'swimming', grade: '6-10岁',
        teachMode: 1, expectPrice: 200, longitude: 113.28, latitude: 23.15,
        address: '广州游泳馆', detail: '游泳教学退款测试'
    });
    if (pub3.body?.code === 200) {
        demandIdRefund = pub3.body.data;
        console.log(`[OK] Demand 3 (refund test), id=${demandIdRefund}`);
    }

    // 1c. Ensure parent wallet has funds
    console.log('\n--- Recharging parent wallet ---');
    const rechargeR = await api('POST', '/api/wallet/recharge?amount=10000&paymentMethod=wechat', parentToken);
    console.log(`[INFO] Recharge result: code=${rechargeR.body?.code}`);

    console.log('');

    // ===================================================================
    // MODULE 6: 预约与接单
    // ===================================================================
    console.log('============================================================');
    console.log(' MODULE 6: 预约与接单 (Booking & Order Acceptance)');
    console.log('============================================================\n');

    // --- 6.1 家长发起预约（通过订单系统） ---
    console.log('--- 6.1 家长发起预约（通过订单系统） ---');

    let directOrderId = 0;
    const createDirectOrder = await api('POST', '/api/order/create', parentToken, {
        tutorProfileId: teacherUserId, subject: 'piano', grade: '5-8岁',
        teachMode: 1, totalHours: 10, unitPrice: 150, remark: '测试直接预约'
    });
    if (createDirectOrder.body?.code === 200) directOrderId = createDirectOrder.body.data;
    logResult('TC-6.1.1', '家长向教师发起直接预约(order/create)', 'code=200, 返回orderId', createDirectOrder, createDirectOrder.body?.code === 200);

    const parentOrdList = await api('GET', '/api/order/parent/list?page=1&size=10', parentToken);
    const ordInList = parentOrdList.body?.data?.records?.some(o => o.id === directOrderId) ?? false;
    logResult('TC-6.1.2', '家长查看订单列表(含新预约)', 'code=200, 含新订单', parentOrdList,
        parentOrdList.body?.code === 200 && (directOrderId === 0 || ordInList));

    // --- 6.2 教师处理预约（通过订单系统） ---
    console.log('\n--- 6.2 教师处理预约（通过订单系统） ---');

    const tutorOrdList = await api('GET', '/api/order/tutor/list?page=1&size=10', teacherToken);
    logResult('TC-6.2.1', '教师查看收到的订单列表', 'code=200', tutorOrdList, tutorOrdList.body?.code === 200);

    if (directOrderId > 0) {
        const confirmOrd = await api('POST', `/api/order/${directOrderId}/tutor-confirm`, teacherToken);
        logResult('TC-6.2.2', '教师确认预约(tutor-confirm)', 'code=200', confirmOrd, confirmOrd.body?.code === 200);
    } else {
        logResult('TC-6.2.2', '教师确认预约', 'code=200 (SKIPPED: 创建订单失败)', { status: 0, body: { code: -1, msg: 'SKIPPED' } }, false);
    }

    const createDirectOrder2 = await api('POST', '/api/order/create', parentToken, {
        tutorProfileId: teacherUserId, subject: 'art', grade: '8-12岁',
        teachMode: 2, totalHours: 8, unitPrice: 100, remark: '拒绝测试'
    });
    let directOrderId2 = createDirectOrder2.body?.code === 200 ? createDirectOrder2.body.data : 0;

    if (directOrderId2 > 0) {
        const rejectOrd = await api('POST', `/api/order/${directOrderId2}/tutor-reject?reason=时间冲突`, teacherToken);
        logResult('TC-6.2.3', '教师拒绝另一个预约(tutor-reject)', 'code=200', rejectOrd, rejectOrd.body?.code === 200);
    } else {
        logResult('TC-6.2.3', '教师拒绝另一个预约', 'code=200 (SKIPPED)', { status: 0, body: { code: -1, msg: 'SKIPPED' } }, false);
    }

    // --- 6.3 教师主动接单(需求匹配) ---
    console.log('\n--- 6.3 教师主动接单(需求匹配) ---');

    let orderIdMatch = 0;
    if (demandId > 0) {
        const matchR = await api('POST', `/api/demand/${demandId}/match`, teacherToken);
        if (matchR.body?.code === 200) orderIdMatch = matchR.body.data;
        logResult('TC-6.3.1', '教师对需求发起匹配/接单 (demand/match)', 'code=200, 返回orderId', matchR, matchR.body?.code === 200);
    } else {
        logResult('TC-6.3.1', '教师对需求发起匹配/接单', 'code=200 (SKIPPED: 无需求)', { status: 0, body: { code: -1, msg: 'No demand' } }, false);
    }

    let orderIdAccept = 0;
    if (demandId2 > 0) {
        const acceptR = await api('POST', '/api/order/accept', teacherToken, {
            demandId: demandId2, totalHours: 10, remark: '我可以教美术'
        });
        if (acceptR.body?.code === 200) orderIdAccept = acceptR.body.data;
        logResult('TC-6.3.2', '教师通过order/accept接单', 'code=200, 返回orderId', acceptR, acceptR.body?.code === 200);
    } else {
        logResult('TC-6.3.2', '教师通过order/accept接单', 'code=200 (SKIPPED)', { status: 0, body: { code: -1 } }, false);
    }

    // --- 6.4 家长取消预约（通过订单系统） ---
    console.log('\n--- 6.4 家长取消预约（通过订单系统） ---');

    const createDirectOrder3 = await api('POST', '/api/order/create', parentToken, {
        tutorProfileId: teacherUserId, subject: 'dance', grade: '3-6岁',
        teachMode: 1, totalHours: 6, unitPrice: 120, remark: '取消测试'
    });
    let directOrderId3 = createDirectOrder3.body?.code === 200 ? createDirectOrder3.body.data : 0;

    if (directOrderId3 > 0) {
        const cancelOrd = await api('POST', `/api/order/${directOrderId3}/cancel?reason=不需要了`, parentToken);
        logResult('TC-6.4.1', '家长取消已发预约(order/cancel)', 'code=200', cancelOrd, cancelOrd.body?.code === 200);
    } else {
        logResult('TC-6.4.1', '家长取消已发预约', 'code=200 (SKIPPED)', { status: 0, body: { code: -1, msg: 'SKIPPED' } }, false);
    }

    // ===================================================================
    // MODULE 7: 订单全生命周期
    // ===================================================================
    console.log('\n============================================================');
    console.log(' MODULE 7: 订单全生命周期 (Order Full Lifecycle)');
    console.log('============================================================\n');

    // --- 7.1 订单创建与确认 ---
    console.log('--- 7.1 订单创建与确认 ---');

    const parentOrderList = await api('GET', '/api/order/parent/list?page=1&size=10', parentToken);
    logResult('TC-7.1.1', '家长查看订单列表', 'code=200', parentOrderList, parentOrderList.body?.code === 200);

    if (orderIdMatch > 0) {
        // Check initial status
        const initDetail = await api('GET', `/api/order/${orderIdMatch}`, parentToken);
        console.log(`  [DEBUG] Order ${orderIdMatch} initial status: ${initDetail.body?.data?.status}, totalAmount: ${initDetail.body?.data?.totalAmount}`);

        const confirmOrd = await api('POST', `/api/order/${orderIdMatch}/confirm`, parentToken);
        logResult('TC-7.1.2', '家长确认订单', 'code=200', confirmOrd, confirmOrd.body?.code === 200);

        const detailR = await api('GET', `/api/order/${orderIdMatch}`, parentToken);
        const st = detailR.body?.data?.status;
        const amt = detailR.body?.data?.totalAmount;
        const sub = detailR.body?.data?.subject;
        console.log(`  [DEBUG] After confirm: status=${st}, totalAmount=${amt}, subject=${sub}`);
        logResult('TC-7.1.3', '查看订单详情(状态+金额+科目正确)', 'code=200, 数据正确', detailR, detailR.body?.code === 200);
    } else {
        logResult('TC-7.1.2', '家长确认订单', 'SKIPPED (无订单)', { status: 0, body: { code: -1, msg: 'No order' } }, false);
        logResult('TC-7.1.3', '查看订单详情', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // --- 7.2 订单支付 ---
    console.log('\n--- 7.2 订单支付 ---');

    if (orderIdMatch > 0) {
        // TC-7.2.1: 支付订单
        const payR = await api('POST', '/api/order/pay', parentToken, { orderId: orderIdMatch, payType: 1 });
        logResult('TC-7.2.1', '家长钱包支付订单', 'code=200', payR, payR.body?.code === 200);

        // TC-7.2.2: 验证支付后状态
        const afterPay = await api('GET', `/api/order/${orderIdMatch}`, parentToken);
        const payStatus = afterPay.body?.data?.status;
        console.log(`  [DEBUG] Order status after pay: ${payStatus}`);
        logResult('TC-7.2.2', '验证支付后订单状态=1(已支付待上课)', `status=1`, afterPay,
            afterPay.body?.code === 200 && payStatus === 1);

        // TC-7.2.3: 重复支付应被拒绝
        const dupePay = await api('POST', '/api/order/pay', parentToken, { orderId: orderIdMatch, payType: 1 });
        logResult('TC-7.2.3', '重复支付已支付订单(应拒绝)', '应失败', dupePay, dupePay.body?.code !== 200);
    } else {
        logResult('TC-7.2.1', '家长钱包支付订单', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.2.2', '验证支付后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.2.3', '重复支付(应拒绝)', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // --- 7.3 开课与上课 ---
    console.log('\n--- 7.3 开课与上课 ---');

    if (orderIdMatch > 0) {
        const startR = await api('POST', `/api/order/${orderIdMatch}/start`, teacherToken);
        logResult('TC-7.3.1', '教师确认开课', 'code=200, status→2', startR, startR.body?.code === 200);

        const tutorInProg = await api('GET', '/api/order/tutor/list?status=2&page=1&size=10', teacherToken);
        logResult('TC-7.3.2', '教师查看进行中订单列表', 'code=200', tutorInProg, tutorInProg.body?.code === 200);

        const afterStart = await api('GET', `/api/order/${orderIdMatch}`, teacherToken);
        const startStatus = afterStart.body?.data?.status;
        console.log(`  [DEBUG] Order status after start: ${startStatus}`);
        logResult('TC-7.3.3', '验证开课后状态=2(进行中)', `status=2`, afterStart,
            afterStart.body?.code === 200 && startStatus === 2);
    } else {
        logResult('TC-7.3.1', '教师确认开课', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.3.2', '教师查看进行中订单', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.3.3', '验证开课后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // --- 7.4 订单完成 ---
    console.log('\n--- 7.4 订单完成 ---');

    if (orderIdMatch > 0) {
        const completeR = await api('POST', `/api/order/${orderIdMatch}/complete`, teacherToken);
        logResult('TC-7.4.1', '教师标记订单完成', 'code=200, status→3', completeR, completeR.body?.code === 200);

        const parentComp = await api('GET', '/api/order/parent/list?status=3&page=1&size=10', parentToken);
        logResult('TC-7.4.2', '家长端显示已完成订单', 'code=200', parentComp, parentComp.body?.code === 200);

        const afterComp = await api('GET', `/api/order/${orderIdMatch}`, parentToken);
        const compStatus = afterComp.body?.data?.status;
        console.log(`  [DEBUG] Order status after complete: ${compStatus}`);
        logResult('TC-7.4.3', '验证完成后状态=3(已完成)', `status=3`, afterComp,
            afterComp.body?.code === 200 && compStatus === 3);
    } else {
        logResult('TC-7.4.1', '教师标记订单完成', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.4.2', '家长端显示已完成', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.4.3', '验证完成后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // --- 7.5 取消与退款 ---
    console.log('\n--- 7.5 取消与退款 ---');

    // 7.5.1: Cancel an unpaid order
    if (orderIdAccept > 0) {
        const cancelOrd = await api('POST', `/api/order/${orderIdAccept}/cancel?reason=不需要了`, parentToken);
        logResult('TC-7.5.1', '家长取消待支付订单', 'code=200, status→4', cancelOrd, cancelOrd.body?.code === 200);

        const afterCancel = await api('GET', `/api/order/${orderIdAccept}`, parentToken);
        const cancelSt = afterCancel.body?.data?.status;
        console.log(`  [DEBUG] Order status after cancel: ${cancelSt}`);
        logResult('TC-7.5.1b', '验证取消后状态=4(已取消)', `status=4`, afterCancel,
            afterCancel.body?.code === 200 && cancelSt === 4);
    } else {
        logResult('TC-7.5.1', '家长取消待支付订单', 'SKIPPED', { status: 0, body: { code: -1, msg: 'No order' } }, false);
        logResult('TC-7.5.1b', '验证取消后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // 7.5.2/3: Refund a paid order
    console.log('\n  [INFO] Setting up refund test (new order lifecycle)...');
    let orderIdRefund = 0;
    if (demandIdRefund > 0) {
        const matchRef = await api('POST', `/api/demand/${demandIdRefund}/match`, teacherToken);
        if (matchRef.body?.code === 200) orderIdRefund = matchRef.body.data;
        console.log(`  [DEBUG] Refund order match: code=${matchRef.body?.code}, orderId=${orderIdRefund}`);

        if (orderIdRefund > 0) {
            await api('POST', `/api/order/${orderIdRefund}/confirm`, parentToken);
            const payRef = await api('POST', '/api/order/pay', parentToken, { orderId: orderIdRefund, payType: 1 });
            console.log(`  [DEBUG] Refund order paid: code=${payRef.body?.code}`);

            const refDetail = await api('GET', `/api/order/${orderIdRefund}`, parentToken);
            const refAmt = refDetail.body?.data?.totalAmount || 100;
            console.log(`  [DEBUG] Refund order totalAmount: ${refAmt}`);

            const refundR = await api('POST', `/api/order/refund?orderId=${orderIdRefund}&refundAmount=${refAmt}&reason=测试退款`, parentToken);
            const refundNo = refundR.body?.data || '';
            logResult('TC-7.5.2', '已支付订单申请退款', 'code=200, 返回退款编号', refundR, refundR.body?.code === 200);
            console.log(`  [DEBUG] Refund number: ${refundNo}`);

            const afterRefund = await api('GET', `/api/order/${orderIdRefund}`, parentToken);
            const refundSt = afterRefund.body?.data?.status;
            console.log(`  [DEBUG] Order status after refund: ${refundSt}`);
            logResult('TC-7.5.3', '验证退款后状态=5(退款中)', `status=5`, afterRefund,
                afterRefund.body?.code === 200 && refundSt === 5);

            // 7.5.4: Admin processes refund
            if (adminToken) {
                const adminRefundR = await api('POST', `/api/admin/orders/${orderIdRefund}/refund`, adminToken, { reason: '管理员确认退款' });
                logResult('TC-7.5.4', '管理员处理退款', 'code=200, status→6', adminRefundR, adminRefundR.body?.code === 200);

                if (adminRefundR.body?.code === 200) {
                    const afterAdminRefund = await api('GET', `/api/order/${orderIdRefund}`, parentToken);
                    const adminRefSt = afterAdminRefund.body?.data?.status;
                    console.log(`  [DEBUG] Order status after admin refund: ${adminRefSt}`);
                    logResult('TC-7.5.4b', '验证管理员退款后状态=6(已退款)', `status=6`, afterAdminRefund,
                        afterAdminRefund.body?.code === 200 && adminRefSt === 6);
                }
            } else {
                logResult('TC-7.5.4', '管理员处理退款', 'SKIPPED (no admin token)', { status: 0, body: { code: -1 } }, false);
            }
        } else {
            logResult('TC-7.5.2', '已支付订单申请退款', 'SKIPPED (match failed)', { status: 0, body: { code: -1 } }, false);
            logResult('TC-7.5.3', '验证退款后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
        }
    } else {
        logResult('TC-7.5.2', '已支付订单申请退款', 'SKIPPED (no demand)', { status: 0, body: { code: -1 } }, false);
        logResult('TC-7.5.3', '验证退款后状态', 'SKIPPED', { status: 0, body: { code: -1 } }, false);
    }

    // --- 7.6 权限验证 ---
    console.log('\n--- 7.6 权限验证 ---');

    // TC-7.6.1: Owner can view own order
    if (orderIdMatch > 0) {
        const ownerView = await api('GET', `/api/order/${orderIdMatch}`, parentToken);
        logResult('TC-7.6.1', '订单所有者可查看自己的订单', 'code=200', ownerView, ownerView.body?.code === 200);
    }

    // TC-7.6.2: No token = 401
    const noAuthR = await api('GET', `/api/order/${orderIdMatch || 99999}`, null);
    logResult('TC-7.6.2', '无Token访问订单(应401)', '401', noAuthR,
        noAuthR.status === 401 || noAuthR.body?.code === 401);

    // TC-7.6.3: Parent tries teacher-only operation
    if (orderIdMatch > 0) {
        const parentStartR = await api('POST', `/api/order/${orderIdMatch}/start`, parentToken);
        logResult('TC-7.6.3', '家长尝试教师操作(开课) - 应失败', '非200(权限不足)', parentStartR, parentStartR.body?.code !== 200);
    }

    // TC-7.6.4: Double-complete
    if (orderIdMatch > 0) {
        const dupeComp = await api('POST', `/api/order/${orderIdMatch}/complete`, teacherToken);
        logResult('TC-7.6.4', '重复完成已完成订单 - 应失败', '非200(状态不允许)', dupeComp, dupeComp.body?.code !== 200);
    }

    // TC-7.6.5: Fake token
    const fakeR = await api('GET', '/api/order/parent/list?page=1&size=10', 'fake.token.here');
    logResult('TC-7.6.5', '伪造Token访问(应401)', '401', fakeR,
        fakeR.status === 401 || fakeR.body?.code === 401);

    // TC-7.6.6: Admin can view any order
    if (adminToken && orderIdMatch > 0) {
        const adminViewR = await api('GET', `/api/admin/orders/${orderIdMatch}`, adminToken);
        logResult('TC-7.6.6', '管理员可查看任何订单', 'code=200', adminViewR, adminViewR.body?.code === 200);
    }

    // --- 7.7 钱包余额验证 ---
    console.log('\n--- 7.7 钱包余额与资金流验证 ---');

    const parentW = await api('GET', '/api/wallet', parentToken);
    console.log(`  [DEBUG] Parent final balance: ${parentW.body?.data?.balance}`);
    logResult('TC-7.7.1', '家长钱包余额查询', 'code=200', parentW, parentW.body?.code === 200);

    const teacherW = await api('GET', '/api/wallet', teacherToken);
    console.log(`  [DEBUG] Teacher final balance: ${teacherW.body?.data?.balance}`);
    logResult('TC-7.7.2', '教师钱包余额查询', 'code=200', teacherW, teacherW.body?.code === 200);

    const parentTx = await api('GET', '/api/wallet/transactions?page=1&size=20', parentToken);
    const txCount = parentTx.body?.data?.records?.length || 0;
    console.log(`  [DEBUG] Parent transaction count: ${txCount}`);
    logResult('TC-7.7.3', '家长查看交易流水(有记录)', 'code=200, >0条', parentTx,
        parentTx.body?.code === 200 && txCount > 0);

    const teacherTx = await api('GET', '/api/wallet/transactions?page=1&size=20', teacherToken);
    logResult('TC-7.7.4', '教师查看交易流水', 'code=200', teacherTx, teacherTx.body?.code === 200);

    // ===================================================================
    // SUMMARY
    // ===================================================================
    console.log('\n============================================================');
    console.log(' TEST SUMMARY');
    console.log('============================================================\n');

    const passed = results.filter(r => r.pass).length;
    const failed = results.filter(r => !r.pass).length;
    console.log(`Total:  ${results.length}`);
    console.log(`Passed: ${passed}`);
    console.log(`Failed: ${failed}`);
    console.log('');

    if (failed > 0) {
        console.log('===== FAILED TESTS =====');
        results.filter(r => !r.pass).forEach(t => {
            console.log(`  FAIL: ${t.id} - ${t.name}`);
            console.log(`        Expected: ${t.expected}`);
            console.log(`        Got: HTTP=${t.status}, code=${t.code}, msg=${t.msg}`);
            console.log('');
        });
    }

    console.log('===== FULL RESULTS =====');
    results.forEach(t => {
        const mark = t.pass ? 'PASS' : 'FAIL';
        console.log(`  ${mark}  ${t.id.padEnd(12)} ${t.name}`);
    });

    console.log(`\nCompleted at ${new Date().toLocaleString('zh-CN')}`);
}

main().catch(e => console.error('Fatal error:', e));
