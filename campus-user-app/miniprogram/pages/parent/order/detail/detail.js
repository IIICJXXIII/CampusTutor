const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    orderId: null,
    order: null,
    loading: true,
    statusText: ''
  },

  onLoad(options) {
    const id = options.id || options.orderId;
    this.setData({ orderId: id });
    this.fetchDetail();
  },

  async fetchDetail() {
    this.setData({ loading: true });
    try {
      const order = await request.get(api.order.detail(this.data.orderId));
      let statusText = '未知';
      switch (order.status) {
        case 0: statusText = '待支付'; break;
        case 1: statusText = '已支付，待开课'; break;
        case 2: statusText = '进行中'; break;
        case 3: statusText = '已完成'; break;
        case 4: statusText = '已取消'; break;
      }
      this.setData({ order, statusText, loading: false });
    } catch (err) {
      console.error(err);
      this.setData({ loading: false });
    }
  },

  // 支付选项（支持钱包/微信模拟）
  showPayOptions() {
    const that = this;
    wx.showActionSheet({
      itemList: ['钱包支付', '微信支付 (Mock)'],
      success(res) {
        if (res.tapIndex === 0) {
          // 钱包支付 - 弹出输入密码
          wx.showModal({
            title: '钱包支付',
            content: '请输入支付密码',
            editable: true,
            placeholderText: '支付密码',
            success: async (m) => {
              if (m.confirm) {
                const payPassword = m.content || '';
                await that.doPay(1, payPassword);
              }
            }
          });
        } else {
          that.doPay(2, ''); // 微信模拟
        }
      }
    });
  },

  async doPay(payType, payPassword) {
    wx.showLoading({ title: '支付中...' });
    try {
      await request.post(api.order.pay, { orderId: this.data.orderId, payType, payPassword });
      wx.hideLoading();
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.fetchDetail();
    } catch (err) {
      wx.hideLoading();
      console.error(err);
    }
  },

  async handleCancel() {
    const res = await wx.showModal({ title: '提示', content: '确定取消该订单？', editable: true, placeholderText: '请输入取消原因' });
    if (!res.confirm) return;
    const reason = res.content || '用户取消';
    try {
      await request.post(`${api.order.detail(this.data.orderId)}/cancel?reason=${encodeURIComponent(reason)}`);
      wx.showToast({ title: '已取消', icon: 'none' });
      this.fetchDetail();
    } catch (err) { console.error(err); }
  },

  async handleComplete() {
    const res = await wx.showModal({ title: '结课确认', content: '确认老师已完成教学？' });
    if (!res.confirm) return;
    try {
      await request.post(`${api.order.detail(this.data.orderId)}/complete`);
      wx.showToast({ title: '已结课', icon: 'success' });
      this.fetchDetail();
    } catch (err) { console.error(err); }
  }
});