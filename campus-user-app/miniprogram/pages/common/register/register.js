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
      // 模拟调用发送验证码接口
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
      console.error(err);
      // 开发测试阶段，即使接口失败也允许倒计时，方便调试UI
      // this.setData({ countDown: 60 }); 
    }
  },

  // 提交注册 (核心修复部分)
  async handleRegister() {
    const { phone, password, code, nickname, role } = this.data;

    // 1. 表单校验
    if (!phone || !password || !code) {
      return wx.showToast({ title: '请填写完整信息', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    try {
      // 2. 调用注册接口
      const res = await request.post(api.auth.register, {
        phone,
        password,
        code,
        nickname: nickname || (role === 1 ? '新教员' : '新家长'),
        role
      });

      console.log('【调试】注册接口原始返回:', res); 

      // 3. 健壮的 Token 提取逻辑
      // 解释：request.js 可能直接返回 data，也可能返回 {code, msg, data}
      // 我们这里做兼容处理，确保一定能拿到内部的 token
      let token = null;
      let userInfo = null;

      if (res && res.token) {
        // 情况A: res 就是 payload (最常见)
        token = res.token;
        userInfo = res;
      } else if (res && res.data && res.data.token) {
        // 情况B: res 是外层包装
        token = res.data.token;
        userInfo = res.data;
      }

      if (token) {
        // 4. 注册成功且拿到Token -> 存缓存
        console.log('【调试】成功获取并存储 Token:', token);
        wx.setStorageSync('token', token);
        wx.setStorageSync('userInfo', userInfo);
        
        wx.showToast({ title: '注册成功', icon: 'success' });

        // 5. 根据角色跳转
        setTimeout(() => {
          if (role === 1) {
            // 教员 -> 跳转到认证第一步
            wx.reLaunch({ url: '/pages/teacher/certification/step1-base/step1-base' });
          } else {
            // 家长 -> 跳转到发布需求页
            wx.reLaunch({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
          }
        }, 1500);

      } else {
        // 异常情况：注册成功了但没给Token
        console.error('【严重警告】注册响应中缺失Token字段，响应内容:', res);
        wx.showToast({ title: '注册成功，请登录', icon: 'none' });
        
        // 兜底方案：跳回登录页让用户手动登录
        setTimeout(() => {
          wx.reLaunch({ url: '/pages/common/login/login' });
        }, 1500);
      }

    } catch (err) {
      console.error('注册请求失败:', err);
      // request.js 应该已经弹窗提示了错误信息
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  goLogin() {
    wx.navigateBack();
  }
});