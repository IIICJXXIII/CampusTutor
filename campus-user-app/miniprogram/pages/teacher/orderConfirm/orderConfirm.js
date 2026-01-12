const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    // 页面接收参数
    demandId: null,
    demand: null,
    
    // 表单数据
    totalHours: 10, // 默认10课时
    teachMode: 1, // 1上门 2在线
    remark: '',
    agreeProtocol: false, // 是否同意协议
    
    // 计算属性
    totalAmount: 0,
    unitPrice: 0,
    
    // 状态
    isSubmitting: false,
    loading: true,
    error: false
  },

  onLoad(options) {
    if (options.demandId) {
      this.setData({ demandId: options.demandId });
      this.fetchDemandDetail();
    } else {
      wx.showToast({ title: '参数错误', icon: 'none' });
      wx.navigateBack();
    }
  },

  // 获取需求详情
  async fetchDemandDetail() {
    this.setData({ loading: true, error: false });
    try {
      const demand = await request.get(api.demand.detail(this.data.demandId));
      this.setData({ 
        demand,
        unitPrice: demand.expectPrice || 0,
        teachMode: demand.teachMode === 3 ? 1 : demand.teachMode
      });
      this.calcTotal();
      this.setData({ loading: false });
    } catch (err) {
      console.error('获取需求详情失败', err);
      this.setData({ error: true, loading: false });
      wx.showToast({ title: '获取详情失败', icon: 'none' });
    }
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

  // 选择授课方式
  selectMode(e) {
    this.setData({ teachMode: parseInt(e.currentTarget.dataset.mode) });
  },

  handleInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 协议签署处理
  handleProtocolChange(e) {
    this.setData({
      agreeProtocol: e.detail.value.includes('agree')
    });
  },

  // 提交订单
  async submitOrder() {
    // 校验
    if (!this.data.agreeProtocol) return wx.showToast({ title: '请阅读并同意服务协议', icon: 'none' });

    this.setData({ isSubmitting: true });

    try {
      // 构造接单请求
      const payload = {
        demandId: this.data.demandId,
        totalHours: this.data.totalHours,
        remark: this.data.remark
      };

      // 调用后端接单接口
      const orderId = await request.post(api.order.accept, {
        demandId: this.data.demandId,
        totalHours: this.data.totalHours,
        remark: this.data.remark
      });
      
      wx.showToast({ title: '接单成功！', icon: 'success' });
      
      // 联系家长
      await this.handleContact();
      
      // 跳转到订单列表
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/teacher/orderList/orderList' });
      }, 1500);
      
    } catch (err) {
      console.error('接单失败:', err);
      wx.showToast({ title: '接单失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
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

  // 重新加载
  handleReload() {
    this.fetchDemandDetail();
  },

  // 返回上一页
  handleBack() {
    wx.navigateBack();
  }
});