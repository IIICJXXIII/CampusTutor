const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: { 
    orderId: null, 
    order: null, 
    loading: true, 
    statusText: '' 
  },
  
  onLoad(options) { 
    this.setData({ orderId: options.id }); 
    this.fetchDetail(); 
  },

  async fetchDetail() {
    this.setData({ loading: true });
    try {
      const order = await request.get(api.order.detail(this.data.orderId));
      let statusText = '未知';
      switch (order.status) {
        case -1: statusText = '待确认'; break;
        case 0: statusText = '待支付'; break;
        case 1: statusText = '已支付，待开课'; break;
        case 2: statusText = '进行中'; break;
        case 3: statusText = '已完成'; break;
        case 4: statusText = '已取消'; break;
        case 5: statusText = '退款中'; break;
        case 6: statusText = '已退款'; break;
      }
      this.setData({ order, statusText, loading: false });
    } catch (err) { 
      console.error(err); 
      this.setData({ loading: false }); 
    }
  },

  async startClass() {
    const res = await wx.showModal({ title: '确认', content: '确认开课？' });
    if (!res.confirm) return;
    try {
      await request.post(`${api.order.detail(this.data.orderId)}/start`);
      wx.showToast({ title: '已确认开课', icon: 'success' });
      this.fetchDetail();
    } catch (err) { 
      console.error(err); 
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  async markComplete() {
    const res = await wx.showModal({ title: '确认', content: '确认已完成所有课程并结算？' });
    if (!res.confirm) return;
    try {
      await request.post(`${api.order.detail(this.data.orderId)}/complete`);
      wx.showToast({ title: '已标记完成', icon: 'success' });
      this.fetchDetail();
    } catch (err) { 
      console.error(err); 
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  async handleCancel() {
    const res = await wx.showModal({ 
      title: '提示', 
      content: '确定取消该订单？', 
      editable: true, 
      placeholderText: '请输入取消原因' 
    });
    if (!res.confirm) return;
    const reason = res.content || '用户取消';
    try {
      await request.post(`${api.order.detail(this.data.orderId)}/cancel?reason=${encodeURIComponent(reason)}`);
      wx.showToast({ title: '已取消', icon: 'none' });
      this.fetchDetail();
    } catch (err) { 
      console.error(err); 
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  }
});