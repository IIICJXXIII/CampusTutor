const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    educationList: ['大专', '本科', '硕士', '博士'],
    educationIndex: -1,
    formData: {
      realName: '',
      idCard: '',
      universityName: '',
      major: '',
      education: '', // 存储文字或数字，根据后端定义，这里假设后端要数字 1-4
      enrollYear: '',
      studentCardUrl: ''
    }
  },

  onLoad() {
    // 如果有本地缓存，回显数据（优化体验）
    const cachedData = wx.getStorageSync('cert_step1');
    if (cachedData) {
      this.setData({ formData: cachedData });
      // 恢复 picker 索引逻辑略
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`formData.${field}`]: e.detail.value
    });
  },

  handleEducationChange(e) {
    this.setData({
      educationIndex: e.detail.value,
      'formData.education': parseInt(e.detail.value) + 1 // 假设后端 1:大专, 2:本科...
    });
  },

  handleYearChange(e) {
    this.setData({
      'formData.enrollYear': e.detail.value
    });
  },

  // 上传图片
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      success: async (res) => {
        const filePath = res.tempFilePaths[0];
        wx.showLoading({ title: '上传中...' });
        
        try {
          // 调用我们封装好的上传方法
          const url = await request.upload(api.file.upload, filePath, { folder: 'cert' });
          this.setData({ 'formData.studentCardUrl': url });
          wx.hideLoading();
        } catch (err) {
          wx.hideLoading();
          // 模拟环境容错：如果没有真实后端，可以使用本地临时路径演示
          // this.setData({ 'formData.studentCardUrl': filePath });
        }
      }
    });
  },

  nextStep() {
    const { realName, idCard, universityName, studentCardUrl } = this.data.formData;
    
    // 简单校验
    if (!realName || !idCard || !universityName) {
      return wx.showToast({ title: '请填写完整信息', icon: 'none' });
    }
    if (!studentCardUrl) {
      return wx.showToast({ title: '请上传学生证', icon: 'none' });
    }

    // 保存当前步骤数据到本地存储，供 Step 2 使用
    wx.setStorageSync('cert_step1', this.data.formData);
    
    wx.navigateTo({
      url: '../step2-ability/step2-ability'
    });
  }
});