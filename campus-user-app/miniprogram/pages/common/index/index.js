// 首页逻辑（根据角色显示不同内容） - index.js
import request from '../../../utils/request';
import apiConfig from '../../../config/apiConfig';
const storageUtil = require('../../../utils/storageUtil');

Page({
    data: {
        userInfo: {},
        // 教师端数据
        walletInfo: { balance: '0' },
        stats: { orderCount: 0, rating: '5.0' },
        nearbyDemands: [],
        // 家长端数据
        demandList: [],
        orderList: []
    },

    onShow() {
        this.initData();
    },

    initData() {
        const userInfo = storageUtil.getUserInfo();

        // 检查登录状态，未登录则跳转到登录页
        if (!userInfo || !userInfo.role) {
            wx.redirectTo({ url: '/pages/common/login/login' });
            return;
        }

        this.setData({ userInfo });

        if (userInfo.role === 1) {
            // 教师端
            this.loadWalletInfo();
            this.loadTutorProfile();
            this.loadNearbyDemands();
        } else {
            // 家长端
            this.loadDemands();
            this.loadOrders();
        }
    },

    // ========== 教师端方法 ==========

    async loadWalletInfo() {
        try {
            const result = await request.get(apiConfig.wallet.info);
            this.setData({
                walletInfo: {
                    balance: result.balance ? Number(result.balance).toFixed(0) : '0'
                }
            });
        } catch (err) {
            console.error('加载钱包失败:', err);
        }
    },

    async loadTutorProfile() {
        try {
            const result = await request.get(apiConfig.tutor.profile);
            if (result) {
                this.setData({
                    stats: {
                        orderCount: result.orderCount || 0,
                        rating: result.rating ? Number(result.rating).toFixed(1) : '5.0'
                    }
                });
            }
        } catch (err) {
            console.error('加载档案失败:', err);
        }
    },

    async loadNearbyDemands() {
        try {
            const location = await this.getCurrentLocation();
            if (location) {
                const result = await request.get(apiConfig.demand.nearby, {
                    longitude: location.longitude,
                    latitude: location.latitude,
                    radius: 5000,
                    page: 1,
                    size: 5
                });
                const records = result.records || result || [];
                this.setData({ nearbyDemands: records.slice(0, 5) });
            }
        } catch (err) {
            console.error('加载附近需求失败:', err);
        }
    },

    getCurrentLocation() {
        return new Promise((resolve) => {
            wx.getLocation({
                type: 'gcj02',
                success: (res) => resolve({ longitude: res.longitude, latitude: res.latitude }),
                fail: () => resolve(null)
            });
        });
    },

    goToMap() {
        wx.navigateTo({ url: '/pages/teacher/mapFindStudent/mapFindStudent' });
    },

    goToCertification() {
        wx.navigateTo({ url: '/pages/teacher/certification/step3-result/step3-result' });
    },

    goToTeacherOrders() {
        wx.showToast({ title: '订单功能开发中', icon: 'none' });
    },

    goToIncome() {
        wx.navigateTo({ url: '/pages/teacher/incomeDetail/incomeDetail' });
    },

    // ========== 家长端方法 ==========

    async loadDemands() {
        try {
            const result = await request.get(apiConfig.demand.my, { page: 1, size: 3 });
            const records = result.records || result || [];
            this.setData({
                demandList: records.slice(0, 3).map(item => ({
                    ...item,
                    createTime: this.formatTime(item.createTime)
                }))
            });
        } catch (err) {
            console.error('加载需求失败:', err);
        }
    },

    async loadOrders() {
        try {
            const result = await request.get(apiConfig.order.listParent, { page: 1, size: 3, status: 2 });
            const records = result.records || result || [];
            this.setData({ orderList: records.slice(0, 3) });
        } catch (err) {
            console.error('加载订单失败:', err);
        }
    },

    goToPublish() {
        wx.navigateTo({ url: '/pages/parent/publishDemand/step1-student/step1-student' });
    },

    goToMyDemands() {
        wx.navigateTo({ url: '/pages/parent/demand/myList/myList' });
    },

    goToParentOrders() {
        wx.navigateTo({ url: '/pages/parent/order/list/list' });
    },

    formatTime(timeStr) {
        if (!timeStr) return '';
        const date = new Date(timeStr.replace(/-/g, '/'));
        if (isNaN(date.getTime())) return timeStr;
        const pad = n => String(n).padStart(2, '0');
        return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
    }
});
