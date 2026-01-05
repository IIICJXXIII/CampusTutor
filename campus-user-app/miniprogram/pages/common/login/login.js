// pages/common/login/login.js
const request = require('../../../utils/request.js');
const validate = require('../../../utils/validateUtil.js');

Page({
  data: {
    phone: '',
    password: ''
  },

  // 处理登录逻辑
  handleLogin() {
    const { phone, password } = this.data;

    // 1. 前端校验
    if (!validate.isPhone(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    if (validate.isEmpty(password)) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    // 2. 调用后端登录接口 [cite: 447]
    request.post('/api/auth/login', {
      phone: phone,
      password: password // 实际项目中建议使用 encryptUtil 加密后传输 [cite: 440]
    }).then(res => {
      // 3. 存储Token和用户信息 [cite: 443]
      wx.setStorageSync('token', res.token);
      wx.setStorageSync('userInfo', res.userInfo);

      wx.showToast({ title: '登录成功', icon: 'success' });

      // 4. 根据角色跳转不同首页
      // 角色定义见数据库设计 [cite: 137]
      if (res.userInfo.role === 'TEACHER') {
        wx.switchTab({ url: '/pages/teacher/index/index' });
      } else if (res.userInfo.role === 'PARENT') {
        wx.switchTab({ url: '/pages/parent/index/index' });
      }
    }).catch(err => {
      console.error('登录失败', err);
    });
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/common/register/register' });
  }
});