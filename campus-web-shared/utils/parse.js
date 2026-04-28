/**
 * 安全解析 JSON，失败时返回 fallback
 */
export const safeParseJson = (str, fallback = null) => {
  if (!str) return fallback
  try {
    return JSON.parse(str)
  } catch {
    return fallback
  }
}
