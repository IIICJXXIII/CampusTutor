// 家长端首页逻辑 - index.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');
const storageUtil = require('../../../utils/storageUtil');

Page({
    data: {
        userInfo: {},
        demandList: [],
        orderList: []
    },

    onShow() {
        this.initData();
    },

    initData() {
        const userInfo = storageUtil.getUserInfo() || {};
        this.setData({ userInfo });

        this.loadDemands();
        this.loadOrders();
    },

    // 加载我的需求
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

    // 加载我的订单
    async loadOrders() {
        try {
            const result = await request.get(apiConfig.order.listParent, { page: 1, size: 3, status: 2 });
            const records = result.records || result || [];
            this.setData({
                orderList: records.slice(0, 3)
            });
        } catch (err) {
            console.error('加载订单失败:', err);
        }
    },

    // 发布需求
    goToPublish() {
        wx.navigateTo({
            url: '/pages/parent/publishDemand/step1-student/step1-student'
        });
    },

    // 我的需求
    goToMyDemands() {
        wx.navigateTo({
            url: '/pages/parent/demand/myList/myList'
        });
    },

    // 我的订单
    goToOrders() {
        wx.navigateTo({
            url: '/pages/parent/order/list/list'
        });
    },

    // 需求详情
    goToDemandDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/parent/demand/detail/detail?id=${id}`
        });
    },

    // 订单详情
    goToOrderDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/parent/order/detail/detail?id=${id}`
        });
    },

    // 格式化时间
    formatTime(timeStr) {
        if (!timeStr) return '';
        const date = new Date(timeStr.replace(/-/g, '/'));
        if (isNaN(date.getTime())) return timeStr;
        const pad = n => String(n).padStart(2, '0');
        return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
    }
});
