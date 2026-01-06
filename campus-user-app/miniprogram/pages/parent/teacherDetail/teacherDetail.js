import request from '../../../utils/request';
import api from '../../../config/apiConfig';

Page({
  data: {
    tutorId: null,
    tutor: null,
    loading: true,
    // 学历映射表 (对应后端 Integer definition)
    educationMap: {
      1: '本科在读',
      2: '本科毕业',
      3: '硕士在读',
      4: '硕士毕业',
      5: '博士'
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ tutorId: options.id });
      this.fetchTutorDetail(options.id);
    }
  },

  async fetchTutorDetail(id) {
    try {
      this.setData({ loading: true });
      // 调用后端 GET /api/tutor/{id}
      const url = api.tutor.detail(id); 
      const res = await request.get(url);
      
      // 数据预处理
      const tutor = this.processTutorData(res);
      
      this.setData({ 
        tutor, 
        loading: false 
      });
    } catch (err) {
      console.error(err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  // 【核心适配】处理后端返回的数据格式
  processTutorData(data) {
    if (!data) return null;

    // 1. 解析 JSON 字符串字段 (后端存的是 String)
    const subjects = this.safeJsonParse(data.teachSubjects);
    const grades = this.safeJsonParse(data.teachGrades);
    const certs = this.safeJsonParse(data.certificateUrls);

    // 2. 计算教龄 (当前年份 - 入学年份)
    const currentYear = new Date().getFullYear();
    const enrollYear = data.enrollYear || currentYear;
    // 简单算法：假设入学即开始兼职，或者显示"大学生"
    const teachYears = currentYear - enrollYear;

    // 3. 构建显示对象
    return {
      ...data,
      // 扩展解析后的字段
      teachSubjectsList: subjects,
      teachGradesList: grades,
      certificateUrlsList: certs,
      // 映射学历文本
      educationText: this.data.educationMap[data.education] || '高等学历',
      // 教龄显示逻辑
      teachYearText: teachYears > 0 ? `${teachYears}年教龄` : '新晋教员',
      // 由于 TutorProfile 没有 avatar 字段，使用默认头像
      avatarUrl: '/static/images/default-avatar.png' 
    };
  },

  // 安全解析 JSON 字符串
  safeJsonParse(jsonStr) {
    if (!jsonStr) return [];
    try {
      // 兼容：如果已经是数组则直接返回，如果是字符串则解析
      return typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
    } catch (e) {
      console.error('JSON解析异常:', e);
      return [];
    }
  },

  // 图片预览
  previewImage(e) {
    const current = e.currentTarget.dataset.src;
    const urls = this.data.tutor.certificateUrlsList;
    if (urls && urls.length) {
      wx.previewImage({ current, urls });
    }
  },

  // 立即预约
  handleBook() {
    const { tutor } = this.data;
    if (!tutor) return;

    // 检查登录
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/common/login/login' });
      return;
    }

    // 传递必要信息到下单页
    const orderData = {
      tutorId: tutor.id,
      realName: tutor.realName,
      price: tutor.expectPrice,
      // 默认选中第一个科目，如果没有则留空
      subject: tutor.teachSubjectsList && tutor.teachSubjectsList.length > 0 ? tutor.teachSubjectsList[0] : ''
    };

    wx.navigateTo({
      url: `/pages/parent/order/confirm/confirm?data=${encodeURIComponent(JSON.stringify(orderData))}`
    });
  }
});