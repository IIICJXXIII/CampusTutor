/**
 * 匹配相关 API
 */
import request from './request';

/**
 * 智能搜索教员 (高级筛选 - POST)
 * 对应后端: MatchController -> @PostMapping("/tutors")
 * * @param {Object} data - 搜索条件 DTO
 * {
 * subject: string,
 * grade: string,
 * minPrice: number,
 * maxPrice: number,
 * teachMode: number, // 1上门 2网课
 * gender: number,    // 1男 2女
 * latitude: number,  // LBS
 * longitude: number, // LBS
 * sortBy: string,    // score, price, distance, rating
 * sortOrder: string, // asc, desc
 * page: number,
 * size: number
 * }
 */
export function searchTutors(data) {
  return request.post('/match/tutors', data);
}

/**
 * 智能推荐/排序获取教员列表 (简单查询 - GET)
 * 对应后端: MatchController -> @GetMapping("/tutors")
 * @param {Object} params - { sortBy, subject, grade, ... }
 */
export function getMatchList(params) {
  return request.get('/match/tutors', { params });
}

/**
 * 智能匹配教员（兼容旧接口）
 * @param {Object} params - { demandId, latitude, longitude, page, size }
 */
export function matchTutors(params) {
  return request.get('/match/tutors', { params });
}

/**
 * 地图模式查找附近需求 (教员端)
 * 对应后端: DemandController -> @GetMapping("/nearby")
 * 注意：后端路径通常在 DemandController 下，这里假设路径为 /demand/nearby
 * 如果后端 MatchController 也有代理，请保持原状。基于代码上下文，修正为 /demand/nearby
 * @param {Object} params - { latitude, longitude, radius, subject }
 */
export function findNearbyDemands(params) {
  // 根据之前的后端代码 DemandController，正确路径应该是 /demand/nearby
  // 如果你的后端专门在 match 下写了接口，请改回 /match/demands/nearby
  return request.get('/demand/nearby', { params });
}

/**
 * 获取教员详情 (公开)
 * 对应后端: TutorController -> @GetMapping("/public/{id}")
 * @param {number} tutorId - 教员ID
 */
export function getTutorDetail(tutorId) {
  return request.get(`/tutor/public/${tutorId}`);
}