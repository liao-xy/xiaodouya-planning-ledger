<template>
  <view class="page">
    <view class="intro"><text class="eyebrow">每一笔都可回看、修正</text><text class="title">我的账单</text></view>
    <view v-if="!rows.length" class="card empty">还没有账单，先记下一笔吧。</view>
    <view v-for="item in rows" :key="item.id" class="bill card" @click="edit(item.id)">
      <view class="bill-main"><view :class="['type-dot', item.type === 'INCOME' ? 'income' : 'expense']">{{ item.type === 'INCOME' ? '入' : '支' }}</view><view><text class="bill-category">{{ item.category }}</text><text class="muted">{{ item.occurredOn }}{{ item.note ? ' · ' + item.note : '' }}</text></view></view>
      <view class="bill-right"><text :class="item.type === 'INCOME' ? 'income-text' : 'expense-text'">{{ item.type === 'INCOME' ? '+' : '-' }} ¥ {{ Number(item.amount).toFixed(2) }}</text><text class="delete" @click.stop="remove(item)">删除</text></view>
    </view>
    <view class="primary add" @click="goRecord">记一笔</view>
    <WalletMascot />
  </view>
</template>
<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import WalletMascot from '../../components/WalletMascot.vue'
const rows = ref([])
async function load(){ try { rows.value = await api('/transactions') } catch(error) { uni.showToast({ title:error?.message || '账单加载失败', icon:'none' }) } }
function edit(id){ uni.navigateTo({ url:`/pages/record/index?id=${id}` }) }
function goRecord(){ uni.navigateTo({ url:'/pages/record/index' }) }
function remove(item){ uni.showModal({ title:'删除这笔账单？', content:'删除后会释放对应计划额度，且无法恢复。', confirmColor:'#58744e', success:async result => { if(!result.confirm) return; try { await api(`/transactions/${item.id}`,{method:'DELETE'}); await load(); uni.showToast({title:'已删除'}) } catch(error) { uni.showToast({title:error?.message || '删除失败',icon:'none'}) } } }) }
onShow(load)
</script>
<style scoped>
.intro{display:flex;flex-direction:column;gap:10rpx;margin:0 0 22rpx}.eyebrow{font-size:22rpx;color:#58744e;font-weight:700}.bill{display:flex;justify-content:space-between;align-items:center;margin-bottom:16rpx;padding:24rpx}.bill-main{display:flex;align-items:center;gap:18rpx;min-width:0}.type-dot{width:58rpx;height:58rpx;line-height:58rpx;border-radius:50%;text-align:center;font-size:23rpx;font-weight:700}.type-dot.expense{background:#fcefe9;color:#b56f55}.type-dot.income{background:#e8f5e4;color:#58744e}.bill-main view:last-child{display:flex;flex-direction:column;gap:8rpx;min-width:0}.bill-category{font-size:29rpx;font-weight:700}.bill-right{display:flex;align-items:flex-end;flex-direction:column;gap:10rpx}.income-text{color:#4f8751;font-weight:700}.expense-text{color:#b56f55;font-weight:700}.delete{font-size:22rpx;color:#9a786c}.empty{text-align:center;color:#6c8066;padding:52rpx 20rpx}.add{position:fixed;left:32rpx;right:32rpx;bottom:138rpx;text-align:center}
</style>
