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
      // 调用后端 API 获取教师课表
      console.log('加载教师课表，tutorId:', tutorId);
      console.log('调用API:', api.tutor.publicSchedule(tutorId));

      // 添加重试机制
      const result = await this.retryRequest(() =>
        request.get(api.tutor.publicSchedule(tutorId)),
        3, // 最多重试3次
        1000 // 重试间隔1秒
      );
      console.log('获取到的课表数据:', result);

      if (result && Array.isArray(result) && result.length > 0) {
        // 有课表数据，解析并显示
        this.parseScheduleFromServer(result);
      } else {
        // 无课表数据或为空，设置所有时段为可用（让家长可以预约）
        console.log('课表数据为空，设置所有时段为可用');
        this.setAllSlotsAvailable();
      }
    } catch (err) {
      console.error('加载课表API失败:', err);
      // API调用失败时，设置所有时段为可用（确保家长可以正常预约）
      // 这是优雅降级策略：宁可让家长多选，不可让家长无法预约
      console.log('API调用失败，设置所有时段为可用作为后备方案');
      this.setAllSlotsAvailable();
    }
  },

  // 设置所有时段为可用（API失败或无数据时的后备方案）
  setAllSlotsAvailable() {
    const data = [];
    const timeSlots = this.data.timeSlots || [];
    for (let i = 0; i < timeSlots.length; i++) {
      data.push(new Array(7).fill(true)); // 全部设置为可用
    }
    this.setData({ scheduleData: data });
    console.log('已将所有时段设置为可用，共', data.length, '行 x 7列');
  },


  // 解析服务器返回的课表
  parseScheduleFromServer(serverData) {
    console.log('开始解析课表数据:', serverData);
    const data = [];
    const timeSlots = this.data.timeSlots || [];
    const timeSlotsCount = timeSlots.length;
    console.log('timeSlots数量:', timeSlotsCount);

    // 初始化二维数组
    for (let i = 0; i < timeSlotsCount; i++) {
      data.push(new Array(7).fill(false));
    }

    if (serverData && Array.isArray(serverData)) {
      console.log('课表数据是数组，长度:', serverData.length);
      serverData.forEach((item, index) => {
        console.log(`处理第${index}个课时配置项:`, item);
        // 确保item是对象且包含必要字段
        if (item && typeof item === 'object') {
          console.log(`item.available:`, item.available);
          console.log(`item.dayOfWeek:`, item.dayOfWeek);
          console.log(`item.startTime:`, item.startTime);

          // 修复：使用更宽松的条件检查available
          const isAvailable = item.available === 1 || item.available === true || item.available === '1';
          if (isAvailable && item.dayOfWeek && item.startTime) {
            const dayIndex = item.dayOfWeek - 1;
            console.log(`计算dayIndex:`, dayIndex);
            const slotIndex = this.matchTimeSlot(item.startTime);
            console.log(`计算slotIndex:`, slotIndex);

            if (slotIndex !== -1 && dayIndex >= 0 && dayIndex < 7) {
              if (slotIndex >= 0 && slotIndex < data.length) {
                data[slotIndex][dayIndex] = true;
                console.log(`标记时段为可用: 时间段${slotIndex}, 星期${dayIndex}`);
              } else {
                console.log(`slotIndex超出范围:`, slotIndex);
              }
            } else {
              console.log(`索引无效: dayIndex=${dayIndex}, slotIndex=${slotIndex}`);
            }
          } else {
            console.log(`课时配置项不完整或不可用:`, item);
          }
        } else {
          console.log(`课时配置项不是对象:`, item);
        }
      });
    } else {
      console.log('课表数据为空或格式不正确:', serverData);
    }

    console.log('解析完成的课表数据:', data);
    this.setData({ scheduleData: data });
    console.log('scheduleData更新完成');
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
    console.log('检查时段可用性:', `slotIdx=${slotIdx}, dayIdx=${dayIdx}`);
    console.log('scheduleData长度:', this.data.scheduleData.length);
    if (!this.data.scheduleData || !Array.isArray(this.data.scheduleData)) {
      console.log('scheduleData未初始化或不是数组');
      return false;
    }
    if (slotIdx < 0 || slotIdx >= this.data.scheduleData.length) {
      console.log('slotIdx超出范围');
      return false;
    }
    const slotData = this.data.scheduleData[slotIdx];
    if (!slotData || !Array.isArray(slotData)) {
      console.log('时段数据未初始化或不是数组');
      return false;
    }
    if (dayIdx < 0 || dayIdx >= slotData.length) {
      console.log('dayIdx超出范围');
      return false;
    }
    const isAvailable = slotData[dayIdx];
    console.log('时段可用性结果:', isAvailable);
    return isAvailable;
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
    console.log('收到点击事件:', e);
    const dataset = e.currentTarget.dataset;
    console.log('事件数据集:', dataset);
    const slot = dataset.slot;
    const day = dataset.day;
    console.log('解析参数:', `slot=${slot}, day=${day}`);

    // 只能选择可用时段
    if (!this.isSlotAvailable(slot, day)) {
      console.log('时段不可预约，显示提示');
      wx.showToast({ title: '该时段不可预约', icon: 'none' });
      return;
    }

    console.log('时段可预约，切换选择状态');
    const data = this.data.bookingData;
    console.log('切换前的bookingData:', data);
    data[slot][day] = !data[slot][day];
    console.log('切换后的bookingData:', data);
    this.setData({ bookingData: data });
    console.log('bookingData更新完成');
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
      price: tutor.expectPrice || (tutor.price || 0),
      // 默认选中第一个科目，如果没有则留空
      subject: tutor.teachSubjectsList && tutor.teachSubjectsList.length > 0 ? tutor.teachSubjectsList[0] : ''
    };

    wx.navigateTo({
      url: `/pages/parent/order/confirm/confirm?data=${encodeURIComponent(JSON.stringify(orderData))}`
    });
  },

  // 带重试机制的请求
  async retryRequest(requestFn, maxRetries = 3, retryDelay = 1000) {
    let lastError;
    for (let i = 0; i < maxRetries; i++) {
      try {
        return await requestFn();
      } catch (error) {
        lastError = error;
        console.warn(`请求失败，${retryDelay}ms后重试 (${i + 1}/${maxRetries})`, error);
        if (i < maxRetries - 1) {
          await new Promise(resolve => setTimeout(resolve, retryDelay));
        }
      }
    }
    throw lastError;
  }
});