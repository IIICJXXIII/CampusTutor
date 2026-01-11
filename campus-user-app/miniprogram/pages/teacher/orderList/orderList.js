const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: { page: 1, size: 10, list: [], loading: false, status: null },
  onLoad() { this.loadList(true); },
  onShow() { /* 可在此刷新 */ },

  async loadList(refresh = false) {
    if (refresh) this.setData({ page: 1, list: [] });
    this.setData({ loading: true });
    try {
      const params = { page: this.data.page, size: this.data.size };
      if (this.data.status !== null) params.status = this.data.status;
      const res = await request.get(api.order.listTutor, params);
      const records = res.records || [];
      this.setData({ list: refresh ? records : this.data.list.concat(records), loading: false });
    } catch (err) { console.error(err); this.setData({ loading: false }); }
  },

  filterAll() { this.setData({ status: null }); this.loadList(true); },
  filterStatus(e) { this.setData({ status: parseInt(e.currentTarget.dataset.status) }); this.loadList(true); },

  goDetail(e) { const id = e.currentTarget.dataset.id; wx.navigateTo({ url: `/pages/teacher/orderDetail/orderDetail?id=${id}` }); },

  async startClass(e) {
    const id = e.currentTarget.dataset.id;
    const res = await wx.showModal({ title: '确认', content: '确认开课并开始教学？' });
    if (!res.confirm) return;
    try {
      await request.post(`${api.order.detail(id)}/start`);
      wx.showToast({ title: '已确认开课', icon: 'success' });
      this.loadList(true);
    } catch (err) { console.error(err); }
  },

  // 取消订单
  handleCancel(e) {
    const orderId = e.currentTarget.dataset.id;
    const orderStatus = e.currentTarget.dataset.status;
    
    // 检查订单状态是否可以取消
    if (orderStatus !== 0 && orderStatus !== 1) {
      wx.showToast({ title: '当前状态无法取消', icon: 'none' });
      return;
    }
    
    const that = this;
    
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      editable: true,
      placeholderText: '请输入取消原因',
      success(res) {
        if (res.confirm) {
          const reason = res.content || '用户主动取消';
          that.doCancel(orderId, reason);
        }
      }
    });
  },

  async doCancel(orderId, reason) {
    try {
      await request.post(`${api.order.detail(orderId)}/cancel?reason=${reason}`);
      wx.showToast({ title: '已取消', icon: 'none' });
      this.loadList(true);
    } catch (err) {
      console.error(err);
      wx.showToast({ title: '取消失败', icon: 'none' });
    }
  },

  // 确认结课
  handleComplete(e) {
    const orderId = e.currentTarget.dataset.id;
    const that = this;
    
    wx.showModal({
      title: '结课确认',
      content: '确认已完成所有教学任务吗？',
      success(res) {
        if (res.confirm) {
          that.doComplete(orderId);
        }
      }
    });
  },

  async doComplete(orderId) {
    try {
      await request.post(`${api.order.detail(orderId)}/complete`);
      wx.showToast({ title: '已确认结课', icon: 'success' });
      this.loadList(true);
    } catch (err) {
      console.error(err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  }
});