<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb>
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="ip_addr">
            <el-tooltip
              effect="dark"
              placement="bottom"
              :content="`IP: ${ipInfo.publicIp} | 地址: ${ipInfo.addr}`"
            >
              <el-tag class="ip-tag" type="info" effect="plain">
                <span class="ip-label">IP：</span>
                <span class="ip-text">{{ ipInfo.pro }}</span>
              </el-tag>
            </el-tooltip>
          </span>
          <el-dropdown trigger="hover">
            <div class="user-info">
              <el-avatar :size="32" :src="avatarUrl" :fallback="defaultAvatar"
                @error="() => avatarUrl = defaultAvatar" />
              <span class="username">{{ userName }}</span>
              <el-icon>
                <CaretBottom />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon>
                    <SwitchButton />
                  </el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, CaretBottom, User, Setting, SwitchButton, Guide, Location, InfoFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import Sidebar from '@/components/Sidebar.vue'
import { useUserStore } from '@/stores/modules/user'
import defaultAvatar from '@/assets/avatar.jpg'
import { getUserIp } from '@/api/user.ts'
import axios from "axios";

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const userStore = useUserStore()

// 获取用户信息
const userInfo = computed(() => userStore.userInfo)

// 获取头像URL
const getAvatarUrl = (avatar: string) => {
  if (!avatar) return defaultAvatar
  return `${import.meta.env.VITE_BASE_URL}${avatar}`
}

// 计算头像URL
const avatarUrl = computed(() => {
  const avatar = userInfo.value?.userAvatar
  if (!avatar) return defaultAvatar
  try {
    return getAvatarUrl(avatar)
  } catch (error) {
    console.error('头像URL处理错误:', error)
    return defaultAvatar
  }
})

// 用户名
const userName = computed(() => userInfo.value?.userName)

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.userLogout().then(() => {
      router.push('/login')
    })
  })
}
const ipInfo = ref({
  city: '',
  pro: '',
  publicIp: '',
  localIpAddress: '',
  addr: ''
})
//获取用户ip信息
const getIpInfo = async () => {
  const res = await getUserIp()
  ipInfo.value.localIpAddress = res.data.localIpAddress
  ipInfo.value.publicIp = res.data.publicIpAddress
  axios.get(`/city?ip=${res.data.publicIpAddress}&json=true`).then(res => {
    ipInfo.value.pro = res.data.pro
    ipInfo.value.city = res.data.city
    ipInfo.value.addr = res.data.addr
  })
  console.log(ipInfo.value)
}

onMounted(() => {
  getIpInfo()
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;

  .aside {
    transition: width 0.3s;
    background-color: #001529;
  }

  .el-header {
    background-color: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        color: #666;
        transition: color 0.3s;

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .el-breadcrumb {
        line-height: 60px;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;

      .ip_addr {
        .ip-tag {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 6px 12px;
          height: 32px;
          border-color: #e4e7ed;
          background-color: #f5f7fa;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            background-color: #ecf5ff;
            border-color: #d9ecff;
          }

          .ip-label {
            color: #909399;
            font-size: 14px;
          }

          .ip-text {
            color: #606266;
            font-size: 14px;
            font-weight: 500;
          }
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        transition: all 0.3s;

        &:hover {
          background-color: #f6f6f6;
        }

        .username {
          font-size: 14px;
          color: #333;
        }

        .el-icon {
          font-size: 12px;
          color: #666;
        }
      }
    }
  }

  .el-main {
    background-color: #f6f9fc;
    padding: 20px;
  }
}
</style>