const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    items: [],
    loading: true
  },

  onLoad() {
    this.loadList();
  },

  async loadList() {
    this.setData({ loading: true });
    try {
      const res = await request.get(api.study.wrongbook.list);
      // 后端可能返回数组或包装在 data 中，兼容处理
      const items = Array.isArray(res) ? res : (res && res.list) ? res.list : [];
      // 简单格式化
      const fmt = items.map(i => ({ id: i.id, questionTitle: i.title || i.question || '题目', subject: i.subject || '', createdAt: i.createdAt || i.createTime || '' }));
      this.setData({ items: fmt });
    } catch (err) {
      console.warn('获取错题本失败，可能后端未实现或接口错误：', err);
      // 友好提示并显示空白状态
      wx.showToast({ title: '无法获取错题，显示本地示例', icon: 'none' });
      // 本地示例数据，便于前端联调
      this.setData({
        items: [
          { id: 'mock1', questionTitle: '示例：二次函数顶点如何求？', subject: '数学', createdAt: '2026-01-07' },
          { id: 'mock2', questionTitle: '示例：英语时态选择题解析', subject: '英语', createdAt: '2026-01-06' }
        ]
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/common/wrongBookDetail/wrongBookDetail?id=${id}` });
  },

  async markResolved(e) {
    if (e && e.stopPropagation) e.stopPropagation();
    const id = e.currentTarget.dataset.id;
    wx.showLoading({ title: '提交中...' });
    try {
      const r = await request.post(api.study.wrongbook.resolve(id));
      wx.showToast({ title: '已标记为已解决', icon: 'success' });
      // 重新加载
      this.loadList();
    } catch (err) {
      console.warn('标记已解决失败（可能后端未实现）：', err);
      wx.showToast({ title: '标记失败（示例演示）', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
