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
        longitude: demand.longitude,
        width: 30,
        height: 30,
        callout: {
          content: `¥${demand.expectPrice}\n${demand.subject} ${demand.grade}`,
          padding: 8,
          borderRadius: 4,
          display: 'ALWAYS',
          textAlign: 'center'
        }
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

  // 联系家长：跳转到聊天页面
  async handleContact() {
    if (!this.data.demand) return;
    const d = this.data.demand;
    const publisherId = d.publisherId || d.parentId;

    if (!publisherId) {
      wx.showToast({ title: '无法获取家长信息', icon: 'none' });
      return;
    }

    // 获取家长信息用于聊天
    try {
      const userInfo = await request.get(api.chat.userInfo(publisherId));
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent(userInfo.nickname || '家长')}&avatar=${encodeURIComponent(userInfo.avatar || '')}`
      });
    } catch (err) {
      // 即使获取失败也跳转，使用默认信息
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent('家长')}&avatar=`
      });
    }
  },

  // 立即接单 -> 临时改为邀约下单引导
  async handleInviteToOrder() {
    if (!this.data.demand) return;
    // 先尝试联系家长（会复制联系方式或用户名）
    await this.handleContact();
    // 弹窗引导
    const id = this.data.demand.id || this.data.demandId;
    wx.showModal({
      title: '邀约下单',
      content: '请通过已复制的联系方式联系家长，确认时间与价格后邀请家长在家长端发起订单并支付；如需平台协助，可复制需求ID并反馈给管理员。是否复制需求ID？',
      confirmText: '复制ID',
      cancelText: '知道了',
      success: (res) => {
        if (res.confirm) {
          wx.setClipboardData({ data: String(id), success() { wx.showToast({ title: '已复制ID', icon: 'none' }); } });
        }
      }
    });
  }
});
