import router from './index'
import {useUserStore} from "../stores/modules/user.ts";
import {ElMessage} from 'element-plus'
// 白名单路径（不需要登录就可以访问的路径）
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `会议管理系统后台-${to.meta.title || '会议管理系统后台'}`

  const userStore = useUserStore()

  // 如果是访问登录页
  if (to.path === '/login') {
    if (userStore.isLogin) {
      console.log('已登录状态访问登录页，重定向到首页')
      // 已登录状态访问登录页，重定向到首页
      next({path: '/'})
      ElMessage.info('您已登录，已为您跳转到首页')
    } else {
      console.log('未登录状态访问登录页，直接放行')
      // 未登录状态访问登录页，直接放行
      next()
    }
    return
  }

  // 尝试从 localStorage 恢复用户状态
  if (!userStore.isLogin) {
    console.log('尝试从 localStorage 恢复用户状态')
    // userStore.loadUserInfo()
    console.log('恢复后的状态:', {
      isLogin: userStore.isLogin,
      userInfo: userStore.userInfo
    })
  }

  // 再次检查登录状态（可能已经从 localStorage 恢复）
  if (!userStore.isLogin) {
    console.log('未登录状态访问其他页面，重定向到登录页')
    console.log(userStore.isLogin)
    // 未登录状态访问其他页面，重定向到登录页
    next({
      path: '/login',
      query: {redirect: to.fullPath}
    })
    ElMessage.warning('请先登录')
    return
  }

  // 已登录状态，进行权限检查
  const userRole = userStore.getUserRole()
  console.log('权限检查:', {
    userRole,
    requiredRoles: to.meta.roles
  })

  // 检查是否需要特定权限
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    console.log('没有访问权限，跳转到 403 页面')
    // 没有权限，跳转到 403 页面
    next({path: '/403'})
    ElMessage.error('您没有访问该页面的权限')
    return
  }

  console.log('权限检查通过，允许访问')
  // 有权限，放行
  next()
})

// 路由加载后钩子
router.afterEach(() => {
  // 路由切换后的操作，比如关闭 loading 等
}) 