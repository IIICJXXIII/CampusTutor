// 教师课时打卡页面逻辑
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');

Page({
    data: {
        orderId: null,
        orderInfo: {},
        orderList: [], // 可选订单列表
        showOrderPicker: false,
        location: {},
        photoUrl: '',
        contentSummary: '',
        homeworkAssigned: '',
        checkInRecordId: null, // 打卡成功后的记录ID
        isSubmitting: false
    },

    onLoad(options) {
        if (options.orderId) {
            this.setData({ orderId: options.orderId });
            this.loadOrderInfo(options.orderId);
        }
        if (options.recordId) {
            // 如果传了recordId，说明是继续下课打卡
            this.setData({ checkInRecordId: options.recordId });
        }
        this.getLocation();
        this.loadOrders(); // 加载可选订单
    },

    // 加载教师的进行中订单
    async loadOrders() {
        try {
            const result = await request.get(apiConfig.order.listTutor, { page: 1, size: 20, status: 2 });
            const records = result.records || result || [];
            this.setData({ orderList: records });
        } catch (err) {
            console.error('加载订单列表失败:', err);
        }
    },

    // 显示订单选择器
    showOrderPicker() {
        this.setData({ showOrderPicker: true });
    },

    // 隐藏订单选择器
    hideOrderPicker() {
        this.setData({ showOrderPicker: false });
    },

    // 选择订单
    selectOrder(e) {
        const order = e.currentTarget.dataset.order;
        this.setData({
            orderId: order.id,
            orderInfo: order,
            showOrderPicker: false
        });
    },

    // 加载订单信息
    async loadOrderInfo(orderId) {
        try {
            const result = await request.get(apiConfig.order.detail(orderId));
            this.setData({ orderInfo: result || {} });
        } catch (err) {
            console.error('加载订单失败:', err);
        }
    },

    // 获取GPS定位
    getLocation() {
        wx.getLocation({
            type: 'gcj02',
            success: (res) => {
                this.setData({
                    location: {
                        latitude: res.latitude,
                        longitude: res.longitude
                    }
                });
            },
            fail: (err) => {
                console.error('定位失败:', err);
                wx.showToast({ title: '定位失败，请检查权限', icon: 'none' });
            }
        });
    },

    // 重新定位
    refreshLocation() {
        wx.showLoading({ title: '定位中...' });
        this.getLocation();
        setTimeout(() => wx.hideLoading(), 1000);
    },

    // 拍照
    takePhoto() {
        wx.chooseMedia({
            count: 1,
            mediaType: ['image'],
            sourceType: ['camera'],
            camera: 'back',
            success: async (res) => {
                const tempFilePath = res.tempFiles[0].tempFilePath;
                wx.showLoading({ title: '上传中...' });
                try {
                    // 上传到服务器 - 后端返回的是字符串URL
                    const uploadRes = await this.uploadFile(tempFilePath);
                    // 兼容处理：可能是字符串URL或对象{url:...}
                    const photoUrl = typeof uploadRes === 'string' ? uploadRes : (uploadRes.url || tempFilePath);
                    this.setData({ photoUrl });
                    wx.hideLoading();
                } catch (err) {
                    wx.hideLoading();
                    // 上传失败时使用本地路径
                    this.setData({ photoUrl: tempFilePath });
                    console.error('上传失败:', err);
                }
            }
        });
    },

    // 上传文件
    uploadFile(filePath) {
        return new Promise((resolve, reject) => {
            const token = wx.getStorageSync('token');
            wx.uploadFile({
                url: apiConfig.file.upload,
                filePath: filePath,
                name: 'file',
                header: { 'Authorization': `Bearer ${token}` },
                success: (res) => {
                    const data = JSON.parse(res.data);
                    if (data.code === 200) {
                        resolve(data.data);
                    } else {
                        reject(new Error(data.message));
                    }
                },
                fail: reject
            });
        });
    },

    // 输入教学内容
    onContentInput(e) {
        this.setData({ contentSummary: e.detail.value });
    },

    // 输入作业
    onHomeworkInput(e) {
        this.setData({ homeworkAssigned: e.detail.value });
    },

    // 上课打卡
    async handleCheckIn() {
        const { orderId, location, photoUrl } = this.data;

        if (!orderId) {
            return wx.showToast({ title: '请先选择订单', icon: 'none' });
        }
        if (!location.latitude) {
            return wx.showToast({ title: '请先获取位置', icon: 'none' });
        }
        if (!photoUrl) {
            return wx.showToast({ title: '请先拍照', icon: 'none' });
        }

        this.setData({ isSubmitting: true });

        try {
            const result = await request.post(apiConfig.teaching.checkIn, {
                orderId: Number(orderId),
                latitude: location.latitude,
                longitude: location.longitude,
                photoUrl: photoUrl
            });

            wx.showToast({ title: '打卡成功', icon: 'success' });
            this.setData({ checkInRecordId: result });
        } catch (err) {
            console.error('打卡失败:', err);
            wx.showToast({ title: err.message || '打卡失败', icon: 'none' });
        } finally {
            this.setData({ isSubmitting: false });
        }
    },

    // 下课打卡
    async handleCheckOut() {
        const { checkInRecordId, contentSummary, homeworkAssigned } = this.data;

        if (!checkInRecordId) {
            return wx.showToast({ title: '请先完成上课打卡', icon: 'none' });
        }

        this.setData({ isSubmitting: true });

        try {
            await request.post(apiConfig.teaching.checkOut(checkInRecordId), {
                contentSummary: contentSummary,
                homeworkAssigned: homeworkAssigned
            });

            wx.showToast({ title: '下课打卡成功', icon: 'success' });
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
        } catch (err) {
            console.error('下课打卡失败:', err);
            wx.showToast({ title: err.message || '下课打卡失败', icon: 'none' });
        } finally {
            this.setData({ isSubmitting: false });
        }
    }
});
