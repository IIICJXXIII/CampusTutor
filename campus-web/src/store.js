import { reactive } from 'vue';

// 全局响应式数据仓库 (就像一个小型的数据库)
export const store = reactive({
  // 1. 当前用户身份 (同步 localStorage)
  userRole: localStorage.getItem('userRole') || 'parent',
  isCertified: false,
  // 2. 模拟订单列表 (初始有一些历史数据，为了让列表不为空)
  orders: [
    {
      id: 'ORD-20241210-33',
      teacher: '王老师',
      subject: '高中物理 · 考前冲刺',
      amount: 2000,
      status: 'done',
      date: '2024-12-10',
      tags: ['已结清']
    }
  ],

  // 3. 模拟发布的课时/课程 (用于课时记录页)
  currentClass: {
    id: 101,
    subject: '初中数学 (一对一)',
    time: '19:00 - 21:00',
    location: '幸福小区3号楼',
    teacher: '张老师',
    status: 'ready', // ready(未开始) | checkin(已打卡) | confirmed(已确认)
    checkinImg: null
  },

  // === 动作 Actions (修改数据的方法) ===
  
  // 切换身份
  setRole(role) {
    this.userRole = role;
    localStorage.setItem('userRole', role);
  },
  setCertification(status) {
    this.isCertified = status;
  },

  // 创建新订单 (签约后调用)
  addOrder(order) {
    this.orders.unshift(order); // 加到数组最前面
  },

  // 更新订单状态 (支付后调用)
  updateOrderStatus(orderId, status) {
    const order = this.orders.find(o => o.id === orderId);
    if (order) order.status = status;
  },

  // 老师打卡
  teacherCheckIn(imgUrl) {
    this.currentClass.status = 'checkin';
    this.currentClass.checkinImg = imgUrl;
  },

  // 家长确认课时
  parentConfirmClass() {
    this.currentClass.status = 'confirmed';
  }
});