import {defineStore} from 'pinia'
import {ref} from 'vue'
import {login, logout} from '@/api/login'
import type {LoginParams} from '@/api/login'
import {ElMessage} from 'element-plus'


interface UserInfo {
    id: string;
    userName: string | null;
    userAvatar: string | null;
    userProfile: string | null;
    userRole: 'user' | 'admin';
    createTime: string;
    updateTime: string;
}

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: ref<UserInfo | null>(null),
        isLogin: ref(false),
    }),
    actions: {
        async userLogin(params: LoginParams) {
            try {
                const res = await login(params)
                if (res.code !== 200) {
                    throw new Error(res.message || '登录失败')
                }
                if (!res.data) {
                    throw new Error('用户数据为空')
                }
                this.setUserInfo(res.data)
                this.isLogin = true
                ElMessage.success('登录成功')
            } catch (error) {
                ElMessage.error(error.message || '登录失败')
                throw error
            }
        },
        async userLogout() {
            try {
                await logout()
                this.clearUserInfo()
                ElMessage.success('登出成功')
                return true
            } catch (error) {
                ElMessage.error(error.message || '登出失败')
                throw error
            }
        },
        setUserInfo(info: UserInfo) {
            if (!info.id || !info.createTime) {
                throw new Error('无效的用户信息')
            }
            this.userInfo = info
            this.isLogin = true
        },
        clearUserInfo() {
            this.userInfo = null
            this.isLogin = false
        },
        getUserRole() {
            return this.userInfo?.userRole || ''
        },
        loadUserInfo() {
            console.log('开始从 localStorage 恢复用户信息');
            const savedInfo = localStorage.getItem('userInfo');
            const savedLogin = localStorage.getItem('isLogin');
            console.log('localStorage 中的数据:', {savedInfo, savedLogin});
            if (savedInfo && savedLogin === 'true') {
                this.userInfo = JSON.parse(savedInfo);
                this.isLogin = true;
                console.log('恢复用户信息后的状态:', {
                    userInfo: this.userInfo,
                    isLogin: this.isLogin,
                });
            } else {
                console.log('localStorage 中没有找到有效的用户信息');
            }
        },
    },
    // 持久化配置
    persist: {
        // key: 'admin_user',
        enabled: true,
        strategies: [
            {
                key: 'userStore',
                storage: localStorage,
                paths: ['userInfo', 'isLogin'],
            },
        ],
    },
})
