// AI助手入口中转页 - 根据角色跳转到不同的AI页面
Page({
  data: {
    userInfo: {}
  },

  onShow() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ userInfo });
  },

  // 跳转到AI教案生成页面
  goToLessonPlan() {
    wx.navigateTo({
      url: '/pages/teacher/aiAssistant/aiLessonPlan'
    });
  },
  
  // 跳转到AI评语润色页面
  goToComment() {
    wx.navigateTo({
      url: '/pages/teacher/aiAssistant/aiComment'
    });
  },
  
  // AI教学咨询（待开发）
  goToChat() {
    wx.showModal({
      title: 'AI教学咨询',
      content: 'AI教学咨询功能即将上线，敬请期待！',
      showCancel: false
    });
  },

  // 家长端AI咨询
  goToParentAi() {
    wx.showModal({
      title: 'AI成长管家',
      content: 'AI成长管家功能即将上线，敬请期待！',
      showCancel: false
    });
  }
});
