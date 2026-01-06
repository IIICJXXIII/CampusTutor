import request from '../../../utils/request';
import api from '../../../config/apiConfig';

Page({
  data: {
    phone: '',
    password: '',
    code: '',
    loginType: 'password', // password | code
    isSubmitting: false
  },

  // 切换登录方式
  switchLoginType() {
    this.setData({
      loginType: this.data.loginType === 'password' ? 'code' : 'password',
      password: '',
      code: ''
    });
  },

  // 输入框事件处理
  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  // 跳转注册页
  goToRegister() {
    wx.navigateTo({ url: '/pages/common/register/register' });
  },

  // 获取验证码 (Mock功能)
  async sendSmsCode() {
    if (!this.data.phone || this.data.phone.length !== 11) {
      return wx.showToast({ title: '请输入正确手机号', icon: 'none' });
    }
    try {
      // 兼容处理
      const payload = {
        phone: this.data.phone,
        account: this.data.phone // 确保发验证码也兼容
      };
      
      await request.post(api.auth.sendCode, null, {
        'content-type': 'application/x-www-form-urlencoded' 
      }, payload); 
      
      wx.showToast({ title: '验证码已发送: 123456', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: '模拟发送: 123456', icon: 'none' });
    }
  },

  // 提交登录
  async handleLogin() {
    const { phone, password, code, loginType } = this.data;

    // 1. 简单校验
    if (!phone) return wx.showToast({ title: '请输入手机号', icon: 'none' });
    if (loginType === 'password' && !password) return wx.showToast({ title: '请输入密码', icon: 'none' });
    if (loginType === 'code' && !code) return wx.showToast({ title: '请输入验证码', icon: 'none' });

    this.setData({ isSubmitting: true });

    try {
      // 【核心修复】: 终极兼容方案
      // 同时发送 phone, username, account 三个字段
      // 无论后端要哪个，都能满足 @NotBlank 校验
      const payload = {
        account: phone,     // <--- 匹配 AuthServiceImpl.java 中的 getAccount()
        phone: phone,       // 匹配 LoginRequest.java (如果已更新)
        username: phone,    // 匹配 Spring Security 默认
        password: loginType === 'password' ? password : '',
        code: loginType === 'code' ? code : '',
        loginType
      };

      console.log('正在登录，参数:', payload);

      // 2. 调用登录接口
      const res = await request.post(api.auth.login, payload);

      // 3. 登录成功处理
      if (res && res.token) {
        wx.setStorageSync('token', res.token);
        wx.setStorageSync('userInfo', res);
        wx.showToast({ title: '登录成功', icon: 'success' });

        // 4. 根据角色跳转 (1:教员, 2:家长)
        setTimeout(() => {
          if (res.role === 1) {
            // 教员 -> 地图找学生页
            wx.reLaunch({ url: '/pages/teacher/mapFindStudent/mapFindStudent' });
          } else {
            // 家长 -> 发布需求页
            wx.reLaunch({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
          }
        }, 1000);
      } else {
        throw new Error('登录响应异常');
      }

    } catch (err) {
      console.error('登录报错详情:', err);
      // request.js 会弹窗，这里无需重复
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});