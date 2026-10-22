<template>
  <view class="page">
    <view v-if="posts.length === 0" class="empty"><text>暂无帖子</text></view>
    <view v-for="post in posts" :key="post.id" class="post-item" @tap="goDetail(post.id)">
      <view class="p-header">
        <text class="p-title">{{ post.title }}</text>
        <text class="p-status" :class="post.status === 1 ? 's-ok' : post.status === 0 ? 's-pending' : 's-rej'">
          {{ post.status === 1 ? '已发布' : post.status === 0 ? '待审核' : '已驳回' }}
        </text>
      </view>
      <view class="p-meta">
        <text class="p-cat">{{ post.categoryName }}</text>
        <text>👁 {{ post.viewCount }}</text>
        <text>👍 {{ post.likeCount }}</text>
        <text>💬 {{ post.commentCount }}</text>
        <text class="p-date">{{ post.createTime }}</text>
      </view>
      <view class="p-actions">
        <text class="p-action" @tap.stop="editPost(post.id)">编辑</text>
        <text class="p-action danger" @tap.stop="delPost(post.id)">删除</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { postApi } from '../../../api/post'

const posts = ref([])

onMounted(async () => {
  const res = await postApi.myPosts()
  posts.value = res.data.records
})

function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
function editPost(id) { uni.navigateTo({ url: `/pages/post/edit/edit?id=${id}` }) }
async function delPost(id) {
  const cf = await new Promise(r => uni.showModal({ title: '确认删除', success: e => r(e.confirm) }))
  if (!cf) return
  await postApi.delete(id)
  uni.showToast({ title: '已删除' })
  posts.value = posts.value.filter(p => p.id !== id)
}
</script>

<style scoped>
.page { padding: 24rpx; }
.post-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.p-header { display: flex; justify-content: space-between; align-items: center; }
.p-title { font-size: 30rpx; font-weight: bold; flex: 1; }
.p-status { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 6rpx; }
.s-ok { background: #e8f5e9; color: #4caf50; }
.s-pending { background: #fff3e0; color: #ff9800; }
.s-rej { background: #fce4ec; color: #f44336; }
.p-meta { display: flex; gap: 16rpx; font-size: 22rpx; color: #999; margin: 12rpx 0; }
.p-cat { color: #4FAE84; }
.p-date { margin-left: auto; }
.p-actions { display: flex; gap: 24rpx; padding-top: 12rpx; border-top: 1rpx solid #f0f0f0; }
.p-action { font-size: 24rpx; color: #4FAE84; }
.p-action.danger { color: #D96666; }
.empty { text-align: center; padding: 120rpx 0; font-size: 28rpx; color: #999; }
</style>
