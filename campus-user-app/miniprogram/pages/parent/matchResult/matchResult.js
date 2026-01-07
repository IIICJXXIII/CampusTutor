const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    // 搜索参数 (对应 TutorSearchRequest)
    query: {
      subject: '',
      grade: '',
      minPrice: null,
      maxPrice: null,
      teachMode: null,
      gender: null,
      sortBy: '', // distance, rating, price
      sortOrder: 'desc',
      longitude: null,
      latitude: null,
      page: 1,
      size: 10
    },

    // 页面数据
    tutorList: [],
    loading: false,
    hasMore: true,
    showFilter: false, // 筛选弹窗控制

    // 字典配置
    subjects: ['全部', '数学', '英语', '语文', '物理', '化学', '生物', '全科'],
    grades: ['全部', '小学', '初一', '初二', '初三', '高一', '高二', '高三'],
    sortOptions: [
      { label: '综合排序', value: '' },
      { label: '好评优先', value: 'rating' },
      { label: '距离最近', value: 'distance' },
      { label: '价格最低', value: 'price_asc' } // 特殊处理
    ],
    
    // UI状态
    activeSubject: 0,
    activeGrade: 0,
    activeSort: 0,
    
    // 学历映射
    eduMap: { 1: '本科在读', 2: '本科毕业', 3: '硕士在读', 4: '硕士毕业', 5: '博士' }
  },

  onLoad(options) {
    // 接收从首页或发布页传来的预设关键词
    if (options.subject) {
      const idx = this.data.subjects.indexOf(options.subject);
      this.setData({ 
        'query.subject': options.subject,
        activeSubject: idx > -1 ? idx : 0 
      });
    }
    
    // 初始化定位并搜索
    this.initLocation();
  },

  // 1. 获取定位 (为了计算距离)
  initLocation() {
    const that = this;
    wx.getLocation({
      type: 'gcj02',
      success(res) {
        that.setData({
          'query.longitude': res.longitude,
          'query.latitude': res.latitude
        });
        that.refreshList();
      },
      fail() {
        // 定位失败也允许搜索，只是没有距离
        that.refreshList();
      }
    });
  },

  // 2. 核心搜索方法
  async fetchTutors(append = false) {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      // 处理特殊排序值
      const q = { ...this.data.query };
      if (this.data.sortOptions[this.data.activeSort].value === 'price_asc') {
        q.sortBy = 'price';
        q.sortOrder = 'asc';
      }

      // 调用接口: POST /api/match/tutors
      const res = await request.post(api.match.search, q);
      const records = res.records || [];

      // 数据处理 (格式化距离、标签等)
      const list = records.map(item => ({
        ...item,
        // 确保数组存在 (防止后端返回null)
        teachSubjects: item.teachSubjects || [],
        teachGrades: item.teachGrades || [],
        // 距离格式化
        distText: item.distance ? (item.distance < 1 ? item.distance * 1000 + 'm' : item.distance.toFixed(1) + 'km') : ''
      }));

      this.setData({
        tutorList: append ? this.data.tutorList.concat(list) : list,
        hasMore: this.data.query.page < res.pages,
        loading: false
      });
      
      if (!append) wx.stopPullDownRefresh();

    } catch (err) {
      console.error(err);
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  // 刷新列表
  refreshList() {
    this.setData({ 'query.page': 1, hasMore: true });
    this.fetchTutors(false);
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.refreshList();
  },

  // 触底加载
  onReachBottom() {
    if (this.data.hasMore) {
      this.setData({ 'query.page': this.data.query.page + 1 });
      this.fetchTutors(true);
    }
  },

  // --- 筛选事件 ---

  // 切换科目
  onSubjectChange(e) {
    const idx = e.detail.value;
    const val = this.data.subjects[idx];
    this.setData({ 
      activeSubject: idx,
      'query.subject': val === '全部' ? '' : val 
    }, () => this.refreshList());
  },

  // 切换年级
  onGradeChange(e) {
    const idx = e.detail.value;
    const val = this.data.grades[idx];
    this.setData({ 
      activeGrade: idx,
      'query.grade': val === '全部' ? '' : val 
    }, () => this.refreshList());
  },

  // 切换排序
  onSortChange(e) {
    const idx = e.detail.value;
    const val = this.data.sortOptions[idx].value;
    this.setData({ 
      activeSort: idx,
      'query.sortBy': val 
    }, () => this.refreshList());
  },

  // 搜索框输入
  onSearchInput(e) {
    // 简单实现：将输入作为科目搜索，也可以扩展为后端支持 keyword
    this.setData({ 'query.subject': e.detail.value });
  },
  
  onSearchConfirm() {
    this.refreshList();
  },

  // 跳转详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/parent/teacherDetail/teacherDetail?id=${id}` });
  }
});