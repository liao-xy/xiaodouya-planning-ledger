<template><web-view :src="src"></web-view></template>
<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
const src = ref('https://beian.miit.gov.cn')
onLoad((options)=>{ const candidate = decodeURIComponent(options?.src || ''); if (candidate.startsWith('https://beian.miit.gov.cn')) src.value = candidate })
</script>
