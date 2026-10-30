<template>
  <view class="page">
    <view class="intro"><text class="eyebrow">先分配，再消费</text><text class="title">资金规划</text><text class="muted">计划额度从当前钱包中划出，不能超过可分配余额。</text></view>
    <view class="allocation card"><view><text>当前钱包</text><strong>¥ {{ money(dashboard.walletBalance) }}</strong></view><view><text>尚可分配</text><strong>¥ {{ money(dashboard.unallocated) }}</strong></view></view>
    <view class="card"><text class="title">{{ editingId ? '编辑资金计划' : '新建资金计划' }}</text><view class="field"><text>计划名称</text><input v-model="form.name" placeholder="例如旅行基金" /></view><view class="field"><text>分配金额</text><input v-model="form.budget" type="digit" placeholder="0.00" /></view><view class="field"><text>截至日期</text><picker mode="date" @change="form.deadline=$event.detail.value"><view>{{ form.deadline || '不设截止日期' }}</view></picker></view><view class="form-actions"><view v-if="editingId" class="secondary" @click="cancelEdit">取消</view><view class="primary" @click="save">{{ editingId ? '保存修改' : '创建计划' }}</view></view></view>
    <view v-if="!plans.length" class="card muted">还没有资金计划。可以从“日常生活”或“旅行基金”开始。</view>
    <view v-for="plan in plans" :key="plan.id" class="card plan" @click="startEdit(plan)"><view class="row"><text class="title">{{ plan.name }}</text><text class="chip">余 ¥ {{ money(plan.remaining) }}</text></view><view class="track"><view class="fill" :style="{width: Math.min(100,plan.spent/plan.budget*100)+'%'}"></view></view><view class="row muted"><text>已用 ¥ {{ money(plan.spent) }} / ¥ {{ money(plan.budget) }}</text><text>{{ plan.deadline || '长期计划' }}</text></view><view class="plan-actions"><text>点按编辑</text><text class="danger" @click.stop="remove(plan)">删除</text></view></view><WalletMascot /></view>
</template>
<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import WalletMascot from '../../components/WalletMascot.vue'
const plans=ref([]), dashboard=ref({}), editingId=ref(null), form=ref({name:'',budget:'',deadline:''})
const money = value => Number(value || 0).toFixed(2)
async function load(){ try { const [planRows, data] = await Promise.all([api('/plans'),api('/dashboard')]); plans.value=planRows; dashboard.value=data } catch(error) { uni.showToast({title:error?.message || '计划加载失败',icon:'none'}) } }
function startEdit(plan){ editingId.value=plan.id; form.value={name:plan.name,budget:String(plan.budget),deadline:plan.deadline || ''}; uni.pageScrollTo({scrollTop:0,duration:200}) }
function cancelEdit(){ editingId.value=null; form.value={name:'',budget:'',deadline:''} }
async function save(){ if(!form.value.name||!Number(form.value.budget))return uni.showToast({title:'请填写名称和分配金额',icon:'none'}); try { const path=editingId.value?`/plans/${editingId.value}`:'/plans'; await api(path,{method:editingId.value?'PUT':'POST',data:{...form.value,budget:Number(form.value.budget),color:'sage'}}); cancelEdit(); await load(); uni.showToast({title:'计划已保存'}) } catch(error) { uni.showToast({title:error?.message || '无法保存计划',icon:'none'}) } }
function remove(plan){ uni.showModal({title:`删除“${plan.name}”？`,content:'历史账单会保留，但不再归入该计划；删除后无法恢复。',confirmColor:'#b56f55',success:async result=>{if(!result.confirm)return;try{await api(`/plans/${plan.id}`,{method:'DELETE'});if(editingId.value===plan.id)cancelEdit();await load();uni.showToast({title:'已删除'})}catch(error){uni.showToast({title:error?.message || '删除失败',icon:'none'})}}}) }
onShow(load)
</script>
<style scoped>
.intro{display:flex;flex-direction:column;gap:10rpx;margin:0 0 22rpx}.eyebrow{font-size:22rpx;color:#58744e;font-weight:700}.allocation{display:flex;gap:20rpx;margin-bottom:20rpx}.allocation view{flex:1;display:flex;flex-direction:column;gap:10rpx;color:#6c8066;font-size:23rpx}.allocation strong{font-size:32rpx;color:#354831}.field{padding:20rpx 0;border-bottom:1rpx solid #edf2ea;display:flex;justify-content:space-between}.field text{color:#6c8066}.field input,.field picker{flex:1;text-align:right}.form-actions{display:flex;gap:16rpx;margin-top:24rpx}.form-actions view{flex:1;text-align:center}.secondary{border:1rpx solid #bcd3b4;color:#58744e;background:#fff;border-radius:18rpx;padding:18rpx 24rpx}.primary{margin-top:0}.plan{margin-bottom:18rpx}.track{height:13rpx;background:#edf3e9;border-radius:99rpx;overflow:hidden;margin:24rpx 0 15rpx}.fill{height:100%;background:#87aa78;border-radius:99rpx}.plan-actions{padding-top:18rpx;margin-top:18rpx;border-top:1rpx solid #edf2ea;display:flex;justify-content:space-between;color:#6c8066;font-size:22rpx}.danger{color:#b56f55}
</style>
