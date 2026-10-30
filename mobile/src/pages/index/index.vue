<template>
  <view class="page">
    <view class="hero"><text class="eyebrow">当前钱包</text><text class="hero-title">钱有去处，生活才从容。</text><text class="hero-money">¥ {{ money(dashboard.walletBalance) }}</text><text class="muted">全部账单累计余额，不等同于本月结余</text></view>
    <view class="allocation"><view><text>已分配到计划</text><strong>¥ {{ money(dashboard.plannedRemaining) }}</strong></view><view><text>尚可自由使用</text><strong>¥ {{ money(dashboard.unallocated) }}</strong></view></view>
    <view class="quote"><view><text class="chip">每日一句</text><text>{{ quote.content }}</text></view><text v-if="quote.source" class="quote-source">— {{ quote.source }}</text></view>
    <view class="stats"><view class="stat"><text>¥ {{ dashboard.income || 0 }}</text><text>本月收入</text></view><view class="stat"><text>¥ {{ dashboard.expense || 0 }}</text><text>本月支出</text></view><view class="stat"><text>{{ plans.length }} 个</text><text>资金计划</text></view></view>
    <view class="card"><view class="row"><text class="title">钱的去处</text><text class="link" @click="goPlans">管理计划</text></view><view v-if="!plans.length" class="empty">还没有计划，先为一笔钱设定用途吧。</view><view v-for="plan in plans.slice(0, 3)" :key="plan.id" class="plan"><view class="row"><text>{{ plan.name }}</text><text>余 ¥ {{ plan.remaining }}</text></view><view class="track"><view class="fill" :style="{width: Math.min(100, plan.spent / plan.budget * 100) + '%'}"></view></view><text class="muted">已用 ¥ {{ plan.spent }} / ¥ {{ plan.budget }}</text></view></view>
    <view class="card focus"><view><text class="title">本月消费构成</text><text class="muted">用饼图看清每一笔钱</text></view><view class="go-stat" @click="goStats">查看消费构成 ›</view></view>
    <view class="actions"><view class="primary" @click="go('/pages/record/index')">记一笔</view><view class="assistant" @click="goBills">查看账单</view></view>
    <WalletMascot />
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import WalletMascot from '../../components/WalletMascot.vue'
const dashboard = ref({}); const plans = ref([]); const quote = ref({ content:'让钱去该去的地方，也让自己去想去的地方。', source:'小兜芽' })
const money = value => Number(value || 0).toFixed(2)
async function load() { try { const [data, daily] = await Promise.all([api('/dashboard'), api('/daily-quote')]); dashboard.value = data; plans.value = data.plans || []; quote.value = daily || quote.value } catch { uni.showToast({ title:'无法连接后端', icon:'none' }) } }
function go(url) { uni.navigateTo({ url }) }
function goStats() { uni.switchTab({ url:'/pages/stats/index' }) }
function goPlans() { uni.switchTab({ url:'/pages/plans/index' }) }
function goBills() { uni.switchTab({ url:'/pages/bills/index' }) }
onMounted(load); onShow(load)
</script>
<style scoped>
.hero{background:#CEEBC5;border-radius:30rpx;padding:36rpx;margin-bottom:16rpx;display:flex;flex-direction:column;gap:14rpx}.eyebrow{font-size:22rpx;color:#58744e;font-weight:700}.hero-title{font-size:38rpx;font-weight:700}.hero-money{font-size:64rpx;color:#58744e;font-weight:800}.allocation{display:flex;gap:14rpx;margin-bottom:22rpx}.allocation view{flex:1;background:#fff;border:1rpx solid #dcebd6;border-radius:20rpx;padding:18rpx;display:flex;flex-direction:column;gap:10rpx;color:#6c8066;font-size:22rpx}.allocation strong{color:#354831;font-size:29rpx}.quote{background:#eef8ea;border:1rpx solid #dcebd6;border-radius:20rpx;padding:22rpx;display:flex;flex-direction:column;gap:12rpx;font-size:26rpx;margin-bottom:22rpx}.quote view{display:flex;gap:16rpx;align-items:center}.quote-source{align-self:flex-end;color:#6c8066;font-size:21rpx}.stats{display:flex;gap:14rpx;margin-bottom:22rpx}.stat{flex:1;background:#fff;border:1rpx solid #dcebd6;border-radius:20rpx;padding:20rpx;display:flex;flex-direction:column;gap:8rpx}.stat text:first-child{font-size:30rpx;font-weight:700}.stat text:last-child{font-size:21rpx;color:#6c8066}.link{font-size:24rpx;color:#58744e}.plan{padding:22rpx 0;border-bottom:1rpx solid #edf2ea;display:flex;flex-direction:column;gap:12rpx}.track{height:12rpx;background:#edf3e9;border-radius:99rpx;overflow:hidden}.fill{height:100%;background:#87aa78;border-radius:99rpx}.actions{display:flex;gap:16rpx}.actions view{flex:1}.assistant{border:1rpx solid #bcd3b4;color:#58744e;background:#fff;border-radius:18rpx;padding:18rpx 24rpx;text-align:center}.focus{display:flex;align-items:center;justify-content:space-between}.focus view:first-child{display:flex;flex-direction:column;gap:10rpx}.go-stat{color:#58744e;font-size:25rpx}.empty{padding:26rpx 0 4rpx;color:#6c8066;font-size:26rpx}
</style>
