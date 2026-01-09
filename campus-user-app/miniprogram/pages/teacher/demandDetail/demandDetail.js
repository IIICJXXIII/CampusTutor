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

  // 联系家长：优先复制手机号并提示拨打（如果有）
  async handleContact() {
    if (!this.data.demand) return;
    const d = this.data.demand || {};
    const phone = d.parentPhone || d.phone || d.mobile || d.contact || null;
    if (phone) {
      wx.setClipboardData({ data: String(phone), success() {
        wx.showToast({ title: '已复制家长手机号，可粘贴拨打', icon: 'none' });
      }});
      return;
    }

    // 无手机号，尝试根据发布者ID去取用户信息
    const publisherId = d.publisherId || d.parentId || null;
    if (publisherId) {
      try {
        const user = await request.get(api.user.byId(publisherId));
        if (user && (user.username || user.nickname)) {
          const contactValue = user.username || user.nickname;
          wx.setClipboardData({ data: String(contactValue), success() { wx.showToast({ title: '已复制家长联系方式（用户名）', icon: 'none' }); } });
          return;
        }
      } catch (err) {
        console.warn('获取家长用户信息失败', err);
      }
    }

    wx.showModal({
      title: '联系方式不可见',
      content: '家长未公开联系电话，您可以在接单成功后通过平台沟通或等待后台开放联系功能。',
      showCancel: false
    });
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
