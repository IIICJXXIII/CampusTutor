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
    subjects: ['全部', '艺术素养', '体育健康', '科创STEAM', '钢琴/乐器陪练', '美术/书法', '中考体育专项', '少儿编程'],
    grades: ['全部', '4-6岁', '7-9岁', '10-12岁', '13-15岁', '16-18岁'],
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
    // 注意：需要对URL参数进行解码
    if (options.subject) {
      const subject = decodeURIComponent(options.subject);
      const idx = this.data.subjects.indexOf(subject);
      this.setData({
        'query.subject': subject,
        activeSubject: idx > -1 ? idx : 0
      });
    }
    if (options.grade) {
      const grade = decodeURIComponent(options.grade);
      const gIdx = this.data.grades.indexOf(grade);
      this.setData({
        'query.grade': grade,
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
      // 处理排序参数
      const q = { ...this.data.query };
      const sortValue = this.data.sortOptions[this.data.activeSort].value;

      if (sortValue === 'price_asc') {
        q.sortBy = 'price';
        q.sortOrder = 'asc';
      } else if (sortValue === 'score') {
        // 智能推荐使用score排序，默认降序
        q.sortBy = 'score';
        q.sortOrder = 'desc';
      } else if (sortValue) {
        q.sortBy = sortValue;
        q.sortOrder = 'desc';
      } else {
        // 默认排序
        delete q.sortBy;
        delete q.sortOrder;
      }

      // 调用接口: POST /api/match/tutors，添加重试机制
      const res = await this.retryRequest(() =>
        request.post(api.match.search, q),
        3, // 最多重试3次
        1000 // 重试间隔1秒
      );
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
        distText: item.distance ? (item.distance < 1 ? Math.round(item.distance * 1000) + 'm' : item.distance.toFixed(1) + 'km') : '',
        // 计算匹配等级（用于UI展示）
        matchLevel: item.matchScore ? (item.matchScore >= 90 ? 'excellent' : item.matchScore >= 75 ? 'good' : item.matchScore >= 60 ? 'fair' : 'poor') : 'unknown'
      }));

      // 如果结果为空，尝试智能降级策略
      if (list.length === 0) {
        // 策略1: 清除位置限制
        if (this.data.query.longitude && this.data.query.latitude) {
          this.setData({ fallbackMessage: '未检索到附近老师，显示系统推荐（不含距离）' });
          // 确保降级查询时也正确处理排序参数
          const q2 = { ...this.data.query, longitude: null, latitude: null, page: 1 };
          delete q2.longitude;
          delete q2.latitude;
          try {
            const res2 = await request.post(api.match.search, q2);
            const rec2 = res2.records || [];
            list = rec2.map(item => ({
              ...item,
              teachSubjects: item.teachSubjects || [],
              teachGrades: item.teachGrades || [],
              matchScore: item.matchScore || null,
              matchTags: item.matchTags || [],
              distText: '',
              matchLevel: item.matchScore ? (item.matchScore >= 90 ? 'excellent' : item.matchScore >= 75 ? 'good' : item.matchScore >= 60 ? 'fair' : 'poor') : 'unknown'
            }));
          } catch (err) {
            console.error('降级获取教师推荐失败', err);
          }
        }
        // 策略2: 清除科目限制
        else if (this.data.query.subject) {
          this.setData({ fallbackMessage: '未检索到匹配的老师，尝试清除科目限制' });
          const q3 = { ...this.data.query, subject: '', page: 1 };
          try {
            const res3 = await request.post(api.match.search, q3);
            const rec3 = res3.records || [];
            list = rec3.map(item => ({
              ...item,
              teachSubjects: item.teachSubjects || [],
              teachGrades: item.teachGrades || [],
              matchScore: item.matchScore || null,
              matchTags: item.matchTags || [],
              distText: item.distance ? (item.distance < 1 ? Math.round(item.distance * 1000) + 'm' : item.distance.toFixed(1) + 'km') : '',
              matchLevel: item.matchScore ? (item.matchScore >= 90 ? 'excellent' : item.matchScore >= 75 ? 'good' : item.matchScore >= 60 ? 'fair' : 'poor') : 'unknown'
            }));
          } catch (err) {
            console.error('清除科目限制后获取教师推荐失败', err);
          }
        }
        // 策略3: 清除年级限制
        else if (this.data.query.grade) {
          this.setData({ fallbackMessage: '未检索到匹配的老师，尝试清除年级限制' });
          const q4 = { ...this.data.query, grade: '', page: 1 };
          try {
            const res4 = await request.post(api.match.search, q4);
            const rec4 = res4.records || [];
            list = rec4.map(item => ({
              ...item,
              teachSubjects: item.teachSubjects || [],
              teachGrades: item.teachGrades || [],
              matchScore: item.matchScore || null,
              matchTags: item.matchTags || [],
              distText: item.distance ? (item.distance < 1 ? Math.round(item.distance * 1000) + 'm' : item.distance.toFixed(1) + 'km') : '',
              matchLevel: item.matchScore ? (item.matchScore >= 90 ? 'excellent' : item.matchScore >= 75 ? 'good' : item.matchScore >= 60 ? 'fair' : 'poor') : 'unknown'
            }));
          } catch (err) {
            console.error('清除年级限制后获取教师推荐失败', err);
          }
        }
        // 最终策略: 显示所有教师
        else {
          this.setData({ fallbackMessage: '未检索到匹配的老师，显示所有可用教师' });
          const q5 = { page: 1, size: 10 };
          try {
            const res5 = await request.post(api.match.search, q5);
            const rec5 = res5.records || [];
            list = rec5.map(item => ({
              ...item,
              teachSubjects: item.teachSubjects || [],
              teachGrades: item.teachGrades || [],
              matchScore: item.matchScore || null,
              matchTags: item.matchTags || [],
              distText: item.distance ? (item.distance < 1 ? Math.round(item.distance * 1000) + 'm' : item.distance.toFixed(1) + 'km') : '',
              matchLevel: item.matchScore ? (item.matchScore >= 90 ? 'excellent' : item.matchScore >= 75 ? 'good' : item.matchScore >= 60 ? 'fair' : 'poor') : 'unknown'
            }));
          } catch (err) {
            console.error('获取所有教师推荐失败', err);
          }
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
      wx.showToast({ title: '获取教师列表失败，请重试', icon: 'none' });
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
    const queryUpdate = { activeSort: idx };

    // 根据选择的排序值设置正确的参数
    if (val === 'price_asc') {
      queryUpdate['query.sortBy'] = 'price';
      queryUpdate['query.sortOrder'] = 'asc';
    } else if (val === 'score') {
      queryUpdate['query.sortBy'] = 'score';
      queryUpdate['query.sortOrder'] = 'desc';
    } else if (val) {
      queryUpdate['query.sortBy'] = val;
      queryUpdate['query.sortOrder'] = 'desc';
    } else {
      queryUpdate['query.sortBy'] = '';
      queryUpdate['query.sortOrder'] = 'desc';
    }

    this.setData(queryUpdate, () => this.refreshList());
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