// 从环境变量中获取基础URL
export const baseUrl = import.meta.env.VITE_BASE_URL || '/api'

// 其他配置项可以在这里添加
export const config = {
  baseUrl,
  uploadUrl: `${baseUrl}/upload`,
  // ... 其他配置
}

export default config 