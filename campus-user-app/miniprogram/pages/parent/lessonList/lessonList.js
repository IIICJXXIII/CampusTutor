// 家长课时列表页面逻辑
import request from '../../../utils/request';
import apiConfig from '../../../config/apiConfig';

Page({
    data: {
        lessonList: [],
        pendingCount: 0,
        showDispute: false,
        disputeRecordId: null,
        disputeReason: '',
        isSubmitting: false
    },

    onShow() {
        this.loadLessons();
    },

    // 加载课时记录
    async loadLessons() {
        try {
            const result = await request.get(apiConfig.teaching.myRecords);
            const list = result || [];

            // 格式化时间
            const formattedList = list.map(item => ({
                ...item,
                startTimeText: this.formatTime(item.startTime),
                endTimeText: this.formatTime(item.endTime)
            }));

            // 计算待确认数量（已下课但未确认的）
            const pendingCount = list.filter(i => i.status === 0 && i.endTime).length;

            this.setData({ lessonList: formattedList, pendingCount });
        } catch (err) {
            console.error('加载课时失败:', err);
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    // 确认课时
    async handleConfirm(e) {
        const recordId = e.currentTarget.dataset.id;

        wx.showModal({
            title: '确认课时',
            content: '确认教师已完成本节课吗？确认后课时费将结算给教师。',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        await request.post(apiConfig.teaching.confirm(recordId));
                        wx.showToast({ title: '确认成功', icon: 'success' });
                        this.loadLessons();
                    } catch (err) {
                        console.error('确认失败:', err);
                        wx.showToast({ title: err.message || '确认失败', icon: 'none' });
                    }
                }
            }
        });
    },

    // 显示申诉弹窗
    showDisputeModal(e) {
        const recordId = e.currentTarget.dataset.id;
        this.setData({
            showDispute: true,
            disputeRecordId: recordId,
            disputeReason: ''
        });
    },

    // 隐藏申诉弹窗
    hideDisputeModal() {
        this.setData({
            showDispute: false,
            disputeRecordId: null,
            disputeReason: ''
        });
    },

    // 输入申诉原因
    onDisputeInput(e) {
        this.setData({ disputeReason: e.detail.value });
    },

    // 提交申诉
    async handleDispute() {
        const { disputeRecordId, disputeReason } = this.data;

        if (!disputeReason.trim()) {
            return wx.showToast({ title: '请填写申诉原因', icon: 'none' });
        }

        this.setData({ isSubmitting: true });

        try {
            await request.post(apiConfig.teaching.dispute(disputeRecordId), null, {}, {
                reason: disputeReason
            });

            wx.showToast({ title: '申诉已提交', icon: 'success' });
            this.hideDisputeModal();
            this.loadLessons();
        } catch (err) {
            console.error('申诉失败:', err);
            wx.showToast({ title: err.message || '申诉失败', icon: 'none' });
        } finally {
            this.setData({ isSubmitting: false });
        }
    },

    // 查看详情
    goToDetail(e) {
        const item = e.currentTarget.dataset.item;
        wx.navigateTo({
            url: `/pages/parent/lessonDetail/lessonDetail?id=${item.id}`
        });
    },

    // 格式化时间
    formatTime(timeStr) {
        if (!timeStr) return '';
        try {
            const date = new Date(timeStr.replace(/-/g, '/'));
            if (isNaN(date.getTime())) return timeStr;
            const pad = n => String(n).padStart(2, '0');
            return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
        } catch (e) {
            return timeStr;
        }
    }
});
