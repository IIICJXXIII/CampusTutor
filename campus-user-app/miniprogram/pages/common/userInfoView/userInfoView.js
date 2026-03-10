// 用户信息查看页面逻辑 - userInfoView.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil');

Page({
  data: {
    userInfo: {},
    tutorProfile: null, // 教师档案信息
    isEditing: false, // 是否编辑模式
    // 年龄段列表
    gradeList: [
      { id: 'age_4_6', name: '4-6岁' },
      { id: 'age_7_9', name: '7-9岁' },
      { id: 'age_10_12', name: '10-12岁' },
      { id: 'age_13_15', name: '13-15岁' },
      { id: 'age_16_18', name: '16-18岁' },
      { id: 'age_18_plus', name: '18岁以上' }
    ],
    // 素质教育科目列表
    subjectList: [
      { id: 'piano', name: '钢琴/乐器陪练' },
      { id: 'art', name: '美术/书法' },
      { id: 'vocal', name: '声乐/视唱练耳' },
      { id: 'pe_exam', name: '中考体育专项' },
      { id: 'racket', name: '羽毛球/网球陪练' },
      { id: 'ball', name: '篮球/足球指导' },
      { id: 'coding', name: '少儿编程(Scratch/Python)' },
      { id: 'robot', name: '机器人/3D打印' },
      { id: 'science', name: '科学实验/航模' }
    ],
    selectedGrades: [],
    selectedSubjects: [],
    expectPrice: '',
    isSaving: false
  },

  onLoad() {
    this.loadUserInfo();
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      const localUserInfo = storageUtil.getUserInfo();

      if (!localUserInfo) {
        wx.showToast({ title: '用户信息缺失', icon: 'none' });
        return;
      }

      this.setData({ userInfo: localUserInfo });

      // 从后端获取最新用户信息
      const userId = localUserInfo.id || localUserInfo.userId;
      if (userId) {
        const response = await request.get(apiConfig.user.byId(userId));
        if (response) {
          storageUtil.setUserInfo({ ...localUserInfo, ...response });
          this.setData({ userInfo: response });
        }
      }

      // 如果是教师，加载教师档案
      if (localUserInfo.role === 1) {
        await this.loadTutorProfile();
      }

    } catch (err) {
      console.error('获取用户信息失败:', err);
      const localUserInfo = storageUtil.getUserInfo();
      this.setData({ userInfo: localUserInfo || {} });
    }
  },

  // 加载教师档案
  async loadTutorProfile() {
    try {
      const profile = await request.get(apiConfig.tutor.profile);
      if (profile) {
        this.setData({ tutorProfile: profile });
        // 解析已选择的年级和科目
        this.parseTeachingSettings(profile);
      }
    } catch (err) {
      console.error('加载教师档案失败:', err);
    }
  },

  // 解析教学设置到选中状态
  parseTeachingSettings(profile) {
    let selectedGrades = [];
    let selectedSubjects = [];
    let expectPrice = '';

    console.log('解析教学设置，原始数据:', profile);

    // 解析年级
    if (profile.teachGrades) {
      try {
        let gradeNames = profile.teachGrades;
        if (typeof gradeNames === 'string') {
          gradeNames = JSON.parse(gradeNames);
        }
        console.log('解析后的年级名称:', gradeNames);

        // 遍历年级列表，检查是否在已选中
        if (Array.isArray(gradeNames)) {
          this.data.gradeList.forEach(g => {
            if (gradeNames.indexOf(g.name) !== -1) {
              selectedGrades.push(g.id);
            }
          });
        }
        console.log('匹配到的年级ID:', selectedGrades);
      } catch (e) {
        console.warn('解析年级失败:', e);
      }
    }

    // 解析科目
    if (profile.teachSubjects) {
      try {
        let subjectNames = profile.teachSubjects;
        if (typeof subjectNames === 'string') {
          subjectNames = JSON.parse(subjectNames);
        }
        console.log('解析后的科目名称:', subjectNames);

        // 遍历科目列表，检查是否在已选中
        if (Array.isArray(subjectNames)) {
          this.data.subjectList.forEach(s => {
            if (subjectNames.indexOf(s.name) !== -1) {
              selectedSubjects.push(s.id);
            }
          });
        }
        console.log('匹配到的科目ID:', selectedSubjects);
      } catch (e) {
        console.warn('解析科目失败:', e);
      }
    }

    // 解析时薪
    if (profile.expectPrice) {
      expectPrice = String(profile.expectPrice);
    }

    this.setData({ selectedGrades, selectedSubjects, expectPrice });
  },

  // 进入编辑模式
  toggleEdit() {
    this.setData({ isEditing: !this.data.isEditing });
  },

  // 切换年级
  toggleGrade(e) {
    if (!this.data.isEditing) return;
    const id = e.currentTarget.dataset.id;
    const selected = [...this.data.selectedGrades];
    const index = selected.indexOf(id);
    if (index > -1) {
      selected.splice(index, 1);
    } else {
      selected.push(id);
    }
    this.setData({ selectedGrades: selected });
  },

  // 切换科目
  toggleSubject(e) {
    if (!this.data.isEditing) return;
    const id = e.currentTarget.dataset.id;
    const selected = [...this.data.selectedSubjects];
    const index = selected.indexOf(id);
    if (index > -1) {
      selected.splice(index, 1);
    } else {
      selected.push(id);
    }
    this.setData({ selectedSubjects: selected });
  },

  // 输入时薪
  onPriceInput(e) {
    this.setData({ expectPrice: e.detail.value });
  },

  // 保存修改
  async saveProfile() {
    const { selectedGrades, selectedSubjects, expectPrice } = this.data;

    if (selectedGrades.length === 0) {
      return wx.showToast({ title: '请选择可教授年级', icon: 'none' });
    }
    if (selectedSubjects.length === 0) {
      return wx.showToast({ title: '请选择可教授科目', icon: 'none' });
    }
    if (!expectPrice || parseFloat(expectPrice) <= 0) {
      return wx.showToast({ title: '请填写期望时薪', icon: 'none' });
    }

    this.setData({ isSaving: true });

    try {
      const gradeNames = this.data.gradeList
        .filter(g => selectedGrades.includes(g.id))
        .map(g => g.name);
      const subjectNames = this.data.subjectList
        .filter(s => selectedSubjects.includes(s.id))
        .map(s => s.name);

      await request.put(apiConfig.tutor.profile, {
        teachGrades: gradeNames,
        teachSubjects: subjectNames,
        expectPrice: parseFloat(expectPrice)
      });

      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ isEditing: false });
      // 重新加载档案
      await this.loadTutorProfile();
    } catch (err) {
      console.error('保存失败:', err);
      wx.showToast({ title: '保存失败', icon: 'none' });
    } finally {
      this.setData({ isSaving: false });
    }
  },

  // 返回上一页
  navigateBack() {
    wx.navigateBack();
  }
});