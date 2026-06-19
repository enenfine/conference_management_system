<script setup lang="ts">
import {ref, onMounted, watch, provide, h} from 'vue'
import {useUserStore} from '@/stores/modules/user'
import {ElNotification} from 'element-plus'
import {useRouter} from 'vue-router'
import dayjs from 'dayjs'
import Loading from '@/components/Loading.vue'

const router = useRouter()
const userStore = useUserStore()
const eventSource = ref<EventSource | null>(null)
const isSSEConnected = ref(false)

provide('isSSEConnected', isSSEConnected)

const closeSSE = () => {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
  isSSEConnected.value = false
}

const connectSSE = () => {
  closeSSE()

  if (!userStore.isLogin) {
    console.log('用户未登录，不建立 SSE 连接')
    return
  }

  const baseUrl = import.meta.env.VITE_API_URL || ''
  eventSource.value = new EventSource(`${baseUrl}/meeting/reminder/subscribe`, {withCredentials: true})

  eventSource.value.onopen = () => {
    console.log('SSE连接已建立')
    isSSEConnected.value = true
  }

  eventSource.value.onerror = () => {
    closeSSE()
    setTimeout(() => {
      if (userStore.isLogin) {
        connectSSE()
      }
    }, 5000)
  }

  eventSource.value.addEventListener('heartbeat', (event) => {
    console.debug('收到心跳:', event.data)
  })

  eventSource.value.addEventListener('reminder', (event) => {
    const reminder = JSON.parse(event.data)
    showNotification(reminder)
  })
}

const showNotification = (reminder: any) => {
  const notificationContent = h('div', {
    class: 'notification-content',
    innerHTML: `
      <div style="margin-top: 8px; font-size: 13px; color: #666;">
        <div>会议时间：${dayjs(reminder.meetingStartTime).format('MM-DD HH:mm')} - ${dayjs(reminder.meetingEndTime).format('HH:mm')}</div>
        <div>提醒时间：${dayjs(reminder.reminderTime).format('MM-DD HH:mm')}</div>
        <div>发送人：${reminder.sendUserName || '系统'}</div>
        <div>内容：${reminder.content}</div>
      </div>
    `
  })

  ElNotification({
    title: reminder.meetingTitle,
    message: notificationContent,
    duration: 15000,
    icon: Loading,
    position: 'top-right',
    showClose: true,
    offset: 16,
    onClick: () => {
      router.push(`/meeting/detail/${reminder.meetingId}`)
    },
    customClass: 'meeting-notification',
  })
}

watch(() => userStore.isLogin, (isLogin) => {
  if (isLogin) {
    connectSSE()
  } else {
    closeSSE()
  }
}, {immediate: true})

window.addEventListener('beforeunload', () => {
  const baseUrl = import.meta.env.VITE_API_URL || ''
  closeSSE()
  fetch(`${baseUrl}/meeting/reminder/unsubscribe`, {
    method: 'POST',
    credentials: 'include'
  })
})

onMounted(() => {
  userStore.loadUserInfo()
  if (userStore.isLogin) {
    connectSSE()
  }
})
</script>

<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  width: 100%;
  overflow-x: hidden;
}

.app-container {
  height: 100vh;
  width: 100vw;
  overflow-x: hidden;
}

.meeting-notification {
  width: 350px;
  padding: 16px;
}

.meeting-notification .el-notification__title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.meeting-notification .el-notification__content {
  margin: 0;
}

.meeting-notification:hover {
  cursor: pointer;
  background: #f5f7fa;
}
</style>
