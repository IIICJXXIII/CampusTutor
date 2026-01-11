// 家长端退款申请页面
const api = require('../../../config/apiConfig.js');
const request = require('../../../utils/request.js');

Page({
  data: {
    orderId: null,
    order: null,
    refundType: 1, // 1全额退款，2部分退款
    refundAmount: '',
    refundReason: '',
    refundableAmount: 0,
    isSubmitting: false
  },

  onLoad: function (options) {
    const orderId = options.orderId;
    this.setData({ orderId });
    this.loadOrderInfo(orderId);
  },

  // 加载订单信息
  loadOrderInfo(orderId) {
    request.get(api.order.detail(orderId))
      .then(res => {
        const order = res;
        const usedHours = order.usedHours || 0;
        const refundableAmount = (order.totalAmount * (order.totalHours - usedHours) / order.totalHours).toFixed(2);
        
        this.setData({
          order,
          refundableAmount,
          refundAmount: refundableAmount
        });
      })
      .catch(err => {
        wx.showToast({ title: '加载订单信息失败', icon: 'none' });
      });
  },

  // 选择退款类型
  selectRefundType(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({
      refundType: type
    });

    if (type === 1) {
      this.setData({
        refundAmount: this.data.refundableAmount
      });
    }
  },

  // 输入退款金额
  handleAmountInput(e) {
    this.setData({ refundAmount: e.detail.value });
  },

  // 输入退款原因
  handleReasonInput(e) {
    this.setData({ refundReason: e.detail.value });
  },

  // 提交退款申请
  submitRefund() {
    const { orderId, refundType, refundAmount, refundReason } = this.data;

    // 验证
    if (!refundReason) {
      return wx.showToast({ title: '请输入退款原因', icon: 'none' });
    }

    if (refundType === 2 && (!refundAmount || parseFloat(refundAmount) <= 0)) {
      return wx.showToast({ title: '请输入有效退款金额', icon: 'none' });
    }

    if (parseFloat(refundAmount) > parseFloat(this.data.refundableAmount)) {
      return wx.showToast({ title: '退款金额不能超过可退金额', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    // 调用退款接口
    request.post(api.order.refund, {
      orderId: orderId,
      refundType: refundType,
      refundAmount: refundAmount,
      refundReason: refundReason
    })
      .then(res => {
        wx.showToast({ title: '退款申请提交成功', icon: 'success' });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch(err => {
        wx.showToast({ title: '提交退款申请失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ isSubmitting: false });
      });
  }
});