// AI教案生成页面逻辑
const api = require('../../../config/apiConfig.js');

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
            url: api.llm.lessonPlan,
            method: 'POST',
            data: {
                subject,
                studentLevel,
                lessonDuration: duration,
                studentInfo: studentInfo || '无特殊情况'
            },
            header: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + (wx.getStorageSync('token') || '')
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 200) {
                    this.setData({
                        result: res.data.data || ''
                    });
                    if (!res.data.data) {
                        wx.showToast({ title: 'AI返回内容为空，请重试', icon: 'none' });
                    }
                } else {
                    wx.showToast({
                        title: res.data.msg || '生成失败，请稍后重试',
                        icon: 'none'
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请检查连接',
                    icon: 'none'
                });
            },
            complete: () => {
                this.setData({ loading: false });
            }
        });
    },
  
  copyResult() {
    if (!this.data.result) return;
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