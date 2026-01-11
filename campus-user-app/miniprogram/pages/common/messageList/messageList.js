const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
    data: {
        systemNotice: null,
        sessions: [],
        unreadCount: 0,
        loading: true
    },

    onShow() {
        this.loadData();
    },

    async loadData() {
        this.setData({ loading: true });

        try {
            // 并行加载数据
            const [sessionsRes, unreadRes] = await Promise.all([
                request.get(api.chat.sessions),
                request.get(api.chat.unreadCount)
            ]);

            this.setData({
                sessions: sessionsRes || [],
                unreadCount: unreadRes || 0,
                loading: false,
                // 模拟系统通知
                systemNotice: {
                    title: '系统通知',
                    content: '欢迎使用校园家教平台',
                    time: '今天',
                    unread: 0
                }
            });
        } catch (err) {
            console.error('加载消息列表失败:', err);
            this.setData({ loading: false });
        }
    },

    // 进入聊天详情
    goToChat(e) {
        const { userId, nickname, avatar } = e.currentTarget.dataset;
        wx.navigateTo({
            url: `/pages/common/chatDetail/chatDetail?userId=${userId}&nickname=${encodeURIComponent(nickname || '')}&avatar=${encodeURIComponent(avatar || '')}`
        });
    },

    // 查看系统通知
    goToNotice() {
        wx.showToast({ title: '暂无更多通知', icon: 'none' });
    },

    // 下拉刷新
    onPullDownRefresh() {
        this.loadData().then(() => {
            wx.stopPullDownRefresh();
        });
    },

    // 格式化时间
    formatTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;

        if (diff < 60000) return '刚刚';
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
        if (diff < 172800000) return '昨天';

        return `${date.getMonth() + 1}/${date.getDate()}`;
    }
});
