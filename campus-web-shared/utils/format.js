import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** YYYY-MM-DD HH:mm:ss */
export const formatDateTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

/** 自定义 format，默认 YYYY-MM-DD */
export const formatDate = (time, fmt = 'YYYY-MM-DD') => {
  if (!time) return ''
  return dayjs(time).format(fmt)
}

/** 自定义 format，默认 HH:mm */
export const formatTime = (time, fmt = 'HH:mm') => {
  if (!time) return ''
  return dayjs(time).format(fmt)
}

/** 相对时间：几分钟前 / 3天前 */
export const relativeFromNow = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

/** 智能时间：今天 HH:mm / 昨天 / MM-DD / YYYY-MM-DD */
export const smartTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const t = dayjs(time)
  if (now.isSame(t, 'day')) return t.format('HH:mm')
  if (now.subtract(1, 'day').isSame(t, 'day')) return '昨天'
  if (now.isSame(t, 'year')) return t.format('MM-DD')
  return t.format('YYYY-MM-DD')
}

/** 聊天时间分隔线：今天 HH:mm / 昨天 HH:mm / MM-DD HH:mm */
export const formatTimeDivider = (time) => {
  if (!time) return ''
  const now = dayjs()
  const t = dayjs(time)
  if (now.isSame(t, 'day')) return t.format('HH:mm')
  if (now.subtract(1, 'day').isSame(t, 'day')) return `昨天 ${t.format('HH:mm')}`
  if (now.isSame(t, 'year')) return t.format('MM-DD HH:mm')
  return t.format('YYYY-MM-DD HH:mm')
}

/** 金额格式化，带千分位 */
export const formatMoney = (value) => {
  if (value == null) return '0'
  return Number(value).toLocaleString()
}
