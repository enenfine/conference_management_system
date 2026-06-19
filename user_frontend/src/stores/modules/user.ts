import {defineStore} from 'pinia'
import {login, logout, getLoginUser} from '@/api/login'
import type {LoginParams, UserInfo} from '@/api/login'
import {ElMessage} from 'element-plus'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: null as UserInfo | null,
        isLogin: false,
    }),
    actions: {
        async userLogin(params: LoginParams) {
            try {
                const res: any = await login(params)
                if (res.code !== 0) {
                    throw new Error(res.message || '登录失败')
                }
                if (!res.data) {
                    throw new Error('用户数据为空')
                }
                this.setUserInfo(res.data)
                ElMessage.success('登录成功')
            } catch (error: any) {
                ElMessage.error(error.message || '登录失败')
                throw error
            }
        },

        async fetchLoginUser() {
            try {
                const res: any = await getLoginUser()
                if (res.code === 0 && res.data) {
                    this.setUserInfo(res.data)
                    return true
                }
                this.clearUserInfo()
                return false
            } catch {
                this.clearUserInfo()
                return false
            }
        },

        async userLogout() {
            try {
                await logout()
                this.clearUserInfo()
                ElMessage.success('登出成功')
                return true
            } catch (error: any) {
                this.clearUserInfo()
                ElMessage.error(error.message || '登出失败')
                return true
            }
        },

        setUserInfo(info: UserInfo) {
            if (!info || !info.id) {
                throw new Error('无效的用户信息')
            }
            this.userInfo = info
            this.isLogin = true
            localStorage.setItem('userInfo', JSON.stringify(info))
            localStorage.setItem('isLogin', 'true')
        },

        clearUserInfo() {
            this.userInfo = null
            this.isLogin = false
            localStorage.removeItem('userInfo')
            localStorage.removeItem('isLogin')
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            localStorage.removeItem('userStore')
        },

        getUserRole() {
            return this.userInfo?.userRole || ''
        },

        loadUserInfo() {
            const savedInfo = localStorage.getItem('userInfo')
            const savedLogin = localStorage.getItem('isLogin')

            if (savedInfo && savedLogin === 'true') {
                try {
                    this.userInfo = JSON.parse(savedInfo)
                    this.isLogin = true
                } catch {
                    this.clearUserInfo()
                }
            }
        },
    },

    persist: {
        key: 'userStore',
        storage: localStorage,
        paths: ['userInfo', 'isLogin'],
    },
})
