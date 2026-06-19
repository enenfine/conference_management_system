import router from './index'
import {useUserStore} from '@/stores/modules/user.ts'
import {ElMessage} from 'element-plus'

const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
    document.title = `会议管理系统系统用户端-${to.meta.title || '会议管理系统系统用户端'}`

    const userStore = useUserStore()

    // 先从 localStorage 恢复前端展示状态
    if (!userStore.isLogin) {
        userStore.loadUserInfo()
    }

    if (whiteList.includes(to.path)) {
        // 已登录时访问登录页，也要向后端确认 Session 还在
        if (userStore.isLogin) {
            const ok = await userStore.fetchLoginUser()
            if (ok) {
                next({path: '/'})
                return
            }
        }
        next()
        return
    }

    if (!userStore.isLogin) {
        next({
            path: '/login',
            query: {redirect: to.fullPath}
        })
        ElMessage.warning('请先登录')
        return
    }

    // 前端显示已登录不等于后端 Session 还有效。
    // 后端重启后 Session 会丢失，所以进入业务页前必须确认一次。
    const sessionOk = await userStore.fetchLoginUser()
    if (!sessionOk) {
        next({
            path: '/login',
            query: {redirect: to.fullPath}
        })
        ElMessage.warning('登录状态已过期，请重新登录')
        return
    }

    const userRole = userStore.getUserRole()
    if (to.meta.roles && !to.meta.roles.includes(userRole)) {
        next({path: '/403'})
        ElMessage.error('您没有访问该页面的权限')
        return
    }

    next()
})
