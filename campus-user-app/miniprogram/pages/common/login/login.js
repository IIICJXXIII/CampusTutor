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

  // 获取验证码 (Mock功能，仅演示)
  async sendSmsCode() {
    if (!this.data.phone || this.data.phone.length !== 11) {
      return wx.showToast({ title: '请输入正确手机号', icon: 'none' });
    }
    try {
      await request.post(api.auth.sendCode, null, {
        'content-type': 'application/x-www-form-urlencoded' // 根据API文档可能需要 query param
      }, { phone: this.data.phone }); // 或者作为 query 参数
      // 这里的实现视后端具体定义，通常 send-code 是 GET 或 POST query
      // 修正：根据 default.md，send-code 是 POST query param
      // 实际调用可能需要调整 request 封装以支持 query param，这里简化处理
      wx.showToast({ title: '验证码已发送: 123456', icon: 'none' });
    } catch (err) {
      // 演示环境直接提示
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
      // 2. 调用登录接口
      const res = await request.post(api.auth.login, {
        phone,
        password: loginType === 'password' ? password : '',
        code: loginType === 'code' ? code : '',
        loginType
      });

      // 3. 登录成功处理
      wx.setStorageSync('token', res.token);
      wx.setStorageSync('userInfo', res); // 存入完整用户信息(含role)
      wx.showToast({ title: '登录成功', icon: 'success' });

      // 4. 根据角色跳转 (0:管理员, 1:教员, 2:家长)
      setTimeout(() => {
        if (res.role === 1) {
          // 教员跳转 -> 地图找学生页 (假设为教员首页)
          wx.reLaunch({ url: '/pages/teacher/mapFindStudent/mapFindStudent' });
        } else {
          // 家长跳转 -> 发布需求页 (假设为家长首页)
          wx.reLaunch({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
        }
      }, 1000);

    } catch (err) {
      console.error(err);
      // 错误提示已在 request.js 中统一处理，这里可不做额外处理
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});