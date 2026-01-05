import request from '../../../utils/request';
import api from '../../../config/apiConfig';

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
      // 模拟调用发送验证码接口 (注意：default.md显示send-code可能是post且在query里)
      await request.post(`${api.auth.sendCode}?phone=${this.data.phone}`);
      wx.showToast({ title: '验证码已发送', icon: 'none' });
      
      // 倒计时逻辑
      this.setData({ countDown: 60 });
      const timer = setInterval(() => {
        if (this.data.countDown <= 0) {
          clearInterval(timer);
        } else {
          this.setData({ countDown: this.data.countDown - 1 });
        }
      }, 1000);
    } catch (err) {
      // 即使接口失败，为了演示方便，也可以开启倒计时（视调试情况而定）
      // 真实上线请删除下面这行
      // this.setData({ countDown: 60 }); 
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
      // 调用注册接口
      const res = await request.post(api.auth.register, {
        phone,
        password,
        code,
        nickname: nickname || (role === 1 ? '新教员' : '新家长'),
        role
      });

      wx.showToast({ title: '注册成功', icon: 'success' });
      
      // 注册成功后自动登录，存储 Token
      // 注意：根据API文档，register接口返回的数据里包含token和用户信息
      if (res && res.token) {
          wx.setStorageSync('token', res.token);
          wx.setStorageSync('userInfo', res);
      }

      // 【核心修改】：根据角色跳转不同页面
      setTimeout(() => {
        if (role === 1) {
          // 教员 -> 跳转到认证第一步
          wx.reLaunch({ url: '/pages/teacher/certification/step1-base/step1-base' });
        } else {
          // 家长 -> 跳转到发布需求页
          wx.reLaunch({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
        }
      }, 1500);

    } catch (err) {
      console.error('注册失败', err);
      // request.js 已经弹窗提示错误 msg，这里不用重复弹窗
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  goLogin() {
    wx.navigateBack();
  }
});