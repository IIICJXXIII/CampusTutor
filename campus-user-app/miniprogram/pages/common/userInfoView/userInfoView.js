// 用户信息查看页面逻辑 - userInfoView.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil');

Page({
  data: {
    userInfo: {},
    isEditMode: false,
    editUserInfo: {} // 用于编辑的临时数据
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

  // 切换到编辑模式
  switchToEditMode() {
    // 复制当前用户信息到编辑数据中
    const editUserInfo = JSON.parse(JSON.stringify(this.data.userInfo));
    
    this.setData({
      isEditMode: true,
      editUserInfo
    });
  },

  // 保存修改
  async saveEdit() {
    let loadingShown = false;
    try {
      wx.showLoading({ title: '保存中...' });
      loadingShown = true;
      
      // 调用后端API保存用户信息 - 使用正确的PUT方法和接口地址
      const response = await request.put(apiConfig.user.updateInfo, this.data.editUserInfo);
      
      // 无论response是否为空，都要更新用户信息
      let updatedUserInfo;
      const localUserInfo = storageUtil.getUserInfo();
      
      if (response) {
        // 合并本地数据和后端返回数据
        updatedUserInfo = { ...localUserInfo, ...response };
        storageUtil.setUserInfo(updatedUserInfo);
        
        // 重新调用loadUserInfo确保获取完整的最新数据
        await this.loadUserInfo();
      } else {
        // 如果后端没有返回数据，使用本地数据和编辑的数据合并
        updatedUserInfo = { ...localUserInfo, ...this.data.editUserInfo };
        storageUtil.setUserInfo(updatedUserInfo);
        this.setData({ userInfo: updatedUserInfo });
      }
      
      // 先隐藏loading，再显示toast
      if (loadingShown) {
        wx.hideLoading();
        loadingShown = false;
      }
      
      wx.showToast({ title: '保存成功', icon: 'success' });
      
      this.setData({ isEditMode: false });
    } catch (err) {
      console.error('保存用户信息失败:', err);
      
      // 先隐藏loading，再显示toast
      if (loadingShown) {
        wx.hideLoading();
        loadingShown = false;
      }
      
      wx.showToast({ title: '保存失败', icon: 'none' });
    } finally {
      // 确保如果loading还在显示，就隐藏它
      if (loadingShown) {
        wx.hideLoading();
      }
    }
  },

  // 取消编辑
  cancelEdit() {
    this.setData({
      isEditMode: false,
      editUserInfo: {} // 清空编辑数据
    });
  },

  // 输入框值变化事件
  onInputChange(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`editUserInfo.${field}`]: value
    });
  },

  // Picker选择变化事件
  onPickerChange(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`editUserInfo.${field}`]: value
    });
  }
});