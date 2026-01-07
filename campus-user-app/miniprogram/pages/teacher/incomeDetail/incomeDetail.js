// 收入明细页面逻辑 - incomeDetail.js
const request = require('../../../utils/request.js');
const apiConfig = require('../../../config/apiConfig.js');

Page({
    data: {
        // 钱包信息
        walletInfo: {
            balance: '0.00',
            frozenAmount: '0.00'
        },

        // Tab状态
        currentTab: 0,

        // 交易流水
        transactionList: [],
        transactionPage: 1,
        transactionHasMore: true,
        transactionLoading: false,
        transactionRefreshing: false,

        // 提现记录
        withdrawalList: [],
        withdrawalPage: 1,
        withdrawalHasMore: true,
        withdrawalLoading: false,
        withdrawalRefreshing: false,

        // 提现弹窗
        showWithdraw: false,
        withdrawForm: {
            amount: '',
            channel: 1,
            accountNo: ''
        },
        submitLoading: false
    },

    onLoad() {
        this.loadWalletInfo();
        this.loadTransactions();
    },

    onShow() {
        // 每次显示页面时刷新钱包信息
        this.loadWalletInfo();
    },

    // 加载钱包信息
    async loadWalletInfo() {
        try {
            const result = await request.get(apiConfig.wallet.info);
            this.setData({
                walletInfo: {
                    balance: result.balance ? Number(result.balance).toFixed(2) : '0.00',
                    frozenAmount: result.frozenAmount ? Number(result.frozenAmount).toFixed(2) : '0.00'
                }
            });
        } catch (err) {
            console.error('获取钱包信息失败:', err);
        }
    },

    // 切换Tab
    switchTab(e) {
        const tab = Number(e.currentTarget.dataset.tab);
        if (tab === this.data.currentTab) return;

        this.setData({ currentTab: tab });

        // 懒加载数据
        if (tab === 0 && this.data.transactionList.length === 0) {
            this.loadTransactions();
        } else if (tab === 1 && this.data.withdrawalList.length === 0) {
            this.loadWithdrawals();
        }
    },

    // 加载交易流水
    async loadTransactions(isRefresh = false) {
        if (this.data.transactionLoading) return;
        if (!isRefresh && !this.data.transactionHasMore) return;

        const page = isRefresh ? 1 : this.data.transactionPage;

        this.setData({ transactionLoading: true });

        try {
            const result = await request.get(apiConfig.wallet.transactions, { page, size: 10 });

            // 格式化时间
            const records = (result.records || []).map(item => ({
                ...item,
                createTime: this.formatTime(item.createTime),
                amount: Number(item.amount).toFixed(2),
                balanceAfter: Number(item.balanceAfter).toFixed(2)
            }));

            const newList = isRefresh ? records : [...this.data.transactionList, ...records];

            this.setData({
                transactionList: newList,
                transactionPage: page + 1,
                transactionHasMore: records.length >= 10,
                transactionLoading: false,
                transactionRefreshing: false
            });
        } catch (err) {
            console.error('获取交易流水失败:', err);
            this.setData({
                transactionLoading: false,
                transactionRefreshing: false

            });
        }
    },

    // 加载提现记录
    async loadWithdrawals(isRefresh = false) {
        if (this.data.withdrawalLoading) return;
        if (!isRefresh && !this.data.withdrawalHasMore) return;

        const page = isRefresh ? 1 : this.data.withdrawalPage;

        this.setData({ withdrawalLoading: true });

        try {
            const result = await request.get(apiConfig.wallet.withdrawals, { page, size: 10 });

            // 格式化时间
            const records = (result.records || []).map(item => ({
                ...item,
                createTime: this.formatTime(item.createTime),
                amount: Number(item.amount).toFixed(2)
            }));

            const newList = isRefresh ? records : [...this.data.withdrawalList, ...records];

            this.setData({
                withdrawalList: newList,
                withdrawalPage: page + 1,
                withdrawalHasMore: records.length >= 10,
                withdrawalLoading: false,
                withdrawalRefreshing: false
            });
        } catch (err) {
            console.error('获取提现记录失败:', err);
            this.setData({
                withdrawalLoading: false,
                withdrawalRefreshing: false
            });
        }
    },

    // 下拉刷新交易流水
    refreshTransactions() {
        this.setData({ transactionRefreshing: true });
        this.loadWalletInfo();
        this.loadTransactions(true);
    },

    // 上拉加载更多交易流水
    loadMoreTransactions() {
        this.loadTransactions();
    },

    // 下拉刷新提现记录
    refreshWithdrawals() {
        this.setData({ withdrawalRefreshing: true });
        this.loadWithdrawals(true);
    },

    // 上拉加载更多提现记录
    loadMoreWithdrawals() {
        this.loadWithdrawals();
    },

    // 显示提现弹窗
    showWithdrawPopup() {
        this.setData({
            showWithdraw: true,
            withdrawForm: {
                amount: '',
                channel: 1,
                accountNo: ''
            }
        });
    },

    // 隐藏提现弹窗
    hideWithdrawPopup() {
        this.setData({ showWithdraw: false });
    },

    // 输入提现金额
    onAmountInput(e) {
        this.setData({
            'withdrawForm.amount': e.detail.value
        });
    },

    // 选择提现渠道
    selectChannel(e) {
        const channel = Number(e.currentTarget.dataset.channel);
        this.setData({
            'withdrawForm.channel': channel
        });
    },

    // 输入收款账号
    onAccountInput(e) {
        this.setData({
            'withdrawForm.accountNo': e.detail.value
        });
    },

    // 提交提现申请
    async submitWithdraw() {
        const { amount, channel, accountNo } = this.data.withdrawForm;

        // 表单验证
        if (!amount || Number(amount) < 1) {
            wx.showToast({ title: '提现金额不能小于1元', icon: 'none' });
            return;
        }

        if (Number(amount) > Number(this.data.walletInfo.balance)) {
            wx.showToast({ title: '提现金额不能超过可用余额', icon: 'none' });
            return;
        }

        if (!accountNo.trim()) {
            wx.showToast({ title: '请输入收款账号', icon: 'none' });
            return;
        }

        this.setData({ submitLoading: true });

        try {
            await request.post(apiConfig.wallet.withdraw, {
                amount: Number(amount),
                channel,
                accountNo: accountNo.trim()
            });

            wx.showToast({ title: '提现申请已提交', icon: 'success' });

            this.hideWithdrawPopup();

            // 刷新数据
            this.loadWalletInfo();
            if (this.data.currentTab === 1) {
                this.loadWithdrawals(true);
            } else {
                // 重置提现记录，下次切换时重新加载
                this.setData({
                    withdrawalList: [],
                    withdrawalPage: 1,
                    withdrawalHasMore: true
                });
            }
        } catch (err) {
            console.error('提现申请失败:', err);
        } finally {
            this.setData({ submitLoading: false });
        }
    },

    // 格式化时间
    formatTime(timeStr) {
        if (!timeStr) return '';
        // 处理后端返回的时间格式
        const date = new Date(timeStr.replace(/-/g, '/'));
        if (isNaN(date.getTime())) return timeStr;

        const pad = n => String(n).padStart(2, '0');
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
    }
});
