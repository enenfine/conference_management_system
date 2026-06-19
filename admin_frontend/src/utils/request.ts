import axios from 'axios'
import type {AxiosInstance, AxiosRequestConfig, AxiosResponse} from 'axios'
import {ElMessage} from 'element-plus'
import {useUserStore} from "../stores/modules/user.ts";
import {useRouter} from "vue-router";


// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const {code, message, data} = response.data

        // 如果请求成功
        if (code === 0) {
            return response.data
        }
        // 显示错误信息
        // ElMessage.error(message || '请求失败')
        if (message == "未登录") {
            localStorage.removeItem('user');
            location.reload();
        }
        return Promise.reject(new Error(message || '请求失败'))
    },
    (error) => {
        // 处理 401 未授权的情况
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            userStore.clearUserInfo()
            window.location.href = '/login'
            return Promise.reject(new Error('请重新登录'))
        }

        ElMessage.error(error.response?.data?.message || '请求失败')
        return Promise.reject(error)
    }
)

export default request 