// 教师端首页逻辑 - index.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil.js');

Page({
    data: {
        userInfo: {},
        walletInfo: {
            balance: '0.00'
        },
        stats: {
            orderCount: 0,
            rating: '5.0'
        },
        nearbyDemands: []
    },

    onShow() {
        this.initData();
    },

    initData() {
        const userInfo = storageUtil.getUserInfo() || {};
        this.setData({ userInfo });

        this.loadWalletInfo();
        this.loadTutorProfile();
        this.loadNearbyDemands();
    },

    // 加载钱包信息
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

    // 加载教员档案
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

    // 加载附近需求
    async loadNearbyDemands() {
        try {
            // 获取当前位置
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
                this.setData({
                    nearbyDemands: records.slice(0, 5)
                });
            }
        } catch (err) {
            console.error('加载附近需求失败:', err);
        }
    },

    // 获取当前位置
    getCurrentLocation() {
        return new Promise((resolve) => {
            wx.getLocation({
                type: 'gcj02',
                success: (res) => {
                    resolve({ longitude: res.longitude, latitude: res.latitude });
                },
                fail: () => {
                    resolve(null);
                }
            });
        });
    },

    // 导航方法
    goToMap() {
        wx.navigateTo({
            url: '/pages/teacher/mapFindStudent/mapFindStudent'
        });
    },

    goToCertification() {
        wx.navigateTo({
            url: '/pages/teacher/certification/step3-result/step3-result'
        });
    },

    goToOrders() {
        wx.navigateTo({
            url: '/pages/teacher/orderList/orderList'
        });
    },

    goToIncome() {
        wx.navigateTo({
            url: '/pages/teacher/incomeDetail/incomeDetail'
        });
    },

    goToDemandDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/teacher/demandDetail/demandDetail?id=${id}`
        });
    }
});
