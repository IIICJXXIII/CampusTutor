// 用户信息查看页面逻辑 - userInfoView.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil');

Page({
  data: {
    userInfo: {}
  },

  onLoad() {
    this.loadUserInfo();
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      // 先从本地存储获取用户信息
      const localUserInfo = storageUtil.getUserInfo();
      
      console.log('本地用户信息:', localUserInfo);
      
      if (!localUserInfo) {
        wx.showToast({ title: '用户信息缺失', icon: 'none' });
        console.error('本地用户信息缺失');
        // 即使本地信息不完整，也设置到页面
        this.setData({ userInfo: localUserInfo || {} });
        return;
      }

      // 从后端获取最新用户信息
      const userId = localUserInfo.id || localUserInfo.userId;
      console.log('从后端获取用户信息，ID:', userId);
      
      if (userId) {
        const response = await request.get(apiConfig.user.byId(userId));
        
        console.log('后端返回的用户信息:', response);
        
        if (response) {
          // 更新本地存储和页面数据
          storageUtil.setUserInfo({ ...localUserInfo, ...response });
          this.setData({ userInfo: response });
        } else {
          // 后端返回空，使用本地数据
          console.warn('后端返回空，使用本地数据');
          this.setData({ userInfo: localUserInfo });
        }
      } else {
        // 无用户ID，直接使用本地数据
        console.warn('无用户ID，直接使用本地数据');
        this.setData({ userInfo: localUserInfo });
      }

    } catch (err) {
      console.error('获取用户信息失败:', err);
      // 如果后端请求失败，使用本地存储的数据
      const localUserInfo = storageUtil.getUserInfo();
      console.log('使用本地存储的数据:', localUserInfo);
      this.setData({ userInfo: localUserInfo || {} });
    }
  },

  // 返回上一页
  navigateBack() {
    wx.navigateBack();
  }
});