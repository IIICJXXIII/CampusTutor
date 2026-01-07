// utils/dateUtil.js

const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()
  return `${[year, month, day].map(formatNumber).join('-')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

/**
 * 检查两个时间段是否冲突
 * 用于排课检测 [cite: 920]
 * @param {string} start1 时间段1开始
 * @param {string} end1 时间段1结束
 * @param {string} start2 时间段2开始
 * @param {string} end2 时间段2结束
 */
const isConflict = (start1, end1, start2, end2) => {
  const s1 = new Date(start1).getTime();
  const e1 = new Date(end1).getTime();
  const s2 = new Date(start2).getTime();
  const e2 = new Date(end2).getTime();
  // 只要时间段有交集即为冲突
  return !(e1 <= s2 || s1 >= e2);
}

module.exports = {
  formatTime,
  isConflict
}