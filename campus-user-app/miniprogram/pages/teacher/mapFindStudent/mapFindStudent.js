const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    latitude: 39.9088, // 默认北京坐标，防止未授权时地图白屏
    longitude: 116.3975,
    markers: [],
    demandList: [],
    currentDemand: null, // 当前选中的需求
    isLoading: false
  },

  onLoad() {
    this.initLocation();
  },

  onShow() {
    // 每次显示页面时尝试刷新数据
    if (this.data.latitude && this.data.longitude) {
      this.fetchNearbyDemands();
    }
  },

  // 1. 初始化位置
  initLocation() {
    const that = this;

    // 先检查用户是否已授权
    wx.getSetting({
      success(res) {
        if (res.authSetting['scope.userLocation'] === false) {
          // 用户之前拒绝过，引导去设置页开启
          wx.showModal({
            title: '需要位置权限',
            content: '请在设置中开启位置权限，以便查找附近的家教需求',
            confirmText: '去设置',
            success(modalRes) {
              if (modalRes.confirm) {
                wx.openSetting({
                  success(settingRes) {
                    if (settingRes.authSetting['scope.userLocation']) {
                      that.getLocationAndFetch();
                    }
                  }
                });
              }
            }
          });
        } else {
          // 未拒绝过，直接请求
          that.getLocationAndFetch();
        }
      }
    });
  },

  // 获取位置并加载数据
  getLocationAndFetch() {
    const that = this;
    wx.getLocation({
      type: 'gcj02',
      success(res) {
        that.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
        that.fetchNearbyDemands();
      },
      fail(err) {
        console.error('定位失败', err);
        wx.showToast({ title: '定位失败，使用默认位置', icon: 'none' });
        // 使用默认坐标
        that.fetchNearbyDemands();
      }
    });
  },

  // 2. 获取附近需求 (核心修复)
  async fetchNearbyDemands() {
    if (this.data.isLoading) return;
    this.setData({ isLoading: true });

    try {
      const res = await request.get(api.demand.nearby, {
        longitude: this.data.longitude,
        latitude: this.data.latitude,
        radius: 10 // 搜索半径 10km
      });

      // 【修复点】：后端如果因Redis挂了返回空，或者真没数据，res可能是空数组
      if (!res || res.length === 0) {
        this.setData({
          demandList: [],
          markers: [],
          currentDemand: null,
          isLoading: false
        });
        console.log('附近暂无需求数据');
        return;
      }

      // 处理数据，计算距离
      const list = res.map(item => {
        return {
          ...item,
          // 简单计算距离展示 (保留1位小数)
          distance: this.getDistance(
            this.data.latitude,
            this.data.longitude,
            item.latitude,
            item.longitude
          ).toFixed(1)
        };
      });

      // 生成地图标记
      const markers = list.map((item, index) => ({
        id: item.id, // 使用需求ID作为Marker ID
        latitude: item.latitude,
        longitude: item.longitude,
        width: 30,
        height: 30,
        // 如果没有自定义图标，不设置 iconPath，微信会用默认红色大头针
        // iconPath: '/static/icons/location.png', 
        callout: {
          content: `¥${item.expectPrice}\n${item.subject} ${item.grade}`,
          padding: 8,
          borderRadius: 4,
          display: 'ALWAYS',
          textAlign: 'center'
        }
      }));

      this.setData({
        demandList: list,
        markers: markers,
        // 默认选中第一个，防止 currentDemand 为空
        currentDemand: list[0],
        isLoading: false
      });

    } catch (err) {
      console.error('获取附近需求失败', err);
      // 出错时重置为空，防止页面崩坏
      this.setData({
        demandList: [],
        currentDemand: null,
        markers: [],
        isLoading: false
      });
    }
  },

  // 点击地图标记
  onMarkerTap(e) {
    const demandId = e.markerId;
    const target = this.data.demandList.find(d => d.id === demandId);
    if (target) {
      this.setData({ currentDemand: target });
    }
  },

  // 点击“立即接单”
  handleAccept() {
    if (!this.data.currentDemand) return;
    wx.showToast({ title: '接单功能开发中', icon: 'none' });
    // 后续跳转逻辑：
    // wx.navigateTo({ url: `/pages/demand/detail/detail?id=${this.data.currentDemand.id}` });
  },

  // 点击“查看详情”
  handleViewDetail() {
    if (!this.data.currentDemand) return;
    // 跳转到需求详情页面
    wx.navigateTo({
      url: `/pages/teacher/demandDetail/demandDetail?id=${this.data.currentDemand.id}`
    });
  },

  // 辅助：计算两点距离 (单位：km)
  getDistance(lat1, lng1, lat2, lng2) {
    const radLat1 = lat1 * Math.PI / 180.0;
    const radLat2 = lat2 * Math.PI / 180.0;
    const a = radLat1 - radLat2;
    const b = (lng1 * Math.PI / 180.0) - (lng2 * Math.PI / 180.0);
    let s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
    s = s * 6378.137; // 地球半径
    return s;
  }
});