const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    demandId: null,
    demand: null,
    loading: true,
    error: false,
    markers: []
  },

  onLoad(options) {
    const id = options.id;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.setData({ demandId: id });
    this.fetchDemandDetail();
  },

  // 获取需求详情
  async fetchDemandDetail() {
    this.setData({ loading: true, error: false });
    try {
      const demand = await request.get(api.demand.detail(this.data.demandId));
      // 动态设置markers数组
      const markers = demand && demand.longitude && demand.latitude ? [{ 
        id: demand.id, 
        latitude: demand.latitude, 
        longitude: demand.longitude 
      }] : [];
      this.setData({ demand, markers, loading: false });
    } catch (err) {
      console.error('获取需求详情失败', err);
      this.setData({ error: true, loading: false, markers: [] });
      wx.showToast({ title: '获取详情失败', icon: 'none' });
    }
  },

  // 重新加载
  handleReload() {
    this.fetchDemandDetail();
  },

  // 返回上一页
  handleBack() {
    wx.navigateBack();
  },

  // 联系家长
  handleContact() {
    if (!this.data.demand) return;
    wx.showToast({ title: '联系功能开发中', icon: 'none' });
  },

  // 立即接单
  handleAccept() {
    if (!this.data.demand) return;
    wx.showToast({ title: '接单功能开发中', icon: 'none' });
  }
});
