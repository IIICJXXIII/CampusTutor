import request from '../../../../utils/request';
import api from '../../../../config/apiConfig';

Page({
  data: {
    address: '',
    addressDetail: '',
    latitude: 0,
    longitude: 0,
    previewData: {}, // 用于页面展示的数据
    isSubmitting: false
  },

  onLoad() {
    // 1. 获取前两步的所有汇总数据
    const flowData = wx.getStorageSync('current_demand_data') || {};
    
    // 2. 格式化预览数据
    const scheduleStr = flowData.scheduleRequire ? flowData.scheduleRequire.join('、') : '';
    
    this.setData({
      previewData: {
        ...flowData,
        scheduleStr
      }
    });

    // 3. 自动定位一次（提升体验）
    this.getCurrentLocation();
  },

  // 获取当前粗略位置
  getCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        // 如果用户还没手动选点，先用当前定位兜底
        if (!this.data.address) {
          this.setData({
            latitude: res.latitude,
            longitude: res.longitude,
            // 自动定位没有中文地址，显示经纬度或提示用户手动选
            address: '当前位置 (点击精确选择)' 
          });
        }
      }
    });
  },

  // 打开地图选点
  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        console.log('选点结果：', res);
        this.setData({
          address: res.name, // 地点名称（如：幸福小区）
          addressDetail: res.address, // 详细地址
          latitude: res.latitude,
          longitude: res.longitude
        });
      },
      fail: (err) => {
        if (err.errMsg.indexOf('auth') !== -1) {
          wx.showModal({
            title: '需要授权',
            content: '请在设置中开启位置权限',
            success: (res) => {
              if (res.confirm) wx.openSetting();
            }
          });
        }
      }
    });
  },

  prevStep() {
    wx.navigateBack();
  },

  // 提交最终需求
  async submitDemand() {
    // 1. 校验位置
    if (!this.data.latitude || !this.data.longitude) {
      return wx.showToast({ title: '请选择上门/授课地址', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    try {
      const flowData = this.data.previewData;

      // 2. 构造请求参数 (对应后端 DemandPostRequest)
      const postData = {
        studentId: flowData.studentId, // Step 1 拿到的 ID
        // 自动生成一个标题，例如 "小学三年级数学辅导"
        title: `${flowData.grade}${flowData.subject}辅导`, 
        subject: flowData.subject,
        grade: flowData.grade,
        expectPrice: flowData.expectPrice,
        scheduleRequire: flowData.scheduleRequire || [],
        teachMode: flowData.teachMode,
        detail: flowData.detail || '',
        
        // 核心 LBS 数据
        longitude: this.data.longitude,
        latitude: this.data.latitude,
        address: this.data.address // 保存地名
      };

      console.log('提交需求参数:', postData);

      // 3. 调用发布接口
      await request.post(api.demand.publish, postData);

      wx.showToast({ title: '发布成功', icon: 'success' });

      // 4. 清除流程缓存
      wx.removeStorageSync('current_demand_data');
      wx.removeStorageSync('demand_draft_step1');
      wx.removeStorageSync('demand_draft_step2');

      // 5. 跳转到匹配结果页 (1.5秒后)
      setTimeout(() => {
        // 使用 reLaunch 或 redirectTo 防止用户点返回键回到表单
        wx.reLaunch({
          url: '/pages/parent/matchResult/matchResult'
        });
      }, 1500);

    } catch (err) {
      console.error('发布失败', err);
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});