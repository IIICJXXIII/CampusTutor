const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    currentTab: -1, // -1:全部, 0:待支付, 1:待开课, 2:进行中
    orderList: [],
    page: 1,
    size: 10,
    loading: false,
    hasMore: true
  },

  onLoad(options) {
    // 允许从其他页面带参数跳转过来 (例如 status=0)
    if (options.status) {
      this.setData({ currentTab: parseInt(options.status) });
    }
    this.loadOrders(true);
  },

  onShow() {
    // 每次显示时刷新，防止状态不同步
    // this.loadOrders(true); 
  },

  onPullDownRefresh() {
    this.loadOrders(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 });
      this.loadOrders(false);
    }
  },

  // 切换标签
  switchTab(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    if (status === this.data.currentTab) return;
    
    this.setData({ 
      currentTab: status, 
      orderList: [], 
      page: 1, 
      hasMore: true 
    });
    this.loadOrders(true);
  },

  // 加载数据
  async loadOrders(refresh = false) {
    if (refresh) {
      this.setData({ page: 1, hasMore: true });
    }
    
    this.setData({ loading: true });

    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      };
      // 如果不是全部(-1)，则传 status
      if (this.data.currentTab !== -1) {
        params.status = this.data.currentTab;
      }

      // 调用接口: GET /api/order/parent/list
      const res = await request.get(api.order.listParent, params);
      
      const records = res.records || [];
      
      // 格式化时间等 (简单截取)
      records.forEach(item => {
        if(item.createTime) item.createTime = item.createTime.replace('T', ' ').substring(0, 16);
      });

      this.setData({
        orderList: refresh ? records : this.data.orderList.concat(records),
        hasMore: this.data.page < res.pages,
        loading: false
      });

      if (refresh) wx.stopPullDownRefresh();

    } catch (err) {
      console.error('loadOrders error:', err);
      wx.showToast({ title: '加载订单失败，请稍后重试', icon: 'none' });
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  // 去支付 (直接调用支付接口模拟)
  handlePay(e) {
    const orderId = e.currentTarget.dataset.id;
    const that = this;

    wx.showActionSheet({
      itemList: ['微信支付 (Mock)'],
      success(res) {
        if (res.tapIndex === 0) {
          that.doPay(orderId);
        }
      }
    });
  },

  async doPay(orderId) {
    wx.showLoading({ title: '支付中...' });
    try {
      // 构造 PayOrderRequest
      await request.post(api.order.pay, {
        orderId: orderId,
        payType: 2 // 微信
      });
      wx.hideLoading();
      wx.showToast({ title: '支付成功', icon: 'success' });
      // 刷新列表
      setTimeout(() => {
        this.loadOrders(true);
      }, 1000);
    } catch (err) {
      wx.hideLoading();
      console.error(err);
    }
  },

  // 取消订单
  handleCancel(e) {
    const orderId = e.currentTarget.dataset.id;
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
      // POST /api/order/{id}/cancel?reason=...
      await request.post(`${api.order.detail(orderId)}/cancel?reason=${reason}`);
      wx.showToast({ title: '已取消', icon: 'none' });
      this.loadOrders(true);
    } catch (err) {
      console.error(err);
    }
  },

  // 确认结课 (Parent Completes Order)
  handleComplete(e) {
    const orderId = e.currentTarget.dataset.id;
    const that = this;
    wx.showModal({
      title: '结课确认',
      content: '确认老师已完成所有教学任务吗？',
      success(res) {
        if (res.confirm) {
          that.doComplete(orderId);
        }
      }
    });
  },

  async doComplete(orderId) {
    try {
      // POST /api/order/{id}/complete
      await request.post(`${api.order.detail(orderId)}/complete`);
      wx.showToast({ title: '已结课', icon: 'success' });
      this.loadOrders(true);
    } catch (err) {
      console.error(err);
    }
  },

  // 跳转详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/parent/order/detail/detail?id=${id}` });
  }
});