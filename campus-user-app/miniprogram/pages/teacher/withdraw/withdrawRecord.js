// 教师端提现记录页面
const api = require('../../../config/apiConfig.js');
const request = require('../../../utils/request.js');

Page({
  data: {
    withdrawals: [],
    statuses: [
      { id: 0, name: '全部' },
      { id: 1, name: '待审核' },
      { id: 2, name: '已通过' },
      { id: 3, name: '已拒绝' },
      { id: 4, name: '已到账' }
    ],
    selectedStatus: 0,
    isLoading: true,
    page: 1,
    size: 10,
    hasMore: true
  },

  onLoad: function () {
    this.loadWithdrawals();
  },

  // 加载提现记录
  loadWithdrawals() {
    if (!this.data.hasMore) return;

    const params = {
      page: this.data.page,
      size: this.data.size
    };

    if (this.data.selectedStatus > 0) {
      params.status = this.data.selectedStatus;
    }

    request.get(api.wallet.withdrawals, params)
      .then(res => {
        const newWithdrawals = res.records || [];
        const withdrawals = this.data.page === 1 ? newWithdrawals : [...this.data.withdrawals, ...newWithdrawals];
        
        this.setData({
          withdrawals: withdrawals,
          hasMore: newWithdrawals.length >= this.data.size,
          isLoading: false
        });
      })
      .catch(err => {
        wx.showToast({ title: '加载提现记录失败', icon: 'none' });
        this.setData({ isLoading: false });
      });
  },

  // 选择状态筛选
  selectStatus(e) {
    const status = e.currentTarget.dataset.id;
    this.setData({
      selectedStatus: status,
      page: 1,
      withdrawals: [],
      hasMore: true,
      isLoading: true
    });
    this.loadWithdrawals();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({
      page: 1,
      withdrawals: [],
      hasMore: true,
      isLoading: true
    });
    this.loadWithdrawals();
    wx.stopPullDownRefresh();
  },

  // 上拉加载
  onReachBottom() {
    if (!this.data.isLoading && this.data.hasMore) {
      this.setData({
        page: this.data.page + 1,
        isLoading: true
      });
      this.loadWithdrawals();
    }
  },

  // 跳转到提现详情
  goToWithdrawDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/teacher/withdraw/withdrawDetail?id=${id}` });
  }
});