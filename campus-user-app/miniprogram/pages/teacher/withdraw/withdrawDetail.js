// 教师端提现详情页面
const api = require('../../../config/apiConfig.js');
const request = require('../../../utils/request.js');

Page({
  data: {
    withdrawal: null,
    isLoading: true
  },

  onLoad: function (options) {
    const id = options.id;
    this.loadWithdrawalDetail(id);
  },

  // 加载提现详情
  loadWithdrawalDetail(id) {
    request.get(`${api.wallet.withdrawals}/${id}`)
      .then(res => {
        this.setData({
          withdrawal: res,
          isLoading: false
        });
      })
      .catch(err => {
        wx.showToast({ title: '加载提现详情失败', icon: 'none' });
        this.setData({ isLoading: false });
      });
  },

  // 获取状态文本
  getStatusText(status) {
    const statusMap = {
      1: '待审核',
      2: '已通过',
      3: '已拒绝',
      4: '已到账'
    };
    return statusMap[status] || '未知状态';
  },

  // 获取状态样式
  getStatusClass(status) {
    const classMap = {
      1: 'pending',
      2: 'approved',
      3: 'rejected',
      4: 'completed'
    };
    return classMap[status] || '';
  },

  // 获取渠道文本
  getChannelText(channel) {
    const channelMap = {
      1: '微信',
      2: '支付宝',
      3: '银行卡'
    };
    return channelMap[channel] || '其他';
  },

  // 格式化时间
  formatTime(time) {
    if (!time) return '';
    const date = new Date(time);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
});