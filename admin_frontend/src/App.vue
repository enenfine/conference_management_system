<script setup lang="ts">

</script>

<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<style>
@import './styles/element/table.scss'; 
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.app-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
