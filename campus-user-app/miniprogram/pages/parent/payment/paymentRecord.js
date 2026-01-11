// 家长端支付记录页面
const api = require('../../../config/apiConfig.js');
const request = require('../../../utils/request.js');

Page({
  data: {
    payments: [],
    statuses: [
      { id: 0, name: '全部' },
      { id: 1, name: '待支付' },
      { id: 2, name: '已支付' },
      { id: 3, name: '已退款' }
    ],
    selectedStatus: 0,
    isLoading: true,
    page: 1,
    size: 10,
    hasMore: true
  },

  onLoad: function () {
    this.loadPayments();
  },

  // 加载支付记录
  loadPayments() {
    if (!this.data.hasMore) return;

    const params = {
      page: this.data.page,
      size: this.data.size
    };

    if (this.data.selectedStatus > 0) {
      params.status = this.data.selectedStatus;
    }

    request.get(api.order.listParent, params)
      .then(res => {
        const newPayments = res.records || [];
        const payments = this.data.page === 1 ? newPayments : [...this.data.payments, ...newPayments];
        
        this.setData({
          payments: payments,
          hasMore: newPayments.length >= this.data.size,
          isLoading: false
        });
      })
      .catch(err => {
        wx.showToast({ title: '加载支付记录失败', icon: 'none' });
        this.setData({ isLoading: false });
      });
  },

  // 选择状态筛选
  selectStatus(e) {
    const status = e.currentTarget.dataset.id;
    this.setData({
      selectedStatus: status,
      page: 1,
      payments: [],
      hasMore: true,
      isLoading: true
    });
    this.loadPayments();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({
      page: 1,
      payments: [],
      hasMore: true,
      isLoading: true
    });
    this.loadPayments();
    wx.stopPullDownRefresh();
  },

  // 上拉加载
  onReachBottom() {
    if (!this.data.isLoading && this.data.hasMore) {
      this.setData({
        page: this.data.page + 1,
        isLoading: true
      });
      this.loadPayments();
    }
  },

  // 跳转到订单详情
  goToOrderDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/parent/order/detail/detail?id=${id}` });
  },

  // 再次支付
  payAgain(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/parent/order/detail/detail?id=${orderId}&action=pay` });
  }
});