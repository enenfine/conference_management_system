import axios from 'axios'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/stores/modules/user.ts'

const redirectToLogin = () => {
    const userStore = useUserStore()
    userStore.clearUserInfo()
    if (window.location.pathname !== '/login') {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        window.location.href = `/login?redirect=${redirect}`
    }
}

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    },
    // 后端登录态保存在 Session 中，必须携带 Cookie/JSESSIONID
    withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const {code, message} = response.data || {}

        if (code === 0) {
            return response.data
        }

        if (code === 40100 || message === '未登录') {
            redirectToLogin()
            return Promise.reject(new Error('未登录，请重新登录'))
        }

        return Promise.reject(new Error(message || '请求失败'))
    },
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            redirectToLogin()
            return Promise.reject(new Error('请重新登录'))
        }

        ElMessage.error(error.response?.data?.message || error.message || '请求失败')
        return Promise.reject(error)
    }
)

export default request
