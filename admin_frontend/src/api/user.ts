import request from '@/utils/request'
import type {PageParams} from '@/types/common'

export interface UserListParams extends PageParams {
    userName?: string
    userProfile?: string
    userRole?: string
    sortField?: string
    sortOrder?: string
}

// 获取用户列表
export function getUserList(params: UserListParams) {
    return request({
        url: '/user/list/page',
        method: 'post',
        data: params
    })
}

// 更新用户信息
export function updateUser(data: {
    id: number
    userAvatar: string
    userName: string
    userProfile: string
    userRole: string
}) {
    return request({
        url: '/user/update',
        method: 'post',
        data
    })
}

// 上传用户头像
export function uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/user/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 添加用户
export function addUser(data: {
    userAccount: string
    userAvatar: string
    userName: string
    userRole: string
}) {
    return request({
        url: '/user/add',
        method: 'post',
        data
    })
}

// 删除用户
export function deleteUser(id: number) {
    return request({
        url: '/user/delete',
        method: 'post',
        data: {id}
    })
}

// 获取用户详情
export function getUserDetail(id: number) {
    return request({
        url: '/user/get',
        method: 'get',
        params: {id}
    })
}

// 重置用户密码
export function resetUserPassword(data: {
    checkPassword: string
    userName: string
    userNewPassword: string
    userOldPassword: string
}) {
    return request({
        url: '/user/update/myPassword',
        method: 'post',
        data
    })
}

//获取用户ip地址
export function getUserIp() {
    return request({
        url: '/user/get/ip',
        method: 'get'
    })
}