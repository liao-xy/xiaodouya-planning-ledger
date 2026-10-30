<template>
  <view class="page">
    <view class="hero"><text class="eyebrow">小兜芽 · 钱格测试</text><text class="title">你的钱，是什么人格？</text><text class="muted">不是给你贴标签，是把你的花钱方式翻译成一张有梗的生活说明书。</text></view>
    <view v-if="loading" class="card muted">正在读取你的账本…</view>
    <template v-else-if="result.completed && !testing">
      <view class="result-card"><image v-if="resultImage" class="result-art" :src="resultImage" mode="aspectFit" /><view v-else class="art-placeholder"><text>待放入专属结果图</text><text>{{ result.imageKey }}</text></view><view class="result-copy"><text class="result-code">{{ result.code }}</text><text class="result-badge">{{ result.badge }}</text><text class="result-title">{{ result.title }}</text><text class="result-motto">{{ result.motto }}</text></view></view>
      <view class="card"><text class="section-title">你的钱格雷达</text><view v-for="item in metrics" :key="item.name" class="metric"><view class="row"><text>{{ item.name }}</text><text>{{ item.score }}%</text></view><view class="track"><view class="fill" :style="{width:item.score+'%'}"></view></view></view></view>
      <view class="card"><text class="section-title">这次分析看到了什么</text><text v-for="item in result.evidence" :key="item" class="paragraph">{{ item }}</text></view>
      <view class="card"><text class="section-title">给你的下一步</text><text v-for="(item,index) in result.suggestions" :key="item" class="suggestion">{{ index + 1 }}. {{ item }}</text></view>
      <view class="actions"><view class="secondary" @click="testing=true">重新测试</view><view class="primary" @click="askAI">让 AI 解读</view></view>
    </template>
    <template v-else>
      <view class="data-note"><text>✦</text><text>结果会结合你的已记录账单；数据不足时会如实提示，不会编造人设。</text></view>
      <view v-for="(question,index) in questions" :key="question.title" class="card question"><text class="question-no">0{{ index + 1 }}</text><text class="question-title">{{ question.title }}</text><view v-for="option in question.options" :key="option.text" :class="['option',{selected:answers[index]===option.value}]" @click="answers[index]=option.value"><text>{{ option.text }}</text><text v-if="answers[index]===option.value">✓</text></view></view>
      <view class="primary submit" @click="submit">生成我的钱格结果</view>
    </template>
    <WalletMascot />
  </view>
