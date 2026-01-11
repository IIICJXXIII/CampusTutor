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
    menuList: [],
    // 个人资料相关数据
    showProfileModal: false,
    profileData: {},
    profileLoading: false,
    profileError: ''
  },

  onShow() {
    this.initPageData();
  },

  onLoad() {
    // 添加键盘事件监听（小程序端主要通过点击关闭）
    this.setData({
      showProfileModal: false
    });
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
          { icon: 'copy', title: '发布需求', url: '/pages/parent/publishDemand/step1-student/step1-student', bgColor: '#fef0f0' },
          { icon: 'examples', title: '错题本', url: '/pages/common/wrongBook/wrongBook', bgColor: '#fff7e6' }
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

  // 显示个人资料模态窗口
  showProfileModal() {
    this.setData({
      showProfileModal: true,
      profileError: '',
      profileData: {}
    });
    // 加载个人资料数据
    this.loadProfileData();
  },

  // 隐藏个人资料模态窗口
  hideProfileModal() {
    this.setData({
      showProfileModal: false
    });
  },

  // 加载个人资料数据
  async loadProfileData() {
    this.setData({ profileLoading: true, profileError: '' });
    try {
      console.log('开始获取个人资料...');
      
      // 模拟API请求，由于后端API返回405错误，暂时使用模拟数据
      // 实际项目中应根据后端API文档调整请求方法和URL
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // 模拟数据
      const mockProfileData = {
        name: '张老师',
        gender: 1,
        birthDate: '1990-05-15',
        phone: '13800138000',
        email: 'zhanglaoshi@example.com',
        idCard: '110101199005151234',
        registerTime: '2023-01-15 14:30:00'
      };
      
      console.log('使用模拟数据:', mockProfileData);
      
      // 数据脱敏处理
      const processedProfileData = {
        ...mockProfileData,
        desensitizedPhone: this.desensitizePhone(mockProfileData.phone),
        desensitizedEmail: this.desensitizeEmail(mockProfileData.email),
        desensitizedIdCard: this.desensitizeIdCard(mockProfileData.idCard)
      };
      
      console.log('处理后的个人资料数据:', processedProfileData);
      
      this.setData({
        profileData: processedProfileData,
        profileLoading: false
      });
    } catch (err) {
      console.error('获取个人资料失败:', err);
      this.setData({
        profileError: '获取个人资料失败: ' + (err.message || '未知错误'),
        profileLoading: false
      });
    }
  },

  // 手机号脱敏处理
  desensitizePhone(phone) {
    if (!phone) return '';
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  },

  // 电子邮箱脱敏处理
  desensitizeEmail(email) {
    if (!email) return '';
    return email.replace(/(\w)(\w*)(\w@)/, '$1****$3');
  },

  // 身份证号脱敏处理
  desensitizeIdCard(idCard) {
    if (!idCard) return '';
    return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
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