<template>
  <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
    <div class="logo">
      <el-icon class="logo-icon"><Monitor /></el-icon>
      <span v-show="!isCollapse">会议管理系统后台</span>
    </div>
    <el-menu
      :collapse="isCollapse"
      :default-active="activeMenu"
      router
      class="el-menu-vertical"
      background-color="#1a1f37"
      text-color="rgba(255,255,255,0.7)"
      active-text-color="#fff"
    >
      <el-menu-item index="/users">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>

      <el-sub-menu index="/meetings">
        <template #title>
          <el-icon><Calendar /></el-icon>
          <span>会议管理</span>
        </template>
        <el-menu-item index="/meetings/list">会议列表</el-menu-item>
        <el-menu-item index="/meetings/type">会议类型管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/rooms">
        <el-icon><OfficeBuilding /></el-icon>
        <template #title>会议室管理</template>
      </el-menu-item>

      <el-menu-item index="/materials/list">
        <el-icon><Document /></el-icon>
        <template #title>会议文档管理</template>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { User, OfficeBuilding, Document, Monitor, Calendar } from '@element-plus/icons-vue'

defineProps<{
  isCollapse: boolean
}>()

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/meetings')) {
    return route.path
  }
  return route.path
})
</script>

<style scoped lang="scss">
.aside {
  background-color: #1a1f37;
  transition: all 0.3s;
  overflow: hidden;
  height: 100%;
  width: 100%;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  background-color: #141829;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #3699ff;
}

.el-menu-vertical {
  border-right: none;
  height: calc(100vh - 60px);
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 240px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  justify-content: flex-start !important;
  padding-left: 20px !important;

  &:hover {
    background-color: #3e77dc !important;
    color: #fff !important;
  }

  .el-icon {
    color: rgba(255, 255, 255, 0.7);
    margin-right: 12px;
    font-size: 18px;
  }
}

:deep(.el-menu-item.is-active),
:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background-color: #3e77dc !important;
  color: #fff !important;

  .el-icon {
    color: #fff;
  }
}

:deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  background-color: #141829 !important;
}

:deep(.el-sub-menu .el-menu-item.is-active),
:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #3e77dc !important;
  color: #fff !important;
}
</style>
