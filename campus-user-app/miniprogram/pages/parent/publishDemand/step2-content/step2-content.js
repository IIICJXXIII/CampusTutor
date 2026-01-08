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
      const res = await request.post(api.demand.publish, this.data.form);
      
      wx.showToast({ title: '发布成功', icon: 'success' });
      
      // 发布后询问是否查看匹配老师
      const demandId = res && res.id ? res.id : null;
      const that = this;

      // 验证：确保需求包含坐标并可被附近搜索检索（用于演示）
      if (demandId) {
        try {
          const detail = await request.get(api.demand.detail(demandId));
          // 如果没有坐标，提醒用户补全位置以便老师能看到
          if (!detail || !detail.longitude || !detail.latitude) {
            wx.showModal({
              title: '提示',
              content: '您尚未填写具体上课位置，老师无法在地图上看到该需求，是否现在去选择位置？',
              confirmText: '去选择',
              cancelText: '稍后',
              success(cm) {
                if (cm.confirm) {
                  // 打开位置选取
                  that.chooseLocation();
                }
                // 无论是否选择，弹窗后询问是否查看匹配老师
                setTimeout(showViewModal, 300);
              }
            });
          } else {
            // 有坐标，试探性调用 nearby 测试是否能检索到自己（小半径）
            try {
              const nearby = await request.get(api.demand.nearby, {
                longitude: detail.longitude,
                latitude: detail.latitude,
                radius: 1
              });
              const found = (nearby || []).some(d => d.id === demandId);
              if (!found) {
                wx.showToast({ title: '已发布，但短时间内在地图上可能无法检索(索引延迟或服务问题)，请稍后刷新', icon: 'none', duration: 3000 });
              }
            } catch (e) {
              // nearby 调用失败（可能Redis或服务问题），告知用户
              wx.showToast({ title: '已发布，但地图检索服务暂不可用，匹配可能受影响', icon: 'none', duration: 3000 });
            }
            // 然后询问是否查看匹配老师
            setTimeout(showViewModal, 300);
          }
        } catch (err) {
          console.error('获取需求详情失败', err);
          // 依然询问查看匹配老师
          setTimeout(showViewModal, 300);
        }
      } else {
        // 无id也弹窗
        setTimeout(showViewModal, 300);
      }

      function showViewModal() {
        wx.showModal({
          title: '已发布',
          content: '是否查看系统为您匹配的老师？',
          confirmText: '查看',
          cancelText: '稍后',
          success(modalRes) {
            if (modalRes.confirm) {
              const params = [];
              if (that.data.form.subject) params.push(`subject=${encodeURIComponent(that.data.form.subject)}`);
              if (that.data.form.grade) params.push(`grade=${encodeURIComponent(that.data.form.grade)}`);
              if (that.data.form.longitude) params.push(`longitude=${that.data.form.longitude}`);
              if (that.data.form.latitude) params.push(`latitude=${that.data.form.latitude}`);
              if (demandId) params.push(`demandId=${demandId}`);
              wx.navigateTo({ url: `/pages/parent/matchResult/matchResult?${params.join('&')}` });
            } else {
              wx.navigateBack({ delta: 2 });
            }
          }
        });
      }

    } catch (err) {
      console.error(err);
      this.setData({ isSubmitting: false });
    }
  }
});