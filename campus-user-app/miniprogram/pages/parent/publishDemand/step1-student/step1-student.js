import request from '../../../../utils/request';
import api from '../../../../config/apiConfig';

Page({
  data: {
    grades: ['小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', '初一', '初二', '初三', '高一', '高二', '高三'],
    gradeIndex: -1,
    
    // 科目列表 (带状态)
    subjects: [
      { name: '数学', selected: false },
      { name: '英语', selected: false },
      { name: '语文', selected: false },
      { name: '物理', selected: false },
      { name: '化学', selected: false },
      { name: '生物', selected: false },
      { name: '全科辅导', selected: false }
    ],

    formData: {
      studentName: '',
      gender: 1, // 1男 2女
      grade: '',
      weakSubjects: [], // 存储选中的科目名称
      studyDesc: ''
    },
    
    isSubmitting: false
  },

  onLoad() {
    // 检查是否有草稿缓存，回显数据
    const draft = wx.getStorageSync('demand_draft_step1');
    if (draft) {
      this.setData({ formData: draft });
      this.restoreUIState(draft);
    }
  },

  // 恢复UI状态 (Picker索引和Tag选中)
  restoreUIState(data) {
    // 恢复年级索引
    if (data.grade) {
      const idx = this.data.grades.indexOf(data.grade);
      this.setData({ gradeIndex: idx });
    }
    // 恢复科目选中状态
    if (data.weakSubjects && data.weakSubjects.length > 0) {
      const newSubjects = this.data.subjects.map(sub => ({
        ...sub,
        selected: data.weakSubjects.includes(sub.name)
      }));
      this.setData({ subjects: newSubjects });
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`formData.${field}`]: e.detail.value });
  },

  selectGender(e) {
    const val = parseInt(e.currentTarget.dataset.val);
    this.setData({ 'formData.gender': val });
  },

  handleGradeChange(e) {
    const idx = parseInt(e.detail.value);
    this.setData({
      gradeIndex: idx,
      'formData.grade': this.data.grades[idx]
    });
  },

  toggleSubject(e) {
    const index = e.currentTarget.dataset.index;
    const subjects = this.data.subjects;
    subjects[index].selected = !subjects[index].selected;
    
    // 更新 formData 中的 weakSubjects 数组
    const selectedNames = subjects.filter(s => s.selected).map(s => s.name);
    
    this.setData({
      subjects: subjects,
      'formData.weakSubjects': selectedNames
    });
  },

  async nextStep() {
    const { studentName, grade, weakSubjects } = this.data.formData;

    // 1. 基础校验
    if (!studentName) return wx.showToast({ title: '请填写学生称呼', icon: 'none' });
    if (!grade) return wx.showToast({ title: '请选择年级', icon: 'none' });
    if (weakSubjects.length === 0) return wx.showToast({ title: '请至少选一个科目', icon: 'none' });

    this.setData({ isSubmitting: true });

    try {
      // 2. 调用API创建学生档案，获取 studentId
      // 注意：根据 API 文档，POST /api/parent/student 返回 Long (studentId)
      // 如果后端设计是更新或创建，这里也可以先判断是否已有 studentId
      
      const studentId = await request.post(api.parent.student, {
        studentName: this.data.formData.studentName,
        gender: this.data.formData.gender,
        grade: this.data.formData.grade,
        weakSubjects: this.data.formData.weakSubjects,
        studyDesc: this.data.formData.studyDesc
      });

      // 3. 将关键数据存入本地，供 Step 2 和 3 使用
      // 我们把 studentId 和 grade 存起来，匹配时用
      const demandDraft = {
        studentId: studentId, 
        grade: this.data.formData.grade,
        subject: this.data.formData.weakSubjects[0] // 默认取第一个科目作为主需求科目
      };
      
      wx.setStorageSync('demand_draft_step1', this.data.formData); // 回显用
      wx.setStorageSync('current_demand_data', demandDraft); // 流程数据用

      // 4. 跳转
      wx.navigateTo({
        url: '../step2-teaching/step2-teaching'
      });

    } catch (err) {
      console.error(err);
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});