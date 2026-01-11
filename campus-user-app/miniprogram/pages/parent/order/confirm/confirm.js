const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    // 页面接收参数
    tutorId: null,
    tutorName: '',
    unitPrice: 0,
    subject: '',

    // 表单数据
    studentList: [],
    studentIndex: -1,
    grade: '',
    grades: ['小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', '初一', '初二', '初三', '高一', '高二', '高三'],
    gradeIndex: -1,
    teachMode: 1, // 1上门 2在线
    totalHours: 2, // 默认2课时
    remark: '',
    
    // 计算属性
    totalAmount: 0,
    
    // 状态
    isSubmitting: false,
    showPayModal: false,
    createdOrderId: null,
    walletBalance: 0,
    isPaying: false
  },

  onLoad(options) {
    // 1. 解析上个页面传递的 JSON 数据
    if (options.data) {
      try {
        const data = JSON.parse(decodeURIComponent(options.data));
        this.setData({
          tutorId: data.tutorId,
          tutorName: data.realName,
          unitPrice: data.price || 0, // 确保单价不为undefined
          subject: data.subject
        });
        this.calcTotal();
      } catch (e) {
        console.error('参数解析失败', e);
      }
    }

    // 2. 获取当前家长的学生列表
    this.fetchMyStudents();
  },

  // 获取学生列表
  async fetchMyStudents() {
    try {
      // 假设 api.parent.myStudents 对应 /api/parent/student/list
      // 如果没有专门的列表接口，可能需要调整 apiConfig
      const res = await request.get(api.parent.myStudents); 
      // 假设返回的是数组
      this.setData({ studentList: res || [] });
    } catch (err) {
      console.error('获取学生失败', err);
      // Mock数据兜底，方便调试
      this.setData({
        studentList: [
          { id: 101, name: '张小明', grade: '小学三年级' },
          { id: 102, name: '张小红', grade: '初二' }
        ]
      });
    }
  },

  // 选择学生
  handleStudentChange(e) {
    const idx = parseInt(e.detail.value);
    const student = this.data.studentList[idx];
    
    // 自动填充年级
    let gIdx = this.data.grades.indexOf(student.grade);
    
    this.setData({
      studentIndex: idx,
      grade: student.grade || '',
      gradeIndex: gIdx
    });
  },

  // 选择年级
  handleGradeChange(e) {
    const idx = parseInt(e.detail.value);
    this.setData({
      gradeIndex: idx,
      grade: this.data.grades[idx]
    });
  },

  // 选择模式
  selectMode(e) {
    this.setData({ teachMode: parseInt(e.currentTarget.dataset.mode) });
  },

  // 调整课时
  changeHours(e) {
    const delta = parseInt(e.currentTarget.dataset.delta);
    let newHours = this.data.totalHours + delta;
    if (newHours < 1) newHours = 1;
    this.setData({ totalHours: newHours });
    this.calcTotal();
  },

  // 计算总价
  calcTotal() {
    const total = (this.data.unitPrice * this.data.totalHours).toFixed(2);
    this.setData({ totalAmount: total });
  },

  handleInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 提交订单
  async submitOrder() {
    // 校验
    if (this.data.studentIndex === -1) return wx.showToast({ title: '请选择学生', icon: 'none' });
    if (!this.data.grade) return wx.showToast({ title: '请选择年级', icon: 'none' });

    this.setData({ isSubmitting: true });

    try {
      const student = this.data.studentList[this.data.studentIndex];

      // 构造 CreateOrderRequest
      const payload = {
        studentId: student.id,
        tutorProfileId: this.data.tutorId,
        subject: this.data.subject,
        grade: this.data.grade,
        teachMode: this.data.teachMode,
        unitPrice: this.data.unitPrice,
        totalHours: this.data.totalHours,
        remark: this.data.remark
        // demandId: null (直接预约暂不关联需求)
      };

      // 调用 /api/order/create
      const orderId = await request.post(api.order.create, payload);
      
      this.setData({ 
        createdOrderId: orderId,
        showPayModal: true, // 弹出支付层
        isSubmitting: false
      });

      // 拉取钱包余额信息以便支付决策
      try {
        await this.fetchWallet();
      } catch (e) {
        console.warn('获取钱包信息失败', e);
      }

    } catch (err) {
      console.error(err);
      this.setData({ isSubmitting: false });
    }
  },

  closePayModal() {
    this.setData({ showPayModal: false });
    // 未支付但也跳转到列表
    wx.redirectTo({ url: '/pages/parent/order/list/list?status=0' });
  },

  // 拉取钱包信息
  async fetchWallet() {
    try {
      const wallet = await request.get(api.wallet.info);
      this.setData({ walletBalance: wallet && wallet.balance ? Number(wallet.balance) : 0 });
    } catch (err) {
      console.error('fetchWallet error', err);
      this.setData({ walletBalance: 0 });
    }
  },

  // 确认支付
  async confirmPay(e) {
    const payType = parseInt(e.currentTarget.dataset.type);
    if (!this.data.createdOrderId) return wx.showToast({ title: '订单未创建', icon: 'none' });

    // 钱包支付余额校验
    if (payType === 1 && (this.data.walletBalance || 0) < Number(this.data.totalAmount)) {
      wx.showModal({
        title: '余额不足',
        content: '钱包余额不足，是否去充值？',
        confirmText: '去充值',
        cancelText: '取消',
        success(res) {
          if (res.confirm) {
            wx.showToast({ title: '充值页面未实现，请在后台充值', icon: 'none' });
          }
        }
      });
      return;
    }

    if (this.data.isPaying) return;
    this.setData({ isPaying: true });
    wx.showLoading({ title: '支付处理中...' });

    try {
      // 构造 PayOrderRequest
      const payload = {
        orderId: this.data.createdOrderId,
        payType: payType,
        payPassword: '' // 微信支付不需要钱包密码
      };

      // 调用 /api/order/pay
      const payResult = await request.post(api.order.pay, payload);

      // 微信支付处理
      if (payType === 2 && payResult.prepayId) {
        // 调用微信支付SDK
        await this.callWechatPay(payResult);
      } else {
        // 钱包支付直接成功
        wx.hideLoading();
        wx.showToast({ title: '支付成功', icon: 'success' });
        this.setData({ isPaying: false });

        // 跳转到订单列表（已支付 Tab）
        setTimeout(() => {
          wx.reLaunch({ url: '/pages/parent/order/list/list?status=1' });
        }, 1500);
      }

    } catch (err) {
      wx.hideLoading();
      this.setData({ isPaying: false });
      console.error(err);
      wx.showToast({ title: err.msg || '支付失败', icon: 'none' });
    }
  },

  // 调用微信支付
  callWechatPay(payResult) {
    return new Promise((resolve, reject) => {
      wx.requestPayment({
        timeStamp: payResult.timeStamp,
        nonceStr: payResult.nonceStr,
        package: payResult.package,
        signType: payResult.signType,
        paySign: payResult.paySign,
        success: (res) => {
          wx.showToast({ title: '支付成功', icon: 'success' });
          this.setData({ isPaying: false });
          
          // 跳转到订单列表（已支付 Tab）
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/parent/order/list/list?status=1' });
          }, 1500);
          resolve(res);
        },
        fail: (err) => {
          this.setData({ isPaying: false });
          wx.showToast({ title: '支付失败', icon: 'none' });
          reject(err);
        },
        complete: () => {
          this.setData({ isPaying: false });
        }
      });
    });
  }
});