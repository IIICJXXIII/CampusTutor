const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    phone: '',
    password: '',
    code: '',
    loginType: 'password', // password | code
    showPassword: false,   // 密码可见状态
    isSubmitting: false,
    loginCode: '' // 微信登录code，用于后续获取手机号
  },

  onLoad() {
    // 页面加载时静默获取login_code
    this.getLoginCode();
  },

  // 静默获取微信login_code
  getLoginCode() {
    wx.login({
      timeout: 5000,
      success: (res) => {
        if (res.code) {
          this.setData({ loginCode: res.code });
          console.log('静默获取login_code成功:', res.code.substring(0, 10) + '...');
        }
      },
      fail: (err) => {
        console.error('获取login_code失败:', err);
      }
    });
  },

  // 切换密码可见性
  togglePasswordVisibility() {
    this.setData({ showPassword: !this.data.showPassword });
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
      // 兼容处理：后端使用 @RequestParam 接收 phone，直接把 phone 放到查询参数中
      await request.post(api.auth.sendCode + '?phone=' + encodeURIComponent(this.data.phone));

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
        
        // 4. 对于教师角色，自动设置认证已提交标志
        // 因为后端在开发阶段会直接通过认证
        if (res.role === 1) {
          wx.setStorageSync('certificationSubmitted', true);
        }
        
        wx.showToast({ title: '登录成功', icon: 'success' });

        // 5. 登录成功后跳转到首页
        setTimeout(() => {
          wx.switchTab({ url: '/pages/common/index/index' });
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
  ,

  // 处理获取手机号回调（新的一键登录流程）
  async handleGetPhoneNumber(e) {
    console.log('handleGetPhoneNumber事件详情:', e);
    
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      console.error('获取手机号失败:', e.detail.errMsg);
      
      // 更详细的错误提示
      let errorMsg = '用户拒绝授权';
      if (e.detail.errMsg.includes('fail')) {
        errorMsg = '获取手机号失败，请检查小程序权限配置';
      } else if (e.detail.errMsg.includes('cancel')) {
        errorMsg = '用户取消了授权';
      }
      
      wx.showModal({
        title: '提示',
        content: errorMsg + '\n\n可能的原因：\n1. 小程序未开通获取手机号权限\n2. 用户拒绝了授权\n3. 网络异常',
        showCancel: false,
        confirmText: '知道了'
      });
      return;
    }

    const phoneCode = e.detail.code; // 获取手机号的动态凭证
    const loginCode = this.data.loginCode; // 第一步静默获取的login_code

    console.log('获取到phoneCode:', phoneCode ? phoneCode.substring(0, 10) + '...' : 'null');
    console.log('当前loginCode:', loginCode ? loginCode.substring(0, 10) + '...' : 'null');

    if (!phoneCode) {
      wx.showToast({ title: '获取手机号失败，未获取到code', icon: 'none' });
      return;
    }

    if (!loginCode) {
      // 如果没有login_code，重新获取
      console.log('loginCode为空，重新获取...');
      await this.getLoginCode();
      if (!this.data.loginCode) {
        wx.showToast({ title: '登录凭证获取失败，请重试', icon: 'none' });
        return;
      }
    }

    wx.showLoading({ title: '微信登录中...', mask: true });

    try {
      // 调用新的后端接口，传递两个code
      const payload = {
        loginCode: this.data.loginCode,
        phoneCode: phoneCode
      };

      console.log('发送登录请求，payload:', {
        loginCode: payload.loginCode ? payload.loginCode.substring(0, 10) + '...' : 'null',
        phoneCode: payload.phoneCode ? payload.phoneCode.substring(0, 10) + '...' : 'null'
      });

      // 调用新的微信手机号一键登录接口
      const r = await request.post(api.auth.wxPhoneLogin, payload);
      console.log('后端响应:', r);

      if (r && r.token) {
        wx.setStorageSync('token', r.token);
        wx.setStorageSync('userInfo', r);
        
        // 存储微信openid
        if (r.openid) {
          wx.setStorageSync('openid', r.openid);
        }
        
        // 对于教师角色，自动设置认证已提交标志
        if (r.role === 1) {
          wx.setStorageSync('certificationSubmitted', true);
        }
        
        wx.hideLoading();
        wx.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => wx.switchTab({ url: '/pages/common/index/index' }), 800);
      } else if (r && r.needBind) {
        // 后端表示需要绑定手机号或完善信息
        wx.hideLoading();
        wx.showModal({ 
          title: '需绑定手机号', 
          content: '请先绑定手机号或完善账号信息', 
          showCancel: false, 
          success() {
            wx.navigateTo({ url: '/pages/common/register/register?from=wx' });
          }
        });
      } else {
        wx.hideLoading();
        throw new Error('微信登录响应异常，未获取到token');
      }

    } catch (err) {
      console.error('微信一键登录失败详情:', err);
      wx.hideLoading();
      
      let errorMessage = err.msg || err.message || '微信登录失败';
      
      // 更友好的错误提示
      if (errorMessage.includes('network') || errorMessage.includes('Network')) {
        errorMessage = '网络连接失败，请检查网络后重试';
      } else if (errorMessage.includes('timeout')) {
        errorMessage = '请求超时，请稍后重试';
      } else if (errorMessage.includes('config')) {
        errorMessage = '小程序配置错误，请检查微信配置';
      }
      
      wx.showModal({
        title: '登录失败',
        content: errorMessage + '\n\n建议：\n1. 检查网络连接\n2. 确认小程序已开通获取手机号权限\n3. 重启小程序后重试',
        showCancel: false,
        confirmText: '确定'
      });
    }
  },

  // 旧的微信登录方法（兼容性保留）
  handleWxLogin() {
    wx.showModal({
      title: '提示',
      content: '请使用上方的一键登录按钮进行微信登录',
      showCancel: false
    });
  }
});