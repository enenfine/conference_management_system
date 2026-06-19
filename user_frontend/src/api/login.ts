import request from '@/utils/request'

export interface RegisterParams {
  userAccount: string
  userPassword: string
  checkPassword: string
}

export interface LoginParams {
  userAccount: string
  userPassword: string
}

// 登录接口返回的用户信息类型
export interface UserInfo {
  id: string
  userAccount?: string
  userName: string | null
  userAvatar: string | null
  userProfile: string | null
  userRole: 'user' | 'admin' | 'ban'
  createTime: string
  updateTime: string
}

export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

/**
 * 用户注册
 */
export const register = (params: RegisterParams) => {
  return request.post<BaseResponse<number>>('/user/register', params)
}

/**
 * 用户登录。后端会把登录态写入 Session，前端必须携带 Cookie。
 */
export const login = (params: LoginParams) => {
  return request.post<BaseResponse<UserInfo>>('/user/login', params)
}

/**
 * 获取当前登录用户，用来校验后端 Session 是否还有效。
 */
export const getLoginUser = () => {
  return request.get<BaseResponse<UserInfo>>('/user/get/login')
}

/**
 * 用户退出登录
 */
export const logout = () => {
  return request.post<BaseResponse<void>>('/user/logout')
}
