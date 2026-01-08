const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

// 调试：查看request和api对象结构
console.log('=== request 对象结构 ===');
console.log('request:', request);
console.log('request.get:', request.get);
console.log('=== api 对象结构 ===');
console.log('api:', api);
console.log('api.demand:', api.demand);
console.log('api.demand.my:', api.demand.my);

Page({
  data: {
    demandList: [],
    loading: true,
    // 状态映射
    statusMap: {
      0: { text: '已关闭', color: '#909399', bg: '#f4f4f5' },
      1: { text: '招聘中', color: '#409EFF', bg: '#ecf5ff' },
      2: { text: '已匹配', color: '#67c23a', bg: '#f0f9eb' }
    }
  },

  onShow() {
    this.fetchMyDemands();
  },

  onPullDownRefresh() {
    this.fetchMyDemands();
  },

  // 获取列表
  async fetchMyDemands() {
    this.setData({ loading: true });
    try {
      // 对应 DemandController.myDemands -> GET /api/demand/my
      const res = await request.get(api.demand.my);
      
      // 简单的时间格式化
      const list = (res || []).map(item => ({
        ...item,
        createTimeStr: item.createTime ? item.createTime.substring(0, 10) : ''
      }));

      this.setData({ demandList: list, loading: false });
      wx.stopPullDownRefresh();
    } catch (err) {
      console.error(err);
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  // 切换上下架状态
  async toggleStatus(e) {
    const { id, status } = e.currentTarget.dataset;
    const newStatus = status === 1 ? 0 : 1; // 1->0(下架), 0->1(上架)
    const actionName = newStatus === 1 ? '上架' : '关闭';
    
    // 状态2(已匹配)不可操作
    if (status === 2) return;

    const that = this;
    wx.showModal({
      title: '提示',
      content: `确定要${actionName}该需求吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            // 根据状态调用不同接口
            // DemandController: POST /{id}/online 或 /{id}/offline
            const url = newStatus === 1 
              ? `${api.demand.publish}/${id}/online` // 注意：这里api路径可能需要微调，假设是 /api/demand/{id}/online
              : `${api.demand.publish}/${id}/offline`;
            
            // 由于apiConfig里可能没单独定义online/offline，这里手动拼接
            // 假设 api.demand.publish 是 .../publish，我们需要 .../demand/{id}/online
            // 更安全的做法是利用 apiConfig.host 拼接
            const baseUrl = api.demand.list.replace('/list', ''); // 提取 /api/demand
            const actUrl = `${baseUrl}/${id}/${newStatus === 1 ? 'online' : 'offline'}`;

            await request.post(actUrl);
            
            wx.showToast({ title: '操作成功', icon: 'success' });
            that.fetchMyDemands();
          } catch (err) {
            console.error(err);
          }
        }
      }
    });
  },

  // 删除需求
  handleDelete(e) {
    const id = e.currentTarget.dataset.id;
    const that = this;
    
    wx.showModal({
      title: '警告',
      content: '删除后无法恢复，确定删除吗？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            // DemandController: DELETE /{id}
            // 同样手动拼接一下URL
            const baseUrl = api.demand.list.replace('/list', '');
            await request.delete(`${baseUrl}/${id}`);
            
            wx.showToast({ title: '已删除', icon: 'success' });
            that.fetchMyDemands();
          } catch (err) {
            console.error(err);
          }
        }
      }
    });
  },

  // 跳转去发布
  goPublish() {
    wx.navigateTo({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
  }
});