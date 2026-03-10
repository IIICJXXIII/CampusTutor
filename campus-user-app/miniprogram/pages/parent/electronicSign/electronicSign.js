Page({
    data: {
        orderNo: 'ORD-20230520-8821',
        subject: '中考体育专项',
        grade: '13-15岁',
        teachMode: 1,
        unitPrice: 120,
        totalHours: 20,
        totalAmount: 2400,
        parentName: '李华',
        tutorName: '王明',
        currentDate: '2023-05-20',
        isAgreed: false,
        isSigned: false
    },

    onLoad(options) {
        const today = new Date();
        this.setData({
            currentDate: `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`
        });
        // In a real app, load order details using options.orderId
    },

    handleAgreementChange(e) {
        this.setData({
            isAgreed: e.detail.value.length > 0
        });
    },

    handleSign() {
        if (!this.data.isAgreed) return;

        wx.showModal({
            title: '确认签约',
            content: `确认签署 ${this.data.orderNo} 订单电子合同？`,
            confirmColor: '#1E6F9F',
            success: (res) => {
                if (res.confirm) {
                    wx.showLoading({ title: '签约中...' });

                    // Simulate API call
                    setTimeout(() => {
                        wx.hideLoading();
                        this.setData({ isSigned: true });
                        wx.showToast({
                            title: '签约成功',
                            icon: 'success'
                        });

                        // Navigate back after delay
                        setTimeout(() => {
                            wx.navigateBack();
                        }, 1500);
                    }, 1000);
                }
            }
        });
    }
});
