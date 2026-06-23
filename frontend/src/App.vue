<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  const savedTheme = localStorage.getItem('wms-theme') || 'default'
  document.documentElement.setAttribute('data-theme', savedTheme)
})
</script>

