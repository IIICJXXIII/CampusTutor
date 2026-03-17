// 教师端AI助手首页逻辑
Page({
  data: {
    // 页面数据
  },
  
  onLoad(options) {
    // 页面加载逻辑
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
  
  // 跳转到AI教学咨询页面
  goToChat() {
    // 这里可以跳转到现有的AI聊天页面或创建新的聊天页面
    wx.showModal({
      title: 'AI教学咨询',
      content: 'AI教学咨询功能即将上线，敬请期待！',
      showCancel: false
    });
  }
});