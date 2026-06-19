<!-- 导航头部组件：只保留用户端需要的功能入口 -->
<template>
  <div class="nav-header">
    <div class="logo">
      <el-icon :size="24"><Monitor /></el-icon>
      <span>会议室管理系统</span>
    </div>

    <div class="nav-menu">
      <el-menu mode="horizontal" :router="true" :default-active="activeMenu" :ellipsis="false" class="menu-list">
        <el-menu-item index="/meetings">
          <el-icon><Calendar /></el-icon>
          <span>会议管理</span>
        </el-menu-item>
        <el-menu-item index="/rooms">
          <el-icon><OfficeBuilding /></el-icon>
          <span>会议室预定</span>
        </el-menu-item>
        <el-menu-item index="/documents">
          <el-icon><Document /></el-icon>
          <span>会议文件上传与下载</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="user-info">
      <el-dropdown trigger="hover" @command="handleCommand">
        <div class="avatar-container">
          <el-avatar
            :size="40"
            shape="circle"
            class="text-avatar"
            :style="avatarStyle"
          >
            {{ avatarText }}
          </el-avatar>
          <span class="username">{{ userName }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/modules/user.ts'
import { ElMessageBox } from 'element-plus'
import { Monitor, Calendar, OfficeBuilding, SwitchButton, CaretBottom, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userInfo?.userName || userStore.userInfo?.userAccount || '用户')
const avatarText = computed(() => (userName.value || '用户').substring(0, 1))

const avatarStyle = computed(() => {
  const colors = ['#165DFF', '#00B42A', '#722ED1', '#F77234', '#F5319D', '#3491FA']
  const name = userName.value || '用户'
  const index = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0) % colors.length
  return {
    backgroundColor: colors[index],
    color: '#fff'
  }
})

onMounted(() => {
  userStore.loadUserInfo()
})

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await handleLogout()
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const success = await userStore.userLogout()
    if (success) {
      await router.push('/login')
    }
  } catch {
    // 用户取消退出
  }
}
</script>

<style scoped>
.nav-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  width: 200px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 600px;
}

.menu-list {
  border-bottom: none;
}

.user-info {
  width: 200px;
  display: flex;
  justify-content: flex-end;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.text-avatar {
  font-size: 16px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #333;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
