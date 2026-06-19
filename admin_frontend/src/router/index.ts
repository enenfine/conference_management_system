import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/users',
      children: [
        {
          path: 'users',
          component: () => import('@/views/users/index.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'meetings/list',
          component: () => import('@/views/meetings/list/index.vue'),
          meta: { title: '会议列表' }
        },
        {
          path: 'meetings/type',
          component: () => import('@/views/meetings/type/index.vue'),
          meta: { title: '会议类型管理' }
        },
        {
          path: 'rooms',
          component: () => import('@/views/rooms/index.vue'),
          meta: { title: '会议室管理' }
        },
        {
          path: 'materials/list',
          component: () => import('@/views/materials/list/index.vue'),
          meta: { title: '会议文档管理' }
        }
      ]
    },
    {
      path: '/403',
      name: '403',
      component: () => import('@/views/error/403.vue'),
      meta: { title: '无权限' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: () => import('@/views/error/404.vue'),
      meta: { title: '页面不存在' }
    }
  ]
})

export default router
