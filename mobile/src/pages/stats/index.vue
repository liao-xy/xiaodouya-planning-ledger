<template>
  <view class="page">
    <view class="intro"><text class="eyebrow">本月支出分析</text><text class="title">消费构成</text><text class="muted">总支出 ¥ {{ Number(dashboard.expense || 0).toFixed(2) }}</text></view>
    <view class="card" v-if="items.length"><view class="chart-row"><view class="donut" :style="pieStyle"><view class="donut-hole"><text>本月</text><text>¥{{ Number(dashboard.expense).toFixed(0) }}</text></view></view><view class="chart-note"><text class="title">消费饼图</text><text class="muted">点击下方分类，核对每笔记录。</text></view></view><view class="legend"><view v-for="item in items" :key="item.category" class="legend-row"><view class="legend-left"><text class="dot" :style="{background:item.color}"></text><text>{{ item.category }}</text></view><text>¥ {{ item.amount }} · {{ item.percent }}%</text></view></view></view>
    <view v-else class="card empty"><text class="title">还没有支出记录</text><text class="muted">记下第一笔支出后，这里会生成你的消费饼图。</text></view>
    <view class="card"><view class="row"><text class="title">每日花销节奏</text><text class="muted">最近 7 天</text></view><view v-if="daily.length" class="bars"><view v-for="item in daily" :key="item.date" class="bar-col"><view class="bar-wrap"><view class="bar" :style="{height: barHeight(item.amount)}"></view></view><text>{{ item.date.slice(5) }}</text><text>¥{{ item.amount }}</text></view></view><text v-else class="muted">暂无支出趋势。</text></view>
    <WalletMascot />
  </view>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import WalletMascot from '../../components/WalletMascot.vue'
const dashboard = ref({}); const palette=['#87aa78','#b8d9a9','#efd28a','#9ccfc4','#c8b6df','#e9aa91']
const items = computed(() => { const total=Number(dashboard.value.expense||0); return (dashboard.value.categoryBreakdown||[]).sort((a,b)=>Number(b.amount)-Number(a.amount)).slice(0,6).map((item,index)=>({...item,color:palette[index],percent:total?Math.round(Number(item.amount)/total*100):0})) })
const daily = computed(() => (dashboard.value.dailyExpense||[]).slice(-7))
const pieStyle = computed(() => { let point=0; const segments=items.value.map(item=>{const end=point+item.percent;const segment=`${item.color} ${point}% ${end}%`;point=end;return segment}); return {background:`conic-gradient(${segments.join(',')})`} })
function barHeight(amount){const max=Math.max(...daily.value.map(item=>Number(item.amount)),1);return `${Math.max(12,Number(amount)/max*100)}%`}
async function load(){dashboard.value=await api('/dashboard')}; onMounted(load);onShow(load)
</script>
<style scoped>
.intro{display:flex;flex-direction:column;gap:10rpx;margin:0 0 22rpx}.eyebrow{font-size:22rpx;color:#58744e;font-weight:700}.chart-row{display:flex;align-items:center;gap:28rpx;margin-bottom:30rpx}.donut{width:240rpx;height:240rpx;border-radius:50%;display:flex;align-items:center;justify-content:center;flex:none}.donut-hole{width:150rpx;height:150rpx;border-radius:50%;background:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;font-size:22rpx;color:#6c8066}.donut-hole text:last-child{font-weight:700;color:#354831;font-size:27rpx;margin-top:6rpx}.chart-note{display:flex;flex-direction:column;gap:12rpx}.legend-row{display:flex;justify-content:space-between;align-items:center;padding:19rpx 0;border-top:1rpx solid #edf2ea;font-size:25rpx}.legend-left{display:flex;align-items:center;gap:12rpx}.dot{display:inline-block;width:16rpx;height:16rpx;border-radius:50%}.empty{display:flex;flex-direction:column;gap:14rpx}.bars{height:260rpx;display:flex;gap:12rpx;align-items:flex-end;padding-top:24rpx}.bar-col{height:100%;flex:1;display:flex;flex-direction:column;justify-content:flex-end;align-items:center;gap:8rpx;font-size:18rpx;color:#6c8066}.bar-wrap{height:175rpx;width:100%;display:flex;align-items:flex-end;justify-content:center}.bar{width:34rpx;background:#87aa78;border-radius:12rpx 12rpx 4rpx 4rpx}
</style>
