// 教师课时列表页面逻辑
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');

Page({
    data: {
        lessonList: [],
        stats: {
            total: 0,
            confirmed: 0,
            pending: 0
        }
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

            // 计算统计
            const stats = {
                total: list.length,
                confirmed: list.filter(i => i.status === 1).length,
                pending: list.filter(i => i.status === 0).length
            };

            this.setData({ lessonList: formattedList, stats });
        } catch (err) {
            console.error('加载课时失败:', err);
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    // 跳转到打卡页面
    goToCheckIn() {
        // 需要先选择订单，这里简化处理跳转到订单列表选择
        wx.showActionSheet({
            itemList: ['选择订单后打卡'],
            success: () => {
                wx.navigateTo({
                    url: '/pages/teacher/lessonCheckIn/lessonCheckIn'
                });
            }
        });
    },

    // 下课打卡
    goToCheckOut(e) {
        const item = e.currentTarget.dataset.item;
        wx.navigateTo({
            url: `/pages/teacher/lessonCheckIn/lessonCheckIn?recordId=${item.id}&orderId=${item.orderId}`
        });
    },

    // 查看详情
    goToDetail(e) {
        const id = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/teacher/lessonDetail/lessonDetail?id=${id}`
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
