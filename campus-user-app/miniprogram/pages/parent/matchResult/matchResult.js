import request from '../../../utils/request';
import api from '../../../config/apiConfig';

Page({
  data: {
    // 搜索参数
    searchParams: {
      subject: '',
      grade: '',
      latitude: null,
      longitude: null,
      minPrice: null,
      maxPrice: null
    },
    
    tutorList: [],
    currentSort: 'score', // score | distance | price
    sortOrder: 'desc',    // desc | asc
    page: 1,
    pageSize: 10,
    hasMore: true,
    
    isCalculating: true,
    matchCount: 0
  },

  onLoad() {
    // 1. 获取 Step 3 存储的最终需求数据
    const flowData = wx.getStorageSync('current_demand_data') || {};
    
    // 2. 初始化搜索参数
    this.setData({
      'searchParams.subject': flowData.subject || '',
      'searchParams.grade': flowData.grade || '',
      'searchParams.latitude': flowData.latitude,
      'searchParams.longitude': flowData.longitude,
      // 这里的 expectPrice 是一个具体的数值，为了搜索范围，可以前后浮动或暂不传价格限制
      // 'searchParams.maxPrice': flowData.expectPrice ? flowData.expectPrice * 1.5 : null
    });

    // 3. 开始动画并请求数据
    this.startMatchingAnimation();
  },

  // 模拟 AI 匹配过程动画
  startMatchingAnimation() {
    let count = 0;
    const timer = setInterval(() => {
      count += Math.floor(Math.random() * 50);
      if (count > 2000) count = 2000;
      this.setData({ matchCount: count });
    }, 100);

    // 1.5秒后请求真实数据
    setTimeout(() => {
      clearInterval(timer);
      this.fetchTutors(true); // true 表示重置列表
    }, 1500);
  },

  // 获取教员列表 (对接 /api/match/tutors)
  async fetchTutors(reset = false) {
    if (reset) {
      this.setData({ page: 1, hasMore: true, tutorList: [] });
    }
    if (!this.data.hasMore) return;

    try {
      // 构造请求体 TutorSearchRequest
      const postData = {
        subject: this.data.searchParams.subject,
        grade: this.data.searchParams.grade,
        latitude: this.data.searchParams.latitude,
        longitude: this.data.searchParams.longitude,
        // 半径默认 10km，可根据需求调整
        radius: 10.0, 
        sortBy: this.data.currentSort === 'score' ? 'rating' : this.data.currentSort, // 后端通常用 rating 或 distance
        sortOrder: this.data.sortOrder,
        page: this.data.page,
        size: this.data.pageSize
      };

      // 调用接口
      const res = await request.post(api.match.search, postData);
      
      const newRecords = res.records || [];
      
      // 处理数据，适配 UI
      const processedList = newRecords.map(item => {
        return {
          ...item,
          // 确保是数组，防止后端返回 JSON 字符串
          teachSubjects: Array.isArray(item.teachSubjects) ? item.teachSubjects : JSON.parse(item.teachSubjects || '[]'),
          teachGrades: Array.isArray(item.teachGrades) ? item.teachGrades : JSON.parse(item.teachGrades || '[]'),
          // 距离处理 (如果后端返回单位是度或米，需统一)
          distance: item.distance // 假设后端返回单位为 km
        };
      });

      this.setData({
        tutorList: reset ? processedList : [...this.data.tutorList, ...processedList],
        page: this.data.page + 1,
        hasMore: this.data.tutorList.length + processedList.length < res.total,
        isCalculating: false
      });

    } catch (err) {
      console.error('获取匹配列表失败', err);
      this.setData({ isCalculating: false });
      // 如果是演示环境且无数据，可在此处 Mock 数据兜底
      if (this.data.tutorList.length === 0) {
        this.mockData();
      }
    }
  },

  // 兜底 Mock 数据 (用于演示)
  mockData() {
    const mocks = [
      { id: 101, realName: '李华', universityName: '北京大学', major: '数学系', expectPrice: 120, certStatus: 2, teachSubjects: ['数学', '奥数'], teachStyle: '逻辑清晰', rating: 4.9, avatarUrl: '', distance: 1.2 },
      { id: 102, realName: '王老师', universityName: '师范大学', major: '英语教育', expectPrice: 100, certStatus: 2, teachSubjects: ['英语', '口语'], teachStyle: '互动教学', rating: 4.8, avatarUrl: '', distance: 2.5 },
      { id: 103, realName: '张伟', universityName: '理工大学', major: '物理系', expectPrice: 150, certStatus: 1, teachSubjects: ['物理'], teachStyle: '严厉负责', rating: 4.7, avatarUrl: '', distance: 3.8 }
    ];
    this.setData({ tutorList: mocks, hasMore: false });
  },

  // 切换排序
  changeSort(e) {
    const type = e.currentTarget.dataset.type;
    let order = 'desc';
    
    // 如果点击的是价格，支持切换升降序
    if (type === 'price' && this.data.currentSort === 'price') {
      order = this.data.sortOrder === 'asc' ? 'desc' : 'asc';
    } else if (type === 'distance') {
      order = 'asc'; // 距离默认升序
    }

    this.setData({ 
      currentSort: type,
      sortOrder: order
    });
    
    wx.showLoading({ title: '重新排序...' });
    this.fetchTutors(true).then(() => wx.hideLoading());
  },

  // 跳转详情页
  goToDetail(e) {
    const id = e.detail.id;
    wx.navigateTo({
      url: `/pages/parent/teacherDetail/teacherDetail?id=${id}`
    });
  },

  goBack() {
    wx.navigateBack();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.fetchTutors(true).then(() => wx.stopPullDownRefresh());
  },

  // 触底加载
  onReachBottom() {
    this.fetchTutors(false);
  }
});