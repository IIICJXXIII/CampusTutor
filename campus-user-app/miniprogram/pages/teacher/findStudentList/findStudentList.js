// findStudentList.js
// 教师端学生需求列表页面
const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    demands: [],
    subjects: [
      { id: 'all', name: '全部科目' },
      { id: '艺术素养', name: '艺术素养' },
      { id: '体育健康', name: '体育健康' },
      { id: '科创STEAM', name: '科创STEAM' },
      { id: '钢琴/乐器陪练', name: '钢琴/乐器陪练' },
      { id: '美术/书法', name: '美术/书法' },
      { id: '中考体育专项', name: '中考体育专项' },
      { id: '羽毛球/网球陪练', name: '羽毛球/网球' },
      { id: '少儿编程(Scratch/Python)', name: '少儿编程' },
      { id: '机器人/3D打印', name: '机器人/3D打印' }
    ],
    selectedSubject: 'all',
    loading: false,
    error: false,
    errorMessage: '',
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    userLocation: null,
    sortOptions: [
      { label: '智能推荐', value: 'score' },
      { label: '最新发布', value: 'createTime' },
      { label: '距离最近', value: 'distance' }
    ],
    activeSort: 0, // 默认智能推荐
    sortBy: 'score', // 默认按匹配分数排序
    sortOrder: 'desc' // 默认降序
  },

  onLoad: function (options) {
    wx.setNavigationBarTitle({
      title: '找学生'
    });
    this.checkLoginStatus();
    this.loadUserLocation();
  },

  onShow: function () {
    if (this.data.userLocation) {
      this.loadDemands(true);
    }
  },

  // 检查登录状态
  checkLoginStatus: function () {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    console.log('登录状态检查:', {
      token: token ? '存在' : '不存在',
      userInfo: userInfo
    });
    if (!token) {
      console.warn('未登录，可能无法获取数据');
    }
  },

  // 加载用户位置
  loadUserLocation: function () {
    wx.getLocation({
      type: 'wgs84',
      success: (res) => {
        console.log('获取位置成功:', res);
        this.setData({
          userLocation: {
            latitude: res.latitude,
            longitude: res.longitude
          }
        });
        this.loadDemands(true);
      },
      fail: (err) => {
        console.error('获取位置失败:', err);
        // 使用默认位置（例如：北京大学）
        this.setData({
          userLocation: {
            latitude: 39.999366,
            longitude: 116.327976
          }
        });
        console.log('使用默认位置');
        this.loadDemands(true);
      }
    });
  },

  // 加载需求列表
  loadDemands: function (refresh = false) {
    if (this.data.loading) {
      console.log('正在加载中，跳过重复请求');
      return;
    }

    if (refresh) {
      this.setData({
        loading: true,
        error: false,
        errorMessage: '',
        currentPage: 1,
        hasMore: true,
        demands: []
      });
    } else if (!this.data.hasMore) {
      console.log('没有更多数据');
      return;
    } else {
      this.setData({
        loading: true,
        error: false
      });
    }

    const page = refresh ? 1 : this.data.currentPage;
    const subject = this.data.selectedSubject === 'all' ? '' : this.data.selectedSubject;

    console.log('请求需求列表:', {
      page: page,
      size: this.data.pageSize,
      subject: subject,
      userLocation: this.data.userLocation,
      sortBy: this.data.sortBy,
      sortOrder: this.data.sortOrder
    });

    // 使用新的API获取带有匹配度的需求列表
    const token = wx.getStorageSync('token') || '';
    wx.request({
      url: api.demand.listWithMatch,
      method: 'GET',
      data: {
        subject: subject,
        longitude: this.data.userLocation ? this.data.userLocation.longitude : null,
        latitude: this.data.userLocation ? this.data.userLocation.latitude : null,
        page: page,
        size: this.data.pageSize,
        sortBy: this.data.sortBy,
        sortOrder: this.data.sortOrder
      },
      header: {
        'Content-Type': 'application/json',
        'token': token,
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        console.log('API请求成功，响应数据:', res);
        if (res.statusCode === 200 && res.data.code === 200) {
          const pageData = res.data.data || {};
          console.log('分页数据:', pageData);
          const newDemands = pageData.records || [];
          console.log('需求列表:', newDemands);

          // 处理匹配度数据
          const demandsWithMatch = newDemands.map(demand => ({
            ...demand,
            // 确保匹配度数据存在，并保留一位小数
            matchScore: demand.matchScore ? Number(demand.matchScore.toFixed(1)) : null,
            matchTags: demand.matchTags || [],
            matchLevel: demand.matchLevel || 'unknown',
            // 距离格式化
            distanceText: demand.distance ? (demand.distance < 1 ? Math.round(demand.distance * 1000) + 'm' : demand.distance.toFixed(1) + 'km') : '未知'
          }));

          this.setData({
            demands: refresh ? demandsWithMatch : [...this.data.demands, ...demandsWithMatch],
            currentPage: page + 1,
            hasMore: (pageData.current || 0) < (pageData.pages || 1),
            loading: false,
            error: false
          });

          console.log('加载完成，共', this.data.demands.length, '条数据');
        } else {
          console.error('API请求失败，响应数据:', res);
          this.setData({
            error: true,
            errorMessage: res.data.msg || '加载失败，请重试',
            loading: false
          });
        }
      },
      fail: (err) => {
        console.error('网络请求失败:', err);
        this.setData({
          error: true,
          errorMessage: '网络请求失败，请检查网络连接',
          loading: false
        });
      }
    });
  },



  // 切换科目
  onSubjectChange: function (e) {
    const subject = e.currentTarget.dataset.subject;
    console.log('切换科目:', subject);
    this.setData({
      selectedSubject: subject
    });
    this.loadDemands(true);
  },

  // 加载更多
  loadMore: function () {
    if (!this.data.loading && this.data.hasMore) {
      console.log('加载更多数据');
      this.loadDemands(false);
    }
  },

  // 刷新页面
  refreshPage: function () {
    console.log('刷新页面');
    this.loadUserLocation();
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

  // 跳转到需求详情页
  goToDemandDetail: function (e) {
    const demandId = e.currentTarget.dataset.demandId;
    console.log('跳转到需求详情页:', demandId);
    wx.navigateTo({
      url: `/pages/teacher/demandDetail/demandDetail?id=${demandId}`
    });
  },

  // 切换排序
  onSortChange: function (e) {
    const idx = e.detail.value;
    const sortOption = this.data.sortOptions[idx];
    console.log('切换排序:', sortOption);

    let sortBy = sortOption.value;
    let sortOrder = 'desc';

    if (sortBy === 'distance') {
      sortOrder = 'asc'; // 距离越近越靠前
    }

    this.setData({
      activeSort: idx,
      sortBy: sortBy,
      sortOrder: sortOrder
    }, () => {
      this.loadDemands(true);
    });
  },

  // 返回上一页
  navigateBack: function () {
    console.log('返回上一页');
    wx.navigateBack({
      delta: 1
    });
  }
});