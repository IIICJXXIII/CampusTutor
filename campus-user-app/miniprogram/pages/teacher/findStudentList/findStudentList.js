const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    latitude: null,
    longitude: null,
    // 过滤项
    subjects: ['全部', '数学', '语文', '英语', '物理', '化学'],
    grades: ['全部', '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', '初中一年级', '初中二年级', '初中三年级', '高中一年级', '高中二年级', '高中三年级'],
    subjectIndex: 0,
    gradeIndex: 0,
    sortOptions: ['综合排序', '距离优先', '发布时间', '价格-高到低', '价格-低到高'],
    sortIndex: 0,

    list: [],
    page: 1,
    size: 20,
    loading: false,
    noMore: false,
    error: false
  },

  onLoad(options) {
    // 接受来自地图页的预设筛选
    if (options && options.subject) {
      const subj = decodeURIComponent(options.subject);
      const i = this.data.subjects.indexOf(subj);
      if (i >= 0) this.setData({ subjectIndex: i });
    }
    if (options && options.grade) {
      const gr = decodeURIComponent(options.grade);
      const j = this.data.grades.indexOf(gr);
      if (j >= 0) this.setData({ gradeIndex: j });
    }

    this.initLocationAndLoad();
  },

  // 获取位置并加载列表
  initLocationAndLoad() {
    const that = this;
    wx.getLocation({
      type: 'gcj02', success(res) {
        that.setData({ latitude: res.latitude, longitude: res.longitude });
        that.refreshList();
      }, fail() {
        // 仍然尝试加载（会降级到公开列表）
        that.refreshList();
      }
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ page: 1, noMore: false });
    this.refreshList().then(() => wx.stopPullDownRefresh());
  },

  // 上拉加载更多
  onReachBottom() {
    if (this.data.noMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.fetchList();
  },

  // 点击加载更多按钮
  loadMore() {
    if (this.data.noMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.fetchList();
  },

  // 刷新列表
  async refreshList() {
    this.setData({ list: [], page: 1, noMore: false });
    return this.fetchList();
  },

  // 获取过滤器字段
  getFilters() {
    const subject = this.data.subjects[this.data.subjectIndex];
    const grade = this.data.grades[this.data.gradeIndex];
    const sort = this.data.sortOptions[this.data.sortIndex];
    return { subject: subject === '全部' ? '' : subject, grade: grade === '全部' ? '' : grade, sort };
  },

  // 主列表获取逻辑：优先调用 nearby（LBS），若为空或失败则使用 list 降级
  async fetchList() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: false });

    const filters = this.getFilters();
    const page = this.data.page;
    try {
      // 若有定位信息，优先调用 nearby
      if (this.data.latitude && this.data.longitude) {
        const params = {
          longitude: this.data.longitude,
          latitude: this.data.latitude,
          radius: 20, // 20km
          page, size: this.data.size,
          subject: filters.subject,
          grade: filters.grade
        };
        let res = await request.get(api.demand.nearby, params);
        // 后端可能忽略过滤参数，或者返回空数组
        let records = res && res.records ? res.records : (Array.isArray(res) ? res : []);

        if (!records || records.length === 0) {
          // 降级调用公开 list
          const listRes = await request.get(api.demand.list, { page, size: this.data.size, subject: filters.subject, grade: filters.grade });
          records = (listRes && listRes.records) ? listRes.records : [];
        }

        // 计算距离并格式化
        const mapped = records.map(item => ({ ...item, distance: (item.latitude && item.longitude && this.data.latitude && this.data.longitude) ? this.getDistance(this.data.latitude, this.data.longitude, item.latitude, item.longitude).toFixed(1) : '' }));

        // 本地排序
        const sorted = this.applySort(mapped, filters.sort);

        const newList = page === 1 ? sorted : (this.data.list.concat(sorted));
        this.setData({ list: newList, noMore: (sorted.length < this.data.size), loading: false });
        return;
      }

      // 无定位 -> 直接调用 list
      const listRes = await request.get(api.demand.list, { page, size: this.data.size, subject: filters.subject, grade: filters.grade });
      const records = (listRes && listRes.records) ? listRes.records : [];
      const mapped = records.map(item => ({ ...item, distance: '' }));
      const sorted = this.applySort(mapped, filters.sort);
      const newList = page === 1 ? sorted : (this.data.list.concat(sorted));
      this.setData({ list: newList, noMore: (sorted.length < this.data.size), loading: false });

    } catch (err) {
      console.error('获取学生列表失败', err);
      this.setData({ error: true, loading: false });
    }
  },

  // 排序实现（简易）
  applySort(list, sortKey) {
    const arr = list.slice();
    switch (sortKey) {
      case '距离优先':
        return arr.sort((a, b) => (parseFloat(a.distance || 99999) - parseFloat(b.distance || 99999)));
      case '发布时间':
        return arr.sort((a, b) => new Date(b.createdAt || b.createTime || 0) - new Date(a.createdAt || a.createTime || 0));
      case '价格-高到低':
        return arr.sort((a, b) => (b.expectPrice || 0) - (a.expectPrice || 0));
      case '价格-低到高':
        return arr.sort((a, b) => (a.expectPrice || 0) - (b.expectPrice || 0));
      default:
        return arr; // 综合排序保持后端顺序
    }
  },

  // 选择科目
  handleSubjectChange(e) {
    this.setData({ subjectIndex: e.detail.value, page: 1, noMore: false }, () => this.refreshList());
  },

  // 选择年级
  handleGradeChange(e) {
    this.setData({ gradeIndex: e.detail.value, page: 1, noMore: false }, () => this.refreshList());
  },

  // 选择排序
  handleSortChange(e) {
    this.setData({ sortIndex: e.detail.value, page: 1, noMore: false }, () => this.refreshList());
  },

  // 查看详情
  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: `/pages/teacher/demandDetail/demandDetail?id=${id}` });
  },

  // 联系家长（跳转到聊天页面）
  async contactParent(e) {
    const item = e.currentTarget.dataset.item;
    if (!item) return;

    const publisherId = item.publisherId || item.parentId;
    if (!publisherId) {
      wx.showToast({ title: '无法获取家长信息', icon: 'none' });
      return;
    }

    // 获取家长信息用于聊天
    try {
      const userInfo = await request.get(api.chat.userInfo(publisherId));
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent(userInfo.nickname || '家长')}&avatar=${encodeURIComponent(userInfo.avatar || '')}`
      });
    } catch (err) {
      // 即使获取失败也跳转，使用默认信息
      wx.navigateTo({
        url: `/pages/common/chatDetail/chatDetail?userId=${publisherId}&nickname=${encodeURIComponent('家长')}&avatar=`
      });
    }
  },

  // 复制需求ID
  copyId(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.setClipboardData({ data: String(id), success() { wx.showToast({ title: '已复制ID', icon: 'none' }); } });
  },

  // 计算距离
  getDistance(lat1, lng1, lat2, lng2) {
    const radLat1 = lat1 * Math.PI / 180.0;
    const radLat2 = lat2 * Math.PI / 180.0;
    const a = radLat1 - radLat2;
    const b = (lng1 * Math.PI / 180.0) - (lng2 * Math.PI / 180.0);
    let s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
    s = s * 6378.137;
    return s;
  }
});