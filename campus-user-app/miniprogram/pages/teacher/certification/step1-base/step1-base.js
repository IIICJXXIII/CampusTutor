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
      education: '',
      enrollYear: '',
      studentCardUrl: ''
    },
    isRecognizing: false  // OCR识别中
  },

  onLoad() {
    const cachedData = wx.getStorageSync('cert_step1');
    if (cachedData) {
      this.setData({ formData: cachedData });
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
      'formData.education': parseInt(e.detail.value) + 1
    });
  },

  handleYearChange(e) {
    this.setData({
      'formData.enrollYear': e.detail.value
    });
  },

  // 上传学生证并触发OCR识别
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      success: async (res) => {
        const filePath = res.tempFilePaths[0];
        wx.showLoading({ title: '上传中...' });

        try {
          // 1. 上传图片获取URL
          const uploadUrl = await request.upload(api.file.upload, filePath, { folder: 'cert' });
          // 兼容处理：可能是字符串URL或对象
          const imageUrl = typeof uploadUrl === 'string' ? uploadUrl : uploadUrl.url;

          this.setData({ 'formData.studentCardUrl': imageUrl });
          wx.hideLoading();

          // 2. 触发OCR识别
          this.recognizeStudentCard(imageUrl);

        } catch (err) {
          wx.hideLoading();
          console.error('上传失败:', err);
          wx.showToast({ title: '上传失败，请重试', icon: 'none' });
        }
      }
    });
  },

  // OCR识别学生证
  async recognizeStudentCard(imageUrl) {
    this.setData({ isRecognizing: true });
    wx.showLoading({ title: '识别中...' });

    try {
      // 调用OCR接口，imageUrl作为query参数
      const ocrResult = await new Promise((resolve, reject) => {
        const token = wx.getStorageSync('token');
        wx.request({
          url: `${api.ocr.studentCard}?imageUrl=${encodeURIComponent(imageUrl)}`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${token}`,
            'content-type': 'application/json'
          },
          success: (res) => {
            if (res.data.code === 200) {
              resolve(res.data.data);
            } else {
              reject(new Error(res.data.msg || '识别失败'));
            }
          },
          fail: reject
        });
      });

      wx.hideLoading();
      this.setData({ isRecognizing: false });

      // 自动填充识别结果
      if (ocrResult && ocrResult.success) {
        const updates = {};
        if (ocrResult.realName) updates['formData.realName'] = ocrResult.realName;
        if (ocrResult.universityName) updates['formData.universityName'] = ocrResult.universityName;
        if (ocrResult.major) updates['formData.major'] = ocrResult.major;
        if (ocrResult.enrollYear) updates['formData.enrollYear'] = String(ocrResult.enrollYear);

        if (Object.keys(updates).length > 0) {
          this.setData(updates);
          wx.showToast({ title: '识别成功，已自动填充', icon: 'success' });
        } else {
          wx.showToast({ title: '未能识别到有效信息', icon: 'none' });
        }
      } else {
        wx.showToast({ title: ocrResult.errorMsg || '识别失败，请手动填写', icon: 'none' });
      }

    } catch (err) {
      wx.hideLoading();
      this.setData({ isRecognizing: false });
      console.warn('OCR识别失败:', err);
      wx.showToast({ title: '识别失败，请手动填写', icon: 'none' });
    }
  },

  nextStep() {
    const { realName, idCard, universityName, studentCardUrl } = this.data.formData;

    if (!realName || !idCard || !universityName) {
      return wx.showToast({ title: '请填写完整信息', icon: 'none' });
    }
    if (!studentCardUrl) {
      return wx.showToast({ title: '请上传学生证', icon: 'none' });
    }

    wx.setStorageSync('cert_step1', this.data.formData);

    wx.navigateTo({
      url: '../step2-ability/step2-ability'
    });
  }
});