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
    console.log('页面加载，options:', options);
    const id = options.id;
    if (!id) {
      console.log('缺少id参数');
      wx.showToast({ title: '参数错误', icon: 'none' });
      wx.navigateBack();
      return;
    }
    console.log('设置demandId:', id);
    this.setData({ demandId: id });
    this.fetchDemandDetail();
  },

  // 获取需求详情
  async fetchDemandDetail() {
    console.log('开始获取需求详情，demandId:', this.data.demandId);
    this.setData({ loading: true, error: false });
    try {
      const url = api.demand.detail(this.data.demandId);
      console.log('请求URL:', url);
      const demand = await request.get(url);
      console.log('获取到的需求数据:', demand);
      
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
      
      console.log('设置的数据:', { demand, markers });
      this.setData({ demand, markers, loading: false });
      console.log('数据设置完成');
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
    console.log('联系家长按钮被点击');
    if (!this.data.demand) {
      console.log('demand数据为空');
      return;
    }
    const d = this.data.demand;
    const publisherId = d.publisherId || d.parentId;

    console.log('家长ID:', publisherId);
    if (!publisherId) {
      wx.showToast({ title: '无法获取家长信息', icon: 'none' });
      return;
    }

    // 获取家长信息用于聊天
    try {
      console.log('获取家长信息:', api.chat.userInfo(publisherId));
      const userInfo = await request.get(api.chat.userInfo(publisherId));
      console.log('获取到的家长信息:', userInfo);
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent(userInfo.nickname || '家长')}&avatar=${encodeURIComponent(userInfo.avatar || '')}`
      });
    } catch (err) {
      console.error('获取家长信息失败:', err);
      // 即使获取失败也跳转，使用默认信息
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent('家长')}&avatar=`
      });
    }
  },

  // 立即接单
  handleInviteToOrder() {
    console.log('邀约下单按钮被点击');
    if (!this.data.demand) {
      console.log('demand数据为空');
      wx.showToast({ title: '数据加载失败，请稍后重试', icon: 'none' });
      return;
    }
    
    console.log('需求数据:', this.data.demand);
    
    // 确保必要字段存在
    const demand = this.data.demand;
    const subject = demand.subject || '未知科目';
    const grade = demand.grade || '未知年级';
    const expectPrice = demand.expectPrice || 0;
    const address = demand.address || '未知地址';
    const id = demand.id || null;
    
    if (!id) {
      console.error('需求ID不存在');
      wx.showToast({ title: '需求信息不完整', icon: 'none' });
      return;
    }
    
    wx.showModal({
      title: '确认接单',
      content: `确定要接取这个需求吗？\n课程：${subject} - ${grade}\n期望薪资：¥${expectPrice}/小时\n上课地址：${address}`,
      confirmText: '确认接单',
      cancelText: '取消',
      success: (res) => {
        console.log('模态框结果:', res);
        if (res.confirm) {
          // 跳转到订单确认页面
          const url = `/pages/teacher/orderConfirm/orderConfirm?demandId=${id}`;
          console.log('跳转到订单确认页面:', url);
          
          // 验证URL格式
          if (!url || typeof url !== 'string') {
            console.error('URL格式错误:', url);
            wx.showToast({ title: '系统错误，请稍后重试', icon: 'none' });
            return;
          }
          
          wx.navigateTo({
            url: url,
            success: function(res) {
              console.log('跳转成功:', res);
            },
            fail: function(err) {
              console.error('跳转失败:', err);
              // 显示详细的错误信息
              wx.showToast({ 
                title: `跳转失败: ${err.errMsg || '未知错误'}`, 
                icon: 'none',
                duration: 3000
              });
            }
          });
        }
      },
      fail: function(err) {
        console.error('模态框显示失败:', err);
        wx.showToast({ title: '系统错误，请稍后重试', icon: 'none' });
      }
    });
  }
});
