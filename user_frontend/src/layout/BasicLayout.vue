<template>
  <div class="basic-layout">
    <nav-header class="layout-header" />
    <div class="layout-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import NavHeader from '@/components/NavHeader.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.layout-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.layout-content {
  margin-top: 60px;
  padding: 20px;
  flex: 1;
}
</style> 