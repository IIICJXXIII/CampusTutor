const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

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
    },
    // 课表相关
    weekDays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    timeSlots: [
      { label: '08:00', startTime: '08:00', endTime: '08:40' },
      { label: '08:50', startTime: '08:50', endTime: '09:30' },
      { label: '09:40', startTime: '09:40', endTime: '10:20' },
      { label: '10:30', startTime: '10:30', endTime: '11:10' },
      { label: '11:20', startTime: '11:20', endTime: '12:00' },
      { label: '14:00', startTime: '14:00', endTime: '14:40' },
      { label: '14:50', startTime: '14:50', endTime: '15:30' },
      { label: '15:40', startTime: '15:40', endTime: '16:20' },
      { label: '16:30', startTime: '16:30', endTime: '17:10' },
      { label: '17:20', startTime: '17:20', endTime: '18:00' },
      { label: '19:00', startTime: '19:00', endTime: '19:40' },
      { label: '19:50', startTime: '19:50', endTime: '20:30' },
      { label: '20:40', startTime: '20:40', endTime: '21:20' }
    ],
    scheduleData: [], // 教师可用时段 [slotIndex][dayIndex] = true/false
    bookingData: [],   // 家长选择的预约时段
    // 联系功能相关
    showContactModal: false, // 联系模态窗口显示状态
    tutorPhone: '' // 教师手机号
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ tutorId: options.id });
      this.initScheduleData();
      this.fetchTutorDetail(options.id);
      this.loadTutorSchedule(options.id);
    }
  },

  // 初始化课表数据结构
  initScheduleData() {
    const data = [];
    const booking = [];
    for (let i = 0; i < this.data.timeSlots.length; i++) {
      data.push(new Array(7).fill(false));
      booking.push(new Array(7).fill(false));
    }
    this.setData({ scheduleData: data, bookingData: booking });
  },

  // 加载教师课表
  async loadTutorSchedule(tutorId) {
    try {
      // 暂时注释掉不存在的API调用
      // const result = await request.get(`${api.host}/api/tutor/${tutorId}/schedule`);
      // if (result && Array.isArray(result)) {
      //   this.parseScheduleFromServer(result);
      // }
      console.log('课表API暂时不可用，使用模拟数据');
      // 使用模拟数据
      const mockData = [];
      this.parseScheduleFromServer(mockData);
    } catch (err) {
      console.error('加载课表失败:', err);
    }
  },

  // 解析服务器返回的课表
  parseScheduleFromServer(serverData) {
    const data = [];
    for (let i = 0; i < this.data.timeSlots.length; i++) {
      data.push(new Array(7).fill(false));
    }
    serverData.forEach(item => {
      if (item.available === 1) {
        const dayIndex = item.dayOfWeek - 1;
        const slotIndex = this.matchTimeSlot(item.startTime);
        if (slotIndex !== -1 && dayIndex >= 0 && dayIndex < 7) {
          data[slotIndex][dayIndex] = true;
        }
      }
    });
    this.setData({ scheduleData: data });
  },

  matchTimeSlot(startTime) {
    const slots = this.data.timeSlots;
    for (let i = 0; i < slots.length; i++) {
      if (slots[i].startTime === startTime) return i;
    }
    return -1;
  },

  // 判断时段是否可用
  isSlotAvailable(slotIdx, dayIdx) {
    return this.data.scheduleData[slotIdx] && this.data.scheduleData[slotIdx][dayIdx];
  },

  // 判断时段是否被选择
  isSlotBooked(slotIdx, dayIdx) {
    return this.data.bookingData[slotIdx] && this.data.bookingData[slotIdx][dayIdx];
  },

  // 获取单元格样式
  getBookingCellClass(slotIdx, dayIdx) {
    if (this.isSlotBooked(slotIdx, dayIdx)) return 'selected';
    if (this.isSlotAvailable(slotIdx, dayIdx)) return 'available';
    return 'unavailable';
  },

  // 切换预约选择
  toggleBooking(e) {
    const { slot, day } = e.currentTarget.dataset;
    // 只能选择可用时段
    if (!this.isSlotAvailable(slot, day)) {
      wx.showToast({ title: '该时段不可预约', icon: 'none' });
      return;
    }
    const data = this.data.bookingData;
    data[slot][day] = !data[slot][day];
    this.setData({ bookingData: data });
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

  // 处理图片加载错误
  handleImageError(e) {
    const { index } = e.currentTarget.dataset;
    const urls = [...this.data.tutor.certificateUrlsList];
    urls[index] = '/static/images/default-cert.png';

    const tutor = { ...this.data.tutor };
    tutor.certificateUrlsList = urls;

    this.setData({ tutor });
  },

  // 联系教师：跳转到聊天页面
  async handleContactTeacher() {
    const { tutor } = this.data;
    if (!tutor || !tutor.userId) {
      wx.showToast({ title: '无法获取教师信息', icon: 'none' });
      return;
    }

    // 检查登录
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/common/login/login' });
      return;
    }

    wx.navigateTo({
      url: `/pages/common/chatDetail/chatDetail?userId=${tutor.userId}&nickname=${encodeURIComponent(tutor.realName || '教师')}&avatar=${encodeURIComponent(tutor.avatarUrl || '')}`
    });
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

    // 传递必要信息到下单页，确保价格不为undefined
    const orderData = {
      tutorId: tutor.id,
      realName: tutor.realName,
      price: tutor.expectPrice || 0,
      // 默认选中第一个科目，如果没有则留空
      subject: tutor.teachSubjectsList && tutor.teachSubjectsList.length > 0 ? tutor.teachSubjectsList[0] : ''
    };

    wx.navigateTo({
      url: `/pages/parent/order/confirm/confirm?data=${encodeURIComponent(JSON.stringify(orderData))}`
    });
  }
});