// 基础环境配置
const ENV = 'dev'; // dev | prod

const BASE_URL = {
  dev: 'http://localhost:8080', // 你的本地后端地址，真机调试需换成局域网IP或内网穿透地址
  prod: 'https://api.yourdomain.com'
};

const API_HOST = BASE_URL[ENV];

export default {
  host: API_HOST,
  // 认证模块
  auth: {
    login: `${API_HOST}/api/auth/login`,
    register: `${API_HOST}/api/auth/register`,
    sendCode: `${API_HOST}/api/auth/send-code`, // Mock验证码
  },
  // 用户模块
  user: {
    info: `${API_HOST}/api/user`, // PUT 更新, GET 获取
    byId: (id) => `${API_HOST}/api/user/${id}`,
  },
  // 文件上传
  file: {
    upload: `${API_HOST}/api/file/upload`,
  },
  // 家教模块 (Tutor)
  tutor: {
    profile: `${API_HOST}/api/tutor/profile`, // 获取当前教员档案
    certification: `${API_HOST}/api/tutor/certification`, // 提交认证
    schedule: `${API_HOST}/api/tutor/schedule`, // 时间配置
    detail: (id) => `${API_HOST}/api/tutor/${id}`, // 公开详情
  },
  // 家长模块 (Parent)
  parent: {
    student: `${API_HOST}/api/parent/student`, // 增删改查
    myStudents: `${API_HOST}/api/parent/students`, // 获取列表
  },
  // 需求模块 (Demand)
  demand: {
    publish: `${API_HOST}/api/demand/publish`,
    list: `${API_HOST}/api/demand/list`,
    my: `${API_HOST}/api/demand/my`,
    nearby: `${API_HOST}/api/demand/nearby`, // LBS
    detail: (id) => `${API_HOST}/api/demand/${id}`,
  },
  // 匹配模块 (Match)
  match: {
    search: `${API_HOST}/api/match/tutors`, // POST 复杂搜索
  },
  // 订单模块 (Order)
  order: {
    create: `${API_HOST}/api/order/create`,
    listParent: `${API_HOST}/api/order/parent/list`,
    listTutor: `${API_HOST}/api/order/tutor/list`,
    pay: `${API_HOST}/api/order/pay`,
    detail: (id) => `${API_HOST}/api/order/${id}`,
  }
};