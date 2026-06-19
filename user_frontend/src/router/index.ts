import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layout/BasicLayout.vue'

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
      component: BasicLayout,
      redirect: '/meetings',
      children: [
        {
          path: 'meetings',
          name: 'Meetings',
          component: () => import('@/views/meeting/index.vue'),
          meta: { title: '会议管理' }
        },
        {
          path: '/meeting/form',
          name: 'MeetingForm',
          component: () => import('@/views/meeting/form.vue'),
          meta: { title: '会议预定' }
        },
        {
          path: 'rooms',
          name: 'Rooms',
          component: () => import('@/views/rooms/index.vue'),
          meta: { title: '会议室预定' }
        },
        {
          path: '/documents',
          name: 'Documents',
          component: () => import('@/views/documents/index.vue'),
          meta: { title: '会议文件上传与下载', requiresAuth: true }
        },
        {
          path: '/meeting/detail/:id',
          name: 'MeetingDetail',
          component: () => import('@/views/meeting/detail.vue'),
          meta: { title: '会议详情', hideInMenu: true }
        },
        {
          path: '/room/detail/:id',
          name: 'RoomDetail',
          component: () => import('@/views/rooms/detail.vue'),
          meta: { title: '会议室详情', hideInMenu: true }
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
