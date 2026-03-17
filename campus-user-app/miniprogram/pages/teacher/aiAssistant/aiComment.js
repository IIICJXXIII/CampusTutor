// AI评语润色页面逻辑
const apiConfig = require('../../../config/apiConfig.js');

Page({
  data: {
    form: {
      rawComment: '',
      subject: '',
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
  
  polishComment() {
    const { rawComment, subject, studentInfo } = this.data.form;
    
    if (!rawComment || !subject) {
      wx.showToast({
        title: '请填写原始评语和科目',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    
    wx.request({
      url: getApp().globalData.baseUrl + '/api/llm/lesson/comment',
      method: 'POST',
      data: {
        rawComment,
        subject,
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
            title: res.data.message || '润色失败',
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