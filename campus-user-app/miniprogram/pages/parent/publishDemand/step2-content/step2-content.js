const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    // 表单数据 (对应 DemandPostRequest)
    form: {
      studentId: null,
      title: '',
      grade: '', // 自动从上一步带入
      subject: '',
      expectPrice: '',
      teachMode: 1, // 1上门
      address: '',
      longitude: null,
      latitude: null,
      detail: '',
      scheduleRequire: [] // 暂时留空
    },

    subjects: ['语文', '数学', '英语', '物理', '化学', '全科作业'],
    isSubmitting: false
  },

  onLoad(options) {
    if (options.data) {
      try {
        const data = JSON.parse(decodeURIComponent(options.data));
        this.setData({
          'form.studentId': data.studentId,
          'form.grade': data.grade,
          // 预设一个默认标题
          'form.title': `诚聘${data.grade}家教老师`
        });
      } catch (e) {
        console.error(e);
      }
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  handleSubjectChange(e) {
    const idx = e.detail.value;
    const subject = this.data.subjects[idx];
    this.setData({
      'form.subject': subject,
      // 优化标题体验
      'form.title': `急寻${this.data.form.grade}${subject}老师`
    });
  },

  setTeachMode(e) {
    this.setData({ 'form.teachMode': parseInt(e.currentTarget.dataset.val) });
  },

  // 选择位置 (调用微信地图SDK)
  chooseLocation() {
    const that = this;
    wx.chooseLocation({
      success(res) {
        that.setData({
          'form.address': res.address + ' ' + res.name,
          'form.latitude': res.latitude,
          'form.longitude': res.longitude
        });
      },
      fail(err) {
        console.error(err);
        wx.showToast({ title: '需要授权位置信息', icon: 'none' });
      }
    });
  },

  async submitDemand() {
    const { title, subject, expectPrice, address, longitude } = this.data.form;

    if (!title || !subject || !expectPrice) {
      return wx.showToast({ title: '请完善核心信息', icon: 'none' });
    }
    // 如果选了上门(1)或均可(3)，必须填地址
    if (this.data.form.teachMode !== 2 && !longitude) {
      return wx.showToast({ title: '请选择上课地点', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    try {
      // 对应 DemandController.publish
      await request.post(api.demand.publish, this.data.form);

      wx.showToast({ title: '发布成功', icon: 'success' });

      // 跳转到需求列表 (需要先开发该页，或者跳回首页)
      setTimeout(() => {
        // wx.reLaunch({ url: '/pages/demand/my/myList' }); // 暂未开发
        wx.navigateBack({ delta: 2 }); // 临时：返回上一级
      }, 1500);

    } catch (err) {
      console.error(err);
      this.setData({ isSubmitting: false });
    }
  }
});