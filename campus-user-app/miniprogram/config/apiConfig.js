// 基础环境配置
const ENV = 'dev'; // dev | prod

const BASE_URL = {
  dev: 'http://localhost:8080', // 你的本地后端地址，真机调试需换成局域网IP或内网穿透地址
  prod: 'https://api.yourdomain.com'
};

const API_HOST = BASE_URL[ENV];

const api = {
  BASE_URL: API_HOST,
  host: API_HOST,
  // 认证模块
  auth: {
    login: `${API_HOST}/api/auth/login`,
    register: `${API_HOST}/api/auth/register`,
    sendCode: `${API_HOST}/api/auth/send-code`,
    wxLogin: `${API_HOST}/api/auth/wx-login`
  },
  // 用户模块
  user: {
    info: `${API_HOST}/api/user`,
    byId: (id) => `${API_HOST}/api/user/${id}`,
    updateInfo: `${API_HOST}/api/user/info`,
  },
  // 文件上传
  file: {
    upload: `${API_HOST}/api/file/upload`,
  },
  // 家教模块 (Tutor)
  tutor: {
    profile: `${API_HOST}/api/tutor/profile`,
    certification: `${API_HOST}/api/tutor/certification`,
    schedule: `${API_HOST}/api/tutor/schedule`,
    publicSchedule: (tutorId) => `${API_HOST}/api/tutor/public/schedule/${tutorId}`,
    // 教师公开详情 (后端为 /api/tutor/public/profile/{id})
    detail: (id) => `${API_HOST}/api/tutor/public/profile/${id}`,
  },
  // 家长模块 (Parent)
  parent: {
    student: `${API_HOST}/api/parent/student`,
    myStudents: `${API_HOST}/api/parent/students`,
  },
  // 需求模块 (Demand)
  demand: {
    publish: `${API_HOST}/api/demand/publish`,
    list: `${API_HOST}/api/demand/list`,
    my: `${API_HOST}/api/demand/my`,
    nearby: `${API_HOST}/api/demand/nearby`,
    detail: (id) => `${API_HOST}/api/demand/detail/${id}`,
    match: (id) => `${API_HOST}/api/demand/${id}/match`,
    listWithMatch: `${API_HOST}/api/demand/list-with-match`,
  },
  // LLM模块 (DeepSeek)
  llm: {
    parse: `${API_HOST}/api/llm/demand/parse`,
    chat: `${API_HOST}/api/llm/chat`,
    quickAnswer: `${API_HOST}/api/llm/quick-answer`
  },
  // 匹配模块 (Match)
  match: {
    search: `${API_HOST}/api/match/tutors`,
  },
  // 订单模块 (Order)
  order: {
    create: `${API_HOST}/api/order/create`,
    accept: `${API_HOST}/api/order/accept`,
    listParent: `${API_HOST}/api/order/parent/list`,
    listTutor: `${API_HOST}/api/order/tutor/list`,
    pay: `${API_HOST}/api/order/pay`,
    refund: `${API_HOST}/api/order/refund`,
    detail: (id) => `${API_HOST}/api/order/${id}`,
  },
  // 钱包模块 (Wallet)
  wallet: {
    info: `${API_HOST}/api/wallet`,
    transactions: `${API_HOST}/api/wallet/transactions`,
    withdraw: `${API_HOST}/api/wallet/withdraw`,
    withdrawals: `${API_HOST}/api/wallet/withdrawals`,
  },
  // 课时打卡模块 (Teaching)
  teaching: {
    checkIn: `${API_HOST}/api/teaching/check-in`,
    checkOut: (recordId) => `${API_HOST}/api/teaching/check-out/${recordId}`,
    confirm: (recordId) => `${API_HOST}/api/teaching/confirm/${recordId}`,
    dispute: (recordId) => `${API_HOST}/api/teaching/dispute/${recordId}`,
    myRecords: `${API_HOST}/api/teaching/my-records`,
    orderRecords: (orderId) => `${API_HOST}/api/teaching/records/${orderId}`,
    detail: (recordId) => `${API_HOST}/api/teaching/record/${recordId}`,
  },
  // OCR识别模块
  ocr: {
    studentCard: `${API_HOST}/api/ocr/student-card`,
    studentCardBase64: `${API_HOST}/api/ocr/student-card-base64`,
    idCardFront: `${API_HOST}/api/ocr/id-card/front`,
    idCardBack: `${API_HOST}/api/ocr/id-card/back`,
    general: `${API_HOST}/api/ocr/general`
  },
  // 聊天模块
  chat: {
    sessions: `${API_HOST}/api/chat/sessions`,
    history: (targetUserId) => `${API_HOST}/api/chat/history/${targetUserId}`,
    send: `${API_HOST}/api/chat/send`,
    unreadCount: `${API_HOST}/api/chat/unread-count`,
    markRead: (targetUserId) => `${API_HOST}/api/chat/read/${targetUserId}`,
    userInfo: (userId) => `${API_HOST}/api/chat/user-info/${userId}`
  },
  // 学习辅助模块（错题本）
  study: {
    wrongbook: {
      list: `${API_HOST}/api/study/wrongbook`,
      detail: (id) => `${API_HOST}/api/study/wrongbook/${id}`,
      resolve: (id) => `${API_HOST}/api/study/wrongbook/${id}/resolve`
    }
  },
  // 行为追踪模块 (Behavior)
  behavior: {
    view: `${API_HOST}/api/behavior/view`,
    favorite: `${API_HOST}/api/behavior/favorite`,
    chat: `${API_HOST}/api/behavior/chat`,
    search: `${API_HOST}/api/behavior/search`,
  },
  // 地图模块 (Map)
  map: {
    distance: `${API_HOST}/api/map/distance`,
    direction: `${API_HOST}/api/map/direction`,
    geocode: `${API_HOST}/api/map/geocoder`,
    reverseGeocode: `${API_HOST}/api/map/geocoder/reverse`
  }
};

// CommonJS 导出
module.exports = api;