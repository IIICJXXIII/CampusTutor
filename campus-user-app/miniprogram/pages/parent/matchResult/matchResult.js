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
    demandId: null,
    fallbackMessage: '',

    // 字典配置
    subjects: ['全部', '数学', '英语', '语文', '物理', '化学', '生物', '全科'],
    grades: ['全部', '小学', '初一', '初二', '初三', '高一', '高二', '高三'],
    sortOptions: [
      { label: '智能推荐', value: 'score' },
      { label: '综合排序', value: '' },
      { label: '好评优先', value: 'rating' },
      { label: '距离最近', value: 'distance' },
      { label: '价格最低', value: 'price_asc' } // 特殊处理
    ],
    
    // UI状态
    activeSubject: 0,
    activeGrade: 0,
    activeSort: 1, // 默认不自动采用“智能推荐”，用户可自主选择

    
    // 学历映射
    eduMap: { 1: '本科在读', 2: '本科毕业', 3: '硕士在读', 4: '硕士毕业', 5: '博士' }
  },

  onLoad(options) {
    // 接收预设关键词: subject, grade, longitude, latitude, demandId
    if (options.subject) {
      const idx = this.data.subjects.indexOf(options.subject);
      this.setData({ 
        'query.subject': options.subject,
        activeSubject: idx > -1 ? idx : 0 
      });
    }
    if (options.grade) {
      const gIdx = this.data.grades.indexOf(options.grade);
      this.setData({
        'query.grade': options.grade,
        activeGrade: gIdx > -1 ? gIdx : 0
      });
    }

    // 如果传入位置参数则直接使用并搜索
    if (options.longitude && options.latitude) {
      this.setData({
        'query.longitude': parseFloat(options.longitude),
        'query.latitude': parseFloat(options.latitude),
        demandId: options.demandId || null
      });
      this.refreshList();
    } else {
      // 初始化定位并搜索
      this.initLocation();
    }
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
      let list = records.map(item => ({
        ...item,
        // 确保数组存在 (防止后端返回null)
        teachSubjects: item.teachSubjects || [],
        teachGrades: item.teachGrades || [],
        // 匹配分数与标签
        matchScore: item.matchScore || null,
        matchTags: item.matchTags || [],
        // 距离格式化
        distText: item.distance ? (item.distance < 1 ? Math.round(item.distance * 1000) + 'm' : item.distance.toFixed(1) + 'km') : ''
      }));

      // 如果结果为空且我们是基于位置搜索的，尝试降级：清除位置并再次获取系统推荐
      if (list.length === 0 && this.data.query.longitude && this.data.query.latitude) {
        this.setData({ fallbackMessage: '未检索到附近老师，显示系统推荐（不含距离）' });
        const q2 = { ...this.data.query, longitude: null, latitude: null, page: 1 };
        try {
          const res2 = await request.post(api.match.search, q2);
          const rec2 = res2.records || [];
          list = rec2.map(item => ({
            ...item,
            teachSubjects: item.teachSubjects || [],
            teachGrades: item.teachGrades || [],
            matchScore: item.matchScore || null,
            matchTags: item.matchTags || [],
            distText: ''
          }));
        } catch (err) {
          console.error('降级获取教师推荐失败', err);
        }
      }

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
    this.setData({ 'query.page': 1, hasMore: true, fallbackMessage: '' });
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

  // 用户主动选择显示系统推荐（清除位置后检索）
  showSystemRecommend() {
    this.setData({ 'query.longitude': null, 'query.latitude': null, 'query.page': 1, fallbackMessage: '' }, () => {
      this.refreshList();
    });
  },

  // 清除所有筛选并查看全部老师（不带位置）
  showAllTeachers() {
    this.setData({ 'query.subject': '', 'query.grade': '', 'query.longitude': null, 'query.latitude': null, 'query.page': 1, fallbackMessage: '正在显示全部老师（可能不包含距离信息）' }, () => {
      // 重置选择器显示
      this.setData({ activeSubject: 0, activeGrade: 0 });
      this.refreshList();
    });
  },

  // 运行可见性诊断：比较当前筛选与全部老师的命中情况
  async runVisibilityDiagnostic() {
    this.setData({ fallbackMessage: '正在诊断可见性...' });
    try {
      // 1) 当前筛选（含科目/年级/位置）
      const curQ = { ...this.data.query, page: 1, size: 1 };
      const curRes = await request.post(api.match.search, curQ);
      const curCount = curRes && curRes.total ? curRes.total : 0;

      // 2) 全部老师（不带任何筛选）
      const allQ = { page: 1, size: 1 };
      const allRes = await request.post(api.match.search, allQ);
      const allCount = allRes && allRes.total ? allRes.total : 0;

      let msg = '';
      if (allCount === 0) {
        msg = '当前系统中没有已认证的教师（系统内无可见教师）。建议检查教师是否已完成认证或联系管理员。';
      } else if (curCount === 0 && allCount > 0) {
        msg = `存在 ${allCount} 名已认证教师，但当前筛选/定位未命中任何教师。建议：取消科目/年级或使用“查看全部老师”查看。`;
      } else {
        msg = `当前筛选命中 ${curCount} 人，系统中共有 ${allCount} 名已认证教师。`;
      }

      this.setData({ fallbackMessage: msg });
    } catch (err) {
      console.error('诊断失败', err);
      this.setData({ fallbackMessage: '诊断失败，请检查网络或稍后重试' });
    }
  },

  // 跳转详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/parent/teacherDetail/teacherDetail?id=${id}` });
  }
});