Page({
  data: {
    subjects: ['数学', '英语', '语文', '物理', '化学', '生物', '全科'],
    subjectIndex: -1,
    
    // 时间段选项
    timeSlots: [
      { name: '周一至周五晚', selected: false },
      { name: '周六上午', selected: false },
      { name: '周六下午', selected: false },
      { name: '周六晚上', selected: false },
      { name: '周日上午', selected: false },
      { name: '周日下午', selected: false },
      { name: '周日晚上', selected: false },
      { name: '协商确定', selected: false }
    ],

    formData: {
      subject: '',
      teachMode: 1, // 1上门, 2在线
      expectPrice: '',
      scheduleRequire: [], // 选中的时间段
      detail: ''
    }
  },

  onLoad() {
    // 1. 获取 Step 1 的数据，看是否有预选科目
    const step1Data = wx.getStorageSync('demand_draft_step1');
    const flowData = wx.getStorageSync('current_demand_data'); // 流程总数据

    // 如果 Step 1 选了薄弱科目，默认选中第一个
    if (step1Data && step1Data.weakSubjects && step1Data.weakSubjects.length > 0) {
      const defaultSub = step1Data.weakSubjects[0];
      const idx = this.data.subjects.indexOf(defaultSub);
      
      if (idx !== -1) {
        this.setData({
          subjectIndex: idx,
          'formData.subject': defaultSub
        });
      } else {
        // 如果 Step 1 的科目不在标准列表里，加入列表并选中
        const newSubjects = [...this.data.subjects, defaultSub];
        this.setData({
          subjects: newSubjects,
          subjectIndex: newSubjects.length - 1,
          'formData.subject': defaultSub
        });
      }
    }

    // 2. 回显本步骤的草稿 (如果用户是返回修改的)
    const step2Draft = wx.getStorageSync('demand_draft_step2');
    if (step2Draft) {
      this.setData({ formData: step2Draft });
      // 恢复 picker
      if (step2Draft.subject) {
        const idx = this.data.subjects.indexOf(step2Draft.subject);
        this.setData({ subjectIndex: idx });
      }
      // 恢复时间标签
      if (step2Draft.scheduleRequire) {
        const newSlots = this.data.timeSlots.map(slot => ({
          ...slot,
          selected: step2Draft.scheduleRequire.includes(slot.name)
        }));
        this.setData({ timeSlots: newSlots });
      }
    }
  },

  handleSubjectChange(e) {
    const idx = parseInt(e.detail.value);
    this.setData({
      subjectIndex: idx,
      'formData.subject': this.data.subjects[idx]
    });
  },

  selectMode(e) {
    this.setData({ 'formData.teachMode': parseInt(e.currentTarget.dataset.val) });
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`formData.${field}`]: e.detail.value });
  },

  toggleTime(e) {
    const index = e.currentTarget.dataset.index;
    const slots = this.data.timeSlots;
    slots[index].selected = !slots[index].selected;
    
    const selectedNames = slots.filter(s => s.selected).map(s => s.name);
    this.setData({
      timeSlots: slots,
      'formData.scheduleRequire': selectedNames
    });
  },

  prevStep() {
    wx.navigateBack();
  },

  nextStep() {
    const { subject, expectPrice, scheduleRequire } = this.data.formData;

    // 校验
    if (!subject) return wx.showToast({ title: '请选择辅导科目', icon: 'none' });
    if (!expectPrice) return wx.showToast({ title: '请输入期望课酬', icon: 'none' });
    
    // 保存当前步骤数据
    wx.setStorageSync('demand_draft_step2', this.data.formData);

    // 合并到总流程数据中
    let flowData = wx.getStorageSync('current_demand_data') || {};
    flowData = {
      ...flowData,
      subject: subject,
      teachMode: this.data.formData.teachMode,
      expectPrice: parseFloat(expectPrice),
      scheduleRequire: scheduleRequire, // 数组
      detail: this.data.formData.detail
    };
    wx.setStorageSync('current_demand_data', flowData);

    // 跳转 Step 3
    wx.navigateTo({
      url: '../step3-preference/step3-preference'
    });
  }
});