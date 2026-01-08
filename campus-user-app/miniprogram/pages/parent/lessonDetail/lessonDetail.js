// 家长课时详情页面逻辑
const request = require('../../../utils/request');
const apiConfig = require('../../../config/apiConfig');

Page({
    data: {
        recordId: null,
        record: {},
        showDispute: false,
        disputeReason: '',
        isSubmitting: false
    },

    onLoad(options) {
        if (options.id) {
            this.setData({ recordId: options.id });
            this.loadDetail(options.id);
        }
    },

    // 加载详情
    async loadDetail(id) {
        try {
            const result = await request.get(apiConfig.teaching.detail(id));
            this.setData({
                record: {
                    ...result,
                    startTimeText: this.formatTime(result.startTime),
                    endTimeText: this.formatTime(result.endTime)
                }
            });
        } catch (err) {
            console.error('加载详情失败:', err);
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    // 预览照片
    previewPhoto() {
        if (this.data.record.clockInImg) {
            wx.previewImage({
                urls: [this.data.record.clockInImg],
                current: this.data.record.clockInImg
            });
        }
    },

    // 确认课时
    async handleConfirm() {
        const { recordId } = this.data;

        wx.showModal({
            title: '确认课时',
            content: '确认教师已完成本节课吗？确认后课时费将结算给教师。',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        await request.post(apiConfig.teaching.confirm(recordId), {});
                        wx.showToast({ title: '确认成功', icon: 'success' });
                        this.loadDetail(recordId);
                    } catch (err) {
                        console.error('确认失败:', err);
                        wx.showToast({ title: err.message || '确认失败', icon: 'none' });
                    }
                }
            }
        });
    },

    // 显示申诉弹窗
    showDisputeModal() {
        this.setData({ showDispute: true, disputeReason: '' });
    },

    // 隐藏申诉弹窗
    hideDisputeModal() {
        this.setData({ showDispute: false, disputeReason: '' });
    },

    // 输入申诉原因
    onDisputeInput(e) {
        this.setData({ disputeReason: e.detail.value });
    },

    // 提交申诉
    async handleDispute() {
        const { recordId, disputeReason } = this.data;

        if (!disputeReason.trim()) {
            return wx.showToast({ title: '请填写申诉原因', icon: 'none' });
        }

        this.setData({ isSubmitting: true });

        try {
            await request.post(apiConfig.teaching.dispute(recordId), {
                reason: disputeReason
            });

            wx.showToast({ title: '申诉已提交', icon: 'success' });
            this.hideDisputeModal();
            this.loadDetail(recordId);
        } catch (err) {
            console.error('申诉失败:', err);
            wx.showToast({ title: err.message || '申诉失败', icon: 'none' });
        } finally {
            this.setData({ isSubmitting: false });
        }
    },

    // 格式化时间
    formatTime(timeStr) {
        if (!timeStr) return '';
        try {
            const date = new Date(timeStr.replace(/-/g, '/'));
            if (isNaN(date.getTime())) return timeStr;
            const pad = n => String(n).padStart(2, '0');
            return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
        } catch (e) {
            return timeStr;
        }
    }
});
