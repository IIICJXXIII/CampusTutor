import request from '../../../utils/request';
import api from '../../../config/apiConfig';

Page({
  data: {
    latitude: 39.9042, // 默认北京坐标，后续会被真实定位覆盖
    longitude: 116.4074,
    markers: [],
    selectedDemand: null, // 当前选中的需求
  },

  onLoad() {
    this.getCurrentLocation();
  },

  onShow() {
    // 每次显示页面时刷新数据
    if (this.data.latitude && this.data.longitude) {
      this.getNearbyDemands(this.data.latitude, this.data.longitude);
    }
  },

  // 1. 获取当前位置
  getCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
        // 获取附近需求
        this.getNearbyDemands(res.latitude, res.longitude);
      },
      fail: () => {
        wx.showToast({ title: '请授权位置信息以获取附近需求', icon: 'none' });
        // 授权失败也尝试获取一次数据（用默认坐标）
        this.getNearbyDemands(this.data.latitude, this.data.longitude);
      }
    });
  },

  // 2. 移动地图到当前位置
  moveToLocation() {
    const mapCtx = wx.createMapContext('myMap');
    mapCtx.moveToLocation();
    this.getCurrentLocation(); // 重新获取数据
  },

  // 3. 获取附近需求数据
  async getNearbyDemands(lat, lng) {
    try {
      // 调用后端接口
      const res = await request.get(api.demand.nearby, {
        latitude: lat,
        longitude: lng,
        radius: 10 // 搜索半径 10km
      });

      // 转换数据为 Markers
      const demands = res || []; 
      const markers = demands.map(item => ({
        id: item.id,
        latitude: item.latitude,
        longitude: item.longitude,
        iconPath: '/static/images/marker-student.png', // 请确保 static/images 下有此图标，否则不显示
        width: 40,
        height: 40,
        callout: {
          content: `${item.grade}${item.subject} ¥${item.expectPrice}`,
          padding: 8,
          borderRadius: 4,
          display: 'ALWAYS',
          bgColor: '#ffffff',
          color: '#409EFF'
        },
        // 保存完整数据以便点击时使用
        rawData: item 
      }));

      // --- Mock 数据逻辑 (如果后端返回空，用于演示) ---
      if (markers.length === 0) {
        markers.push({
          id: 999,
          latitude: lat + 0.005,
          longitude: lng + 0.005,
          width: 40,
          height: 40,
          iconPath: '/static/images/marker-student.png', // 临时用
          callout: { content: '小学数学 ¥150', padding: 8, borderRadius: 4, display: 'ALWAYS' },
          rawData: {
            id: 999,
            subject: '数学',
            grade: '小学三年级',
            expectPrice: 150,
            address: '幸福家园小区',
            detail: '基础较弱，需要耐心辅导',
            distance: 0.8
          }
        });
      }
      // ---------------------------------------------

      this.setData({ markers });

    } catch (err) {
      console.error('获取附近需求失败', err);
    }
  },

  // 4. 点击 Marker
  handleMarkerTap(e) {
    const markerId = e.detail.markerId;
    const targetMarker = this.data.markers.find(m => m.id === markerId);
    
    if (targetMarker) {
      this.setData({
        selectedDemand: targetMarker.rawData
      });
    }
  },

  // 5. 点击地图空白处，关闭卡片
  closeCard() {
    this.setData({ selectedDemand: null });
  },

  // 6. 联系家长（模拟）
  handleContact() {
    wx.showModal({
      title: '提示',
      content: '即将拨打虚拟中间号联系家长',
      confirmText: '呼叫',
      success: (res) => {
        if (res.confirm) wx.showToast({ title: '呼叫中...', icon: 'none' });
      }
    });
  },

  // 7. 立即接单
  async handleApply() {
    if (!this.data.selectedDemand) return;
    
    wx.showLoading({ title: '接单中...' });
    try {
      // 这里应该调用 create order 接口，或者先创建一个 match 记录
      // 简化 MVP：直接提示成功并跳转
      // await request.post(api.order.create, { ... });
      
      setTimeout(() => {
        wx.hideLoading();
        wx.showToast({ title: '接单申请已发送', icon: 'success' });
        this.setData({ selectedDemand: null });
      }, 1000);
    } catch(err) {
      wx.hideLoading();
    }
  }
});