<script>export default { onLaunch() { if (!uni.getStorageSync('accessToken')) uni.reLaunch({ url:'/pages/auth/index' }) } }</script>
<style>
page { background:#f8fcf6; color:#354831; font-family:PingFang SC,Microsoft YaHei,sans-serif; }
.page { min-height:100vh; padding:28rpx; box-sizing:border-box; }
.card { background:#fff; border:1rpx solid #dcebd6; border-radius:28rpx; padding:28rpx; margin-bottom:22rpx; }
.muted { color:#6c8066; font-size:24rpx; }.title { font-size:38rpx; font-weight:700; }.primary { background:#87aa78; color:#fff; border-radius:18rpx; padding:18rpx 24rpx; text-align:center; }.row { display:flex; align-items:center; justify-content:space-between; }.chip { background:#eef8ea; color:#58744e; padding:8rpx 14rpx; border-radius:999rpx; font-size:22rpx; }
</style>
