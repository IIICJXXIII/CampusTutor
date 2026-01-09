const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    role: 2, // 默认家长 (1:教员, 2:家长)
    phone: '',
    password: '',
    code: '',
    nickname: '',
    countDown: 0,
    isSubmitting: false
  },

  // 切换角色
  selectRole(e) {
    const role = parseInt(e.currentTarget.dataset.role);
    this.setData({ role });
  },

  // 输入框处理
  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  // 发送验证码
  async sendSmsCode() {
    if (this.data.countDown > 0) return;
    if (!this.data.phone || this.data.phone.length !== 11) {
      return wx.showToast({ title: '请输入正确手机号', icon: 'none' });
    }

    try {
      await request.post(`${api.auth.sendCode}?phone=${this.data.phone}`);
      wx.showToast({ title: '验证码已发送', icon: 'none' });

      this.setData({ countDown: 60 });
      const timer = setInterval(() => {
        if (this.data.countDown <= 0) {
          clearInterval(timer);
        } else {
          this.setData({ countDown: this.data.countDown - 1 });
        }
      }, 1000);
    } catch (err) {
      console.error(err);
    }
  },

  // 提交注册
  async handleRegister() {
    const { phone, password, code, nickname, role } = this.data;

    if (!phone || !password || !code) {
      return wx.showToast({ title: '请填写完整信息', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    try {
      const res = await request.post(api.auth.register, {
        phone,
        password,
        code,
        nickname: nickname || (role === 1 ? '新教员' : '新家长'),
        role
      });

      console.log('【调试】注册接口返回:', res);

      let token = null;
      let userInfo = null;

      if (res && res.token) {
        token = res.token;
        userInfo = res;
      } else if (res && res.data && res.data.token) {
        token = res.data.token;
        userInfo = res.data;
      }

      if (token) {
        wx.setStorageSync('token', token);
        wx.setStorageSync('userInfo', userInfo);

        wx.showToast({ title: '注册成功', icon: 'success' });

        setTimeout(() => {
          if (role === 1) {
            // 教员 -> 跳转到认证第一步
            wx.reLaunch({ url: '/pages/teacher/certification/step1-base/step1-base' });
          } else {
            // 家长 -> 跳转到首页
            wx.switchTab({ url: '/pages/common/index/index' });
          }
        }, 1500);

      } else {
        console.error('注册响应中缺失Token字段:', res);
        wx.showToast({ title: '注册成功，请登录', icon: 'none' });
        setTimeout(() => {
          wx.reLaunch({ url: '/pages/common/login/login' });
        }, 1500);
      }

    } catch (err) {
      console.error('注册请求失败:', err);
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  goLogin() {
    wx.navigateBack();
  }
});