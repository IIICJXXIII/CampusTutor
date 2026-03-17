// AI教案生成页面逻辑
const apiConfig = require('../../../config/apiConfig.js');

Page({
  data: {
    form: {
      subject: '',
      studentLevel: '',
      duration: '60分钟',
      studentInfo: ''
    },
    result: '',
    loading: false
  },
  
  onLoad(options) {
    // 可以从参数中获取课时信息
    if (options.lessonId) {
      console.log('Lesson ID:', options.lessonId);
      // 这里可以加载课时相关信息
    }
  },
  
  bindInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },
  
  generatePlan() {
    const { subject, studentLevel, duration, studentInfo } = this.data.form;
    
    if (!subject || !studentLevel) {
      wx.showToast({
        title: '请填写科目和学生水平',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    
    wx.request({
      url: getApp().globalData.baseUrl + '/api/llm/lesson/plan',
      method: 'POST',
      data: {
        subject,
        studentLevel,
        lessonDuration: duration,
        studentInfo
      },
      header: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            result: res.data.data
          });
        } else {
          wx.showToast({
            title: res.data.message || '生成失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },
  
  copyResult() {
    wx.setClipboardData({
      data: this.data.result,
      success: () => {
        wx.showToast({
          title: '复制成功'
        });
      }
    });
  }
});