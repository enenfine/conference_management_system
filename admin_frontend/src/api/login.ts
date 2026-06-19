import request from '@/utils/request'

interface RegisterParams {
  userAccount: string
  userPassword: string
  checkPassword: string
}

interface LoginParams {
  userAccount: string
  userPassword: string
}

// 登录接口返回的用户信息类型
interface UserInfo {
  id: string
  userName: string | null
  userAvatar: string | null
  userProfile: string | null
  userRole: 'user' | 'admin'
  createTime: string
  updateTime: string
}

interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

/**
 * 用户注册
 * @param params 注册参数
 */
export const register = (params: RegisterParams) => {
  return request.post<BaseResponse<number>>('/user/register', params)
}

/**
 * 用户登录
 * @param params 登录参数
 */
export const login = (params: LoginParams) => {
  return request.post<BaseResponse<UserInfo>>('/user/login', params)
}

/**
 * 用户退出登录
 */
export const logout = () => {
  return request.post<BaseResponse<void>>('/user/logout')
} 