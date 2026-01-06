const storageUtil = require('../../../utils/storageUtil');

Page({
  data: {
    userInfo: {},
    menuList: [] // 动态菜单
  },

  onShow() {
    this.initPageData();
  },

  initPageData() {
    const userInfo = storageUtil.getUserInfo();
    this.setData({ userInfo });

    // 根据角色生成不同的菜单配置
    if (userInfo.role === 'TEACHER') {
      this.setData({
        menuList: [
          { icon: 'cert', title: '我的认证', url: '/pages/teacher/certification/step3-result/step3-result' }, // [cite: 445]
          { icon: 'schedule', title: '我的课表', url: '/pages/teacher/teacherSchedule/teacherSchedule' }, // [cite: 455]
          { icon: 'wallet', title: '收入明细', url: '/pages/teacher/incomeDetail/incomeDetail' } // [cite: 465]
        ]
      });
    } else {
      // 家长端菜单
      this.setData({
        menuList: [
          { icon: 'order', title: '我的订单', url: '/pages/parent/paymentOrder/paymentOrder' }, // [cite: 518]
          { icon: 'list', title: '课时记录', url: '/pages/parent/lessonList/lessonList' }, // [cite: 513]
          { icon: 'edit', title: '发布需求', url: '/pages/parent/publishDemand/step1-student/step1-student' } // [cite: 478]
        ]
      });
    }
  },

  // 菜单点击跳转
  handleMenuClick(e) {
    const url = e.currentTarget.dataset.url;
    if (url) wx.navigateTo({ url });
  },

  handleLogout() {
    storageUtil.clear(); // 清除缓存
    wx.reLaunch({ url: '/pages/common/login/login' });
  }
});