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
    e.stopPropagation();
    const id = e.currentTarget.dataset.id;
    const res = await wx.showModal({ title: '确认', content: '确认开课并开始教学？' });
    if (!res.confirm) return;
    try {
      await request.post(`${api.order.detail(id)}/start`);
      wx.showToast({ title: '已确认开课', icon: 'success' });
      this.loadList(true);
    } catch (err) { console.error(err); }
  }
});