// 教师课表页面逻辑 - 修复版本
import request from '../../../utils/request';
import apiConfig from '../../../config/apiConfig';

Page({
    data: {
        weekDays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        // 40分钟一节课，10分钟休息
        timeSlots: [
            { label: '08:00', startTime: '08:00', endTime: '08:40' },
            { label: '08:50', startTime: '08:50', endTime: '09:30' },
            { label: '09:40', startTime: '09:40', endTime: '10:20' },
            { label: '10:30', startTime: '10:30', endTime: '11:10' },
            { label: '11:20', startTime: '11:20', endTime: '12:00' },
            { label: '14:00', startTime: '14:00', endTime: '14:40' },
            { label: '14:50', startTime: '14:50', endTime: '15:30' },
            { label: '15:40', startTime: '15:40', endTime: '16:20' },
            { label: '16:30', startTime: '16:30', endTime: '17:10' },
            { label: '17:20', startTime: '17:20', endTime: '18:00' },
            { label: '19:00', startTime: '19:00', endTime: '19:40' },
            { label: '19:50', startTime: '19:50', endTime: '20:30' },
            { label: '20:40', startTime: '20:40', endTime: '21:20' }
        ],
        // 用于渲染的网格数据，每个元素包含 active 状态
        gridData: [],
        isSaving: false
    },

    onLoad() {
        this.initGridData();
        this.loadSchedule();
    },

    // 初始化网格数据
    initGridData() {
        const gridData = [];
        for (let slotIdx = 0; slotIdx < this.data.timeSlots.length; slotIdx++) {
            const row = [];
            for (let dayIdx = 0; dayIdx < 7; dayIdx++) {
                row.push({ active: false, slotIdx, dayIdx });
            }
            gridData.push(row);
        }
        this.setData({ gridData });
    },

    // 加载已保存的课表
    async loadSchedule() {
        try {
            const result = await request.get(apiConfig.tutor.schedule);
            if (result && Array.isArray(result)) {
                this.parseScheduleFromServer(result);
            }
        } catch (err) {
            console.error('加载课表失败:', err);
        }
    },

    // 解析服务器返回的课表数据
    parseScheduleFromServer(serverData) {
        const gridData = this.data.gridData;

        serverData.forEach(item => {
            if (item.available === 1) {
                const dayIndex = item.dayOfWeek - 1;
                const slotIndex = this.matchTimeSlot(item.startTime);
                if (slotIndex !== -1 && dayIndex >= 0 && dayIndex < 7) {
                    gridData[slotIndex][dayIndex].active = true;
                }
            }
        });

        this.setData({ gridData });
    },

    // 匹配时间段
    matchTimeSlot(startTime) {
        const slots = this.data.timeSlots;
        for (let i = 0; i < slots.length; i++) {
            if (slots[i].startTime === startTime) {
                return i;
            }
        }
        return -1;
    },

    // 切换单元格状态
    toggleCell(e) {
        const { slot, day } = e.currentTarget.dataset;
        const key = `gridData[${slot}][${day}].active`;
        const currentValue = this.data.gridData[slot][day].active;
        this.setData({ [key]: !currentValue });
    },

    // 保存课表
    async saveSchedule() {
        const { gridData, timeSlots } = this.data;

        // 构造请求数据
        const schedules = [];
        gridData.forEach((row, slotIndex) => {
            row.forEach((cell, dayIndex) => {
                schedules.push({
                    dayOfWeek: dayIndex + 1,
                    startTime: timeSlots[slotIndex].startTime,
                    endTime: timeSlots[slotIndex].endTime,
                    available: cell.active ? 1 : 0
                });
            });
        });

        this.setData({ isSaving: true });

        try {
            await request.post(apiConfig.tutor.schedule, { schedules });
            wx.showToast({ title: '保存成功', icon: 'success' });
        } catch (err) {
            console.error('保存失败:', err);
            wx.showToast({ title: err.message || '保存失败', icon: 'none' });
        } finally {
            this.setData({ isSaving: false });
        }
    }
});
