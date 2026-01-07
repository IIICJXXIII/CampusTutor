// 个人中心页面逻辑 - personalCenter.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil');

Page({
  data: {
    userInfo: {},
    walletInfo: {
      balance: '0.00',
      frozenAmount: '0.00'
    },
    menuList: []
  },

  onShow() {
    this.initPageData();
  },

  initPageData() {
    const userInfo = storageUtil.getUserInfo();

    // 检查登录状态，未登录则跳转到登录页
    if (!userInfo || !userInfo.role) {
      wx.redirectTo({ url: '/pages/common/login/login' });
      return;
    }

    this.setData({ userInfo });

    // 根据角色生成不同的菜单配置
    if (userInfo.role === 1) {
      // 教师端菜单
      this.setData({
        menuList: [
          { icon: 'business', title: '我的认证', url: '/pages/teacher/certification/step3-result/step3-result', bgColor: '#e8f4fd' },
          { icon: 'examples', title: '我的课表', url: '/pages/teacher/teacherSchedule/teacherSchedule', bgColor: '#f0f9eb' },
          { icon: 'goods', title: '我的订单', url: '/pages/teacher/orderList/orderList', bgColor: '#fdf6ec' },
          { icon: 'home', title: '找学生', url: '/pages/teacher/mapFindStudent/mapFindStudent', bgColor: '#fef0f0' }
        ]
      });
      // 加载钱包信息
      this.loadWalletInfo();
    } else {
      // 家长端菜单
      this.setData({
        menuList: [
          { icon: 'goods', title: '我的订单', url: '/pages/parent/order/list/list', bgColor: '#e8f4fd' },
          { icon: 'examples', title: '课时记录', url: '/pages/parent/lessonList/lessonList', bgColor: '#f0f9eb' },
          { icon: 'goods', title: '我的需求', url: '/pages/parent/demand/myList/myList', bgColor: '#fdf6ec' },
          { icon: 'copy', title: '发布需求', url: '/pages/parent/publishDemand/step1-student/step1-student', bgColor: '#fef0f0' }
        ]
      });
    }
  },

  // 加载钱包信息（教师专用）
  async loadWalletInfo() {
    try {
      const result = await request.get(apiConfig.wallet.info);
      this.setData({
        walletInfo: {
          balance: result.balance ? Number(result.balance).toFixed(2) : '0.00',
          frozenAmount: result.frozenAmount ? Number(result.frozenAmount).toFixed(2) : '0.00'
        }
      });
    } catch (err) {
      console.error('获取钱包信息失败:', err);
    }
  },

  // 进入收入明细页面
  goToIncome() {
    wx.navigateTo({
      url: '/pages/teacher/incomeDetail/incomeDetail'
    });
  },

  // 菜单点击跳转
  handleMenuClick(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({ url });
    }
  },

  // 关于我们
  handleAbout() {
    wx.showModal({
      title: '关于我们',
      content: 'CampusTutor 校园智教平台\n版本: 1.0.0\n致力于连接优质教员与家长，提供高效、透明的家教服务。',
      showCancel: false
    });
  },

  // 意见反馈
  handleFeedback() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          storageUtil.clear();
          wx.reLaunch({ url: '/pages/common/login/login' });
        }
      }
    });
  }
});