</template>
<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import WalletMascot from '../../components/WalletMascot.vue'
const loading=ref(true), testing=ref(false), result=ref({}), answers=ref(Array(8).fill(null))
const questions=[
  {title:'一笔收入到账后，你通常会？',options:[{text:'先分好用途，再开始花',value:2},{text:'大概心里有数',value:1},{text:'先开心一下，之后再说',value:0}]},
  {title:'看到喜欢但不在计划内的东西时？',options:[{text:'先看余额和计划',value:0},{text:'纠结一下再决定',value:1},{text:'心动就直接拿下',value:2}]},
  {title:'突然来了一笔必要支出，你会？',options:[{text:'从对应计划里调整',value:2},{text:'临时挪一挪',value:1},{text:'先付了再想办法',value:0}]},
  {title:'理想周末的花钱方式更像？',options:[{text:'低成本充电，也很舒服',value:0},{text:'看心情安排',value:1},{text:'体验拉满，值得就花',value:2}]},
  {title:'以下哪种消费最让你觉得“值”？',options:[{text:'能让我更安心的储备',value:0},{text:'实用又耐用的东西',value:1},{text:'难忘的体验和开心',value:2}]},
  {title:'多出一笔钱时，你更倾向？',options:[{text:'存起来或补齐计划',value:0},{text:'一半存，一半犒劳',value:1},{text:'安排一次想做很久的事',value:2}]},
  {title:'你记账的真实状态是？',options:[{text:'有消费就想顺手记下',value:2},{text:'隔几天集中补记',value:1},{text:'想起来才记',value:0}]},
  {title:'打开小兜芽时，你最想看到？',options:[{text:'我的钱是否还在轨道上',value:2},{text:'这个月花到哪里了',value:1},{text:'别再劝我省钱就行',value:0}]}
]
const metrics=computed(()=>[{name:'规划感',score:result.value.planningScore||0},{name:'体验感',score:result.value.experienceScore||0},{name:'记录力',score:result.value.recordScore||0}])
const defaultImages={
  'personality-p-g-r':'/static/personality/personality-p-g-r.jpg','personality-p-g-s':'/static/personality/personality-p-g-s.jpg',
  'personality-p-e-r':'/static/personality/personality-p-e-r.jpg','personality-p-e-s':'/static/personality/personality-p-e-s.jpg',
  'personality-f-g-r':'/static/personality/personality-f-g-r.jpg','personality-f-g-s':'/static/personality/personality-f-g-s.jpg',
  'personality-f-e-r':'/static/personality/personality-f-e-r.jpg','personality-f-e-s':'/static/personality/personality-f-e-s.jpg'
}
const resultImage=computed(()=>result.value.imageUrl||defaultImages[result.value.imageKey]||'')
onLoad(load)
async function load(){try{result.value=await api('/money-personality')}catch(error){uni.showToast({title:error?.message||'加载失败',icon:'none'})}finally{loading.value=false}}
async function submit(){if(answers.value.some(item=>item===null))return uni.showToast({title:'还有题目没选完哦',icon:'none'});try{result.value=await api('/money-personality/assess',{method:'POST',data:{answers:answers.value}});testing.value=false;uni.pageScrollTo({scrollTop:0,duration:250})}catch(error){uni.showToast({title:error?.message||'生成失败',icon:'none'})}}
function askAI(){uni.setStorageSync('pendingAssistantQuestion',{text:'请解读我最新的钱格测试结果：结合最新测试结论与当前真实账本，说明这个结果对我的钱意味着什么，并给出不超过 3 条具体可执行的建议。',createNew:true});uni.navigateTo({url:'/pages/assistant/index'})}
</script>
<style scoped>
.hero{padding:8rpx 6rpx 30rpx;display:flex;flex-direction:column;gap:13rpx}.eyebrow{color:#58744e;font-size:22rpx;font-weight:700;letter-spacing:3rpx}.hero .title{font-size:50rpx;line-height:1.2}.data-note{background:#eff8eb;border:1rpx solid #dcebd6;border-radius:22rpx;padding:20rpx;display:flex;gap:14rpx;color:#58744e;font-size:24rpx;margin-bottom:20rpx}.question{position:relative;padding-top:34rpx}.question-no{position:absolute;right:28rpx;top:23rpx;font-size:30rpx;color:#c1d6ba;font-weight:800}.question-title{display:block;font-size:31rpx;font-weight:700;margin-bottom:22rpx;padding-right:50rpx}.option{border:1rpx solid #deebd9;border-radius:16rpx;padding:19rpx;margin-top:14rpx;display:flex;justify-content:space-between;font-size:25rpx;color:#52654e}.option.selected{background:#e4f3de;border-color:#9fbd93;color:#3d6536;font-weight:700}.submit{margin:12rpx 0 40rpx}.result-card{position:relative;overflow:hidden;border-radius:34rpx;padding:36rpx;background:linear-gradient(145deg,#ceeac6,#eaf7e6);display:flex;flex-direction:column;gap:14rpx;margin-bottom:22rpx;min-height:330rpx}.result-code{font-family:monospace;font-size:24rpx;color:#63865a;letter-spacing:3rpx}.result-badge{font-size:23rpx;color:#58744e}.result-title{font-size:52rpx;font-weight:800;color:#304c2b;z-index:1}.result-motto{font-size:27rpx;line-height:1.6;color:#476343;max-width:440rpx;z-index:1}.mini-mascot{position:absolute;width:250rpx;height:250rpx;right:-25rpx;bottom:-30rpx;opacity:.92}.mini-mascot image{width:100%;height:100%}.section-title{font-size:31rpx;font-weight:800;display:block;margin-bottom:18rpx}.metric{margin-top:20rpx}.metric .row{font-size:25rpx}.track{height:16rpx;background:#edf3e9;border-radius:999rpx;overflow:hidden;margin-top:11rpx}.fill{height:100%;background:linear-gradient(90deg,#86aa78,#b9d8ac);border-radius:999rpx}.paragraph,.suggestion{display:block;color:#60735b;font-size:25rpx;line-height:1.65;margin-top:14rpx}.suggestion{color:#465e40}.actions{display:flex;gap:16rpx;margin-bottom:35rpx}.actions view{flex:1;text-align:center}.secondary{border:1rpx solid #bcd3b4;color:#58744e;background:#fff;border-radius:18rpx;padding:18rpx 24rpx}
.result-card{padding:0 0 34rpx;gap:0;min-height:0}.result-art{position:relative;right:auto;bottom:auto;width:100%;height:510rpx;display:block;z-index:1}.result-copy{padding:0 34rpx;display:flex;flex-direction:column;gap:14rpx}.result-code,.result-badge,.result-title,.result-motto{display:block}.art-placeholder{position:relative;right:auto;bottom:auto;margin:28rpx;width:auto;height:360rpx;border:2rpx dashed #9fc595;border-radius:24rpx;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:10rpx;color:#63865a;font-size:20rpx;text-align:center}.art-placeholder text:last-child{font-family:monospace;font-size:17rpx;color:#88a27e}
</style>
