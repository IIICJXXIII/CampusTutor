// pages/common/register/register.js
const request = require('../../../utils/request.js');
const validate = require('../../../utils/validateUtil.js');

Page({
  data: {
    role: 'PARENT', // 默认角色：家长
    phone: '',
    password: '',
    confirmPassword: ''
  },

  // 切换角色
  switchRole(e) {
    const role = e.currentTarget.dataset.role;
    this.setData({ role });
  },

  // 处理注册
  handleRegister() {
    const { phone, password, confirmPassword, role } = this.data;

    // 1. 表单校验
    if (!validate.isPhone(phone)) {
      wx.showToast({ title: '手机号格式错误', icon: 'none' });
      return;
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码不能少于6位', icon: 'none' });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }

    // 2. 发送请求 (模拟)
    const postData = {
      phone,
      password, // 实际开发建议前端加密或HTTPS传输
      role,
      username: role === 'PARENT' ? '新用户家长' : '新用户教师' // 默认昵称
    };

    // 这里调用后端注册接口
    // request.post('/api/auth/register', postData).then...
    // 演示阶段模拟成功：
    wx.showLoading({ title: '注册中...' });
    
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({ title: '注册成功', icon: 'success' });
      
      // 延迟跳转回登录页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }, 1000);
  },

  // 返回登录页
  goToLogin() {
    wx.navigateBack();
  }
});