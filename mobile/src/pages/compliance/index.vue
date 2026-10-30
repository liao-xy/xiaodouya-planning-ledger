<template>
  <view class="page">
    <view class="hero"><text class="eyebrow">小兜芽 · 服务公开信息</text><text class="title">服务与资质</text><text>把必要信息放在这里，方便你随时核对。</text></view>
    <view class="card filing-card">
      <view class="row"><text class="section-title">互联网信息服务备案</text><text :class="['status', compliance.filingReady ? 'ready' : 'pending']">{{ compliance.filingReady ? '已配置' : '待配置' }}</text></view>
      <template v-if="compliance.filingReady"><text class="filing-no" @click="openMiit">{{ compliance.filingNumber }}</text><text class="hint">点击备案号，前往工信部备案系统核验。</text></template>
      <template v-else><text class="notice">尚未取得并配置备案编号。此状态下仅可用于开发、测试，不应面向公众正式提供服务。</text><text class="hint">获得核准编号后，在后端配置 APP_ICP_FILING_NO 并重新启动服务。</text></template>
    </view>
    <view class="card">
      <text class="section-title">主体与联系</text>
      <view class="info-row"><text>服务名称</text><text>小兜芽</text></view>
      <view class="info-row"><text>主办者</text><text>{{ compliance.operatorName || '正式上线前配置' }}</text></view>
      <view class="info-row"><text>服务邮箱</text><text>{{ compliance.serviceEmail || '正式上线前配置' }}</text></view>
      <view class="info-row"><text>联系电话</text><text>{{ compliance.servicePhone || '正式上线前配置' }}</text></view>
    </view>
    <view class="card">
      <text class="section-title">内容安全与反馈</text>
      <text class="description">兜兜 AI 会拦截明显涉及违法、高风险活动的提问；你可以在这里反馈违法有害信息、账号异常或服务问题。反馈会在服务端留痕，供后续处理和核查。</text>
      <textarea v-model="reportContent" maxlength="1000" placeholder="请描述问题，不要填写密码、验证码、身份证号等敏感信息" class="report-input" />
      <view :class="['primary', { disabled: !reportContent.trim() || submitting }]" @click="submitReport">{{ submitting ? '提交中…' : '提交反馈' }}</view>
    </view>
    <view class="card danger-card">
      <text class="section-title">账号与数据</text><text class="description">注销后会删除该微信身份下的账单、资金计划、钱格测试和 AI 对话，且无法恢复。已进入违法有害内容处置流程的必要记录，可能依法留存。</text>
      <view class="delete-button" @click="deleteAccount">注销账号并删除数据</view>
    </view>
  </view>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../utils/api'
const compliance = ref({ filingReady:false }); const reportContent = ref(''); const submitting = ref(false)
async function load(){ compliance.value = await api('/public/compliance', { skipAuth:true }) }
onMounted(load)
function openMiit(){ const src = encodeURIComponent(compliance.value.filingUrl || 'https://beian.miit.gov.cn'); uni.navigateTo({ url:`/pages/webview/index?src=${src}` }) }
async function submitReport(){ if(!reportContent.value.trim() || submitting.value) return; submitting.value=true; try { await api('/safety/reports',{method:'POST',data:{content:reportContent.value.trim()}}); reportContent.value=''; uni.showToast({title:'已收到反馈',icon:'success'}) } catch(e){uni.showToast({title:e.message||'提交失败',icon:'none'})} finally{submitting.value=false} }
function deleteAccount(){ uni.showModal({title:'确认注销？',content:'你的账单、计划、AI 对话和测试结果将被永久删除，无法恢复；依法需留存的安全处置记录除外。',confirmText:'确认注销',confirmColor:'#b8544c',success:async ({confirm})=>{if(!confirm)return;try{await api('/auth/account',{method:'DELETE'});uni.removeStorageSync('accessToken');uni.removeStorageSync('profile');uni.reLaunch({url:'/pages/auth/index'})}catch(e){uni.showToast({title:e.message||'注销失败',icon:'none'})}}}) }
</script>
<style scoped>
.hero{display:flex;flex-direction:column;gap:12rpx;padding:16rpx 8rpx 30rpx;color:#6c8066;font-size:25rpx}.eyebrow{font-size:21rpx;letter-spacing:2rpx;color:#6e9562}.title{font-size:46rpx;font-weight:800;color:#354831}.section-title{display:block;font-size:31rpx;font-weight:750;color:#354831;margin-bottom:20rpx}.status{padding:7rpx 15rpx;border-radius:999rpx;font-size:21rpx}.ready{background:#ddf1d8;color:#497443}.pending{background:#fff0d9;color:#9a6b27}.filing-no{font-size:32rpx;font-weight:750;color:#58744e;text-decoration:underline;margin-bottom:12rpx}.notice{line-height:1.7;color:#8b5e22}.hint{display:block;font-size:22rpx;color:#789073;line-height:1.6;margin-top:12rpx}.info-row{display:flex;justify-content:space-between;gap:24rpx;padding:20rpx 0;border-top:1rpx solid #edf2ea;font-size:25rpx;color:#58744e}.info-row text:last-child{text-align:right;color:#354831;max-width:430rpx;word-break:break-all}.description{display:block;color:#667b61;font-size:24rpx;line-height:1.75}.report-input{margin-top:20rpx;width:100%;height:170rpx;box-sizing:border-box;background:#f4faef;border-radius:18rpx;padding:18rpx;color:#354831;font-size:25rpx}.primary{margin-top:18rpx}.disabled{opacity:.55}.danger-card{border-color:#f1d4ce}.delete-button{margin-top:20rpx;background:#fff0ec;color:#a65a4c;border-radius:18rpx;padding:20rpx;text-align:center;font-size:26rpx}
</style>
