const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    latitude: 39.9088, // 默认北京坐标，防止未授权时地图白屏
    longitude: 116.3975,
    markers: [],
    demandList: [],
    currentDemand: null, // 当前选中的需求
    isLoading: false,
    // 降级与诊断信息
    isFallback: false,
    fallbackMessage: ''
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
        // 先显示诊断信息并尝试降级展示系统公开的需求（不基于 LBS）
        this.setData({
          demandList: [],
          markers: [],
          currentDemand: null,
          isLoading: false,
          fallbackMessage: '未检索到附近需求（可能是未填写位置或位置索引暂不可用），显示系统推荐',
          isFallback: true
        });
        console.log('附近暂无需求数据，尝试降级获取系统推荐');

        // 请求公开列表作为降级方案
        try {
          const listRes = await request.get(api.demand.list, { page: 1, size: 20 });
          const records = (listRes && listRes.records) ? listRes.records : [];
          // 计算距离（如果条目有坐标）
          const list = records.map(item => ({
            ...item,
            distance: (item.latitude && item.longitude) ? this.getDistance(this.data.latitude, this.data.longitude, item.latitude, item.longitude).toFixed(1) : ''
          }));

          const markers = list
            .filter(i => i.latitude && i.longitude)
            .map(item => ({ id: item.id, latitude: item.latitude, longitude: item.longitude, width:30, height:30, callout: { content: `¥${item.expectPrice}\n${item.subject} ${item.grade}`, padding:8, borderRadius:4, display:'ALWAYS', textAlign:'center'} }));

          this.setData({ demandList: list, markers: markers, currentDemand: list[0] || null, isLoading: false });
        } catch (err) {
          console.error('降级获取公开需求失败', err);
        }
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
  async handleAccept() {
    if (!this.data.currentDemand) return;
    const id = this.data.currentDemand.id;
    try {
      const res = await request.post(api.demand.match(id));
      const newList = this.data.demandList.map(d => d.id === id ? ({ ...d, status: res && res.status ? res.status : 2 }) : d);
      const newMarkers = this.data.markers.filter(m => m.id !== id);
      this.setData({ demandList: newList, markers: newMarkers, currentDemand: null });
      wx.showToast({ title: '接单成功', icon: 'success' });
    } catch (err) {
      console.error('接单失败', err);
      const statusCode = err && err.statusCode ? err.statusCode : (err && err.code ? err.code : null);
      if (statusCode === 404) {
        wx.showModal({
          title: '接单暂不可用',
          content: '后端接口未部署（404）或当前不支持接单，建议联系管理员或复制需求ID进行反馈。是否复制需求ID？',
          confirmText: '复制ID',
          cancelText: '知道了',
          success: (mres) => {
            if (mres.confirm) {
              wx.setClipboardData({ data: String(id), success() { wx.showToast({ title: '已复制ID', icon: 'none' }); } });
            }
          }
        });
      } else {
        wx.showToast({ title: '接单失败，网络或权限异常', icon: 'none' });
      }
    }
  },



  // 点击“查看详情”
  handleViewDetail() {
    if (!this.data.currentDemand) return;
    // 跳转到需求详情页面
    wx.navigateTo({
      url: `/pages/teacher/demandDetail/demandDetail?id=${this.data.currentDemand.id}`
    });
  },

  // 打开列表页，传递当前筛选条件（如果有）
  handleOpenList() {
    const subject = this.data.currentDemand ? encodeURIComponent(this.data.currentDemand.subject || '') : '';
    const grade = this.data.currentDemand ? encodeURIComponent(this.data.currentDemand.grade || '') : '';
    wx.navigateTo({ url: `/pages/teacher/findStudentList/findStudentList?subject=${subject}&grade=${grade}` });
  },

  // 从地图页联系家长（尝试复制手机号或弹提示）
  async handleContactFromMap() {
    if (!this.data.currentDemand) return;
    const demand = this.data.currentDemand;
    const phone = demand.parentPhone || demand.phone || demand.mobile || null;
    if (phone) {
      wx.setClipboardData({ data: String(phone), success() { wx.showToast({ title: '已复制家长手机号', icon: 'none' }); } });
      return;
    }

    // 尝试按发布者ID获取用户信息（username通常是手机号）
    const publisherId = demand.publisherId || demand.parentId || null;
    if (publisherId) {
      try {
        const userRes = await request.get(api.user.byId(publisherId));
        if (userRes && (userRes.username || userRes.nickname)) {
          const contactValue = userRes.username || userRes.nickname;
          wx.setClipboardData({ data: String(contactValue), success() { wx.showToast({ title: '已复制家长联系方式（用户名）', icon: 'none' }); } });
          return;
        }
      } catch (err) {
        console.warn('获取发布者信息失败', err);
      }
    }

    wx.showModal({ title: '联系方式不可见', content: '家长未公开联系方式，建议复制需求ID并反馈或等待平台开放沟通接口', showCancel: false });
  },

  // 复制当前需求ID
  copyCurrentDemandId() {
    if (!this.data.currentDemand) return;
    const id = this.data.currentDemand.id;
    wx.setClipboardData({ data: String(id), success() { wx.showToast({ title: '已复制ID', icon: 'none' }); } });
  },

  // 邀约家长下单（替代立即接单），先尝试联系并弹出说明
  async handleInviteToOrder() {
    if (!this.data.currentDemand) return;
    // 先触发联系逻辑（会尝试复制手机号或用户名）
    await this.handleContactFromMap();

    // 弹窗引导教师邀约家长下单
    wx.showModal({
      title: '邀约下单',
      content: '请通过已复制的联系方式联系家长，确认时间与价格后邀请家长在家长端发起订单并支付。若需要进一步协助，可复制需求ID并反馈给平台管理员。是否复制需求ID？',
      confirmText: '复制ID',
      cancelText: '知道了',
      success: (res) => {
        if (res.confirm) {
          this.copyCurrentDemandId();
        }
      }
    });
  },

  // 点击降级列表项查看详情
  handleFallbackViewDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: `/pages/teacher/demandDetail/demandDetail?id=${id}` });
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