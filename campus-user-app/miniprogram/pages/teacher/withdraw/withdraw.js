// 教师端提现页面
const api = require('../../../config/apiConfig.js');
const request = require('../../../utils/request.js');

Page({
  data: {
    balance: '0',
    amount: '',
    channels: [
      { id: 1, name: '微信', icon: 'wechat' },
      { id: 2, name: '支付宝', icon: 'alipay' },
      { id: 3, name: '银行卡', icon: 'bank' }
    ],
    selectedChannel: 1,
    accountNo: '',
    payPassword: '',
    isSubmitting: false,
    rules: [
      { title: '提现金额', desc: '最低1元，最高5000元' },
      { title: '手续费', desc: '每笔提现收取1元手续费' },
      { title: '到账时间', desc: '工作日1-2小时内到账，节假日顺延' }
    ]
  },

  onLoad: function () {
    this.loadWalletInfo();
  },

  // 加载钱包信息
  loadWalletInfo() {
    request.get(api.wallet.info)
      .then(res => {
        this.setData({
          balance: res.balance || '0'
        });
      })
      .catch(err => {
        wx.showToast({ title: '加载钱包信息失败', icon: 'none' });
      });
  },

  // 输入金额
  handleAmountInput(e) {
    this.setData({ amount: e.detail.value });
  },

  // 选择提现渠道
  selectChannel(e) {
    this.setData({ selectedChannel: e.currentTarget.dataset.id });
  },

  // 输入账号
  handleAccountInput(e) {
    this.setData({ accountNo: e.detail.value });
  },

  // 输入支付密码
  handlePasswordInput(e) {
    this.setData({ payPassword: e.detail.value });
  },

  // 提交提现申请
  submitWithdraw() {
    const { amount, selectedChannel, accountNo, payPassword } = this.data;

    // 验证
    if (!amount) {
      return wx.showToast({ title: '请输入提现金额', icon: 'none' });
    }

    if (parseFloat(amount) < 1) {
      return wx.showToast({ title: '提现金额不能小于1元', icon: 'none' });
    }

    if (parseFloat(amount) > 5000) {
      return wx.showToast({ title: '提现金额不能超过5000元', icon: 'none' });
    }

    if (!accountNo) {
      return wx.showToast({ title: '请输入收款账号', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    // 调用提现接口
    request.post(api.wallet.withdraw, {
      amount: amount,
      channel: selectedChannel,
      accountNo: accountNo,
      payPassword: payPassword
    })
      .then(res => {
        wx.showToast({ title: '提现申请提交成功', icon: 'success' });
        setTimeout(() => {
          wx.navigateTo({ url: '/pages/teacher/withdraw/withdrawRecord' });
        }, 1500);
      })
      .catch(err => {
        wx.showToast({ title: '提现申请失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ isSubmitting: false });
      });
  },

  // 跳转到提现记录
  goToWithdrawRecord() {
    wx.navigateTo({ url: '/pages/teacher/withdraw/withdrawRecord' });
  }
});
