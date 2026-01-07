const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    isAdding: true, // 默认新增模式(如果是新用户)
    studentList: [],
    selectedId: null,
    
    // 表单数据 (对应 StudentRequest)
    form: {
      studentName: '',
      gender: 1,
      grade: '',
      schoolName: '',
      studyDesc: '',
      weakSubjects: [] // 暂未做多选UI，传空数组
    },

    grades: ['小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', 
             '初一', '初二', '初三', '高一', '高二', '高三'],
    gradeIndex: -1
  },

  onShow() {
    this.fetchStudents();
  },

  // 获取已有学生列表
  async fetchStudents() {
    try {
      // 对应 ParentController.listStudents
      const res = await request.get(api.parent.myStudents);
      if (res && res.length > 0) {
        this.setData({ 
          studentList: res,
          isAdding: false,
          selectedId: res[0].id // 默认选中第一个
        });
      } else {
        this.setData({ isAdding: true });
      }
    } catch (err) {
      console.error(err);
    }
  },

  // 切换选中
  selectStudent(e) {
    this.setData({ selectedId: e.currentTarget.dataset.id });
  },

  // 切换模式
  toggleMode() {
    this.setData({ isAdding: !this.data.isAdding });
  },

  // 表单输入
  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },

  setGender(e) {
    this.setData({ 'form.gender': parseInt(e.currentTarget.dataset.val) });
  },

  handleGradeChange(e) {
    const idx = e.detail.value;
    this.setData({
      gradeIndex: idx,
      'form.grade': this.data.grades[idx]
    });
  },

  // 保存新学生并跳转
  async saveAndNext() {
    const { studentName, gender, grade } = this.data.form;
    if (!studentName || !grade) {
      return wx.showToast({ title: '请填写姓名和年级', icon: 'none' });
    }

    try {
      // 对应 ParentController.addStudent
      const studentId = await request.post(api.parent.student, this.data.form);
      this.goToStep2(studentId);
    } catch (err) {
      console.error(err);
    }
  },

  // 选已有学生跳转
  handleNext() {
    if (!this.data.selectedId) return;
    this.goToStep2(this.data.selectedId);
  },

  // 跳转核心逻辑
  goToStep2(studentId) {
    // 找到学生对象，把年级传给下一步自动填充
    let student = this.data.studentList.find(s => s.id === studentId);
    if (!student) {
      // 如果是刚新增的，列表里可能还没刷新，用form兜底
      student = { id: studentId, grade: this.data.form.grade };
    }
    
    const params = encodeURIComponent(JSON.stringify({
      studentId: studentId,
      grade: student.grade
    }));
    
    wx.navigateTo({
      url: `/pages/parent/publishDemand/step2-content/step2-content?data=${params}`
    });
  }
});