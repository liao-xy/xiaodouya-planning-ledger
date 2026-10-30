<template><view class="mascot" @click="openAssistant"><image class="mascot-image" src="./wallet-mascot.png" mode="aspectFit" /></view></template>
<script setup>
function openAssistant(){ uni.navigateTo({ url:'/pages/assistant/index' }) }
</script>
<style scoped>
.mascot{position:fixed;right:20rpx;bottom:38rpx;width:150rpx;height:150rpx;background:transparent;border:0;box-shadow:none;z-index:20}.mascot-image{width:150rpx;height:150rpx;display:block}
</style>
