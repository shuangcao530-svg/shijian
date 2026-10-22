<template>
  <view class="page">
    <view v-if="posts.length === 0" class="empty"><text>暂无收藏</text></view>
    <view v-for="post in posts" :key="post.id" class="fav-item" @tap="goDetail(post.id)">
      <view class="fav-info">
        <text class="fav-title">{{ post.title }}</text>
        <text class="fav-excerpt">{{ post.content?.substring(0, 80) }}</text>
        <view class="fav-stats">
          <text>👁 {{ post.viewCount }}</text>
          <text>👍 {{ post.likeCount }}</text>
          <text>💬 {{ post.commentCount }}</text>
        </view>
      </view>
      <text class="fav-del" @tap.stop="removeFav(post.id)">取消收藏</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { favoriteApi } from '../../api/favorite'

const posts = ref([])

onMounted(async () => {
  const res = await favoriteApi.list()
  posts.value = res.data.records || []
})

async function removeFav(postId) {
  await favoriteApi.remove(postId)
  uni.showToast({ title: '已取消' })
  posts.value = posts.value.filter(p => p.id !== postId)
}

function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
</script>

<style scoped>
.page { padding: 24rpx; }
.fav-item { display: flex; align-items: center; background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; }
.fav-info { flex: 1; }
.fav-title { font-size: 28rpx; font-weight: bold; display: block; }
.fav-excerpt { font-size: 24rpx; color: #666; display: block; margin: 8rpx 0; }
.fav-stats { display: flex; gap: 16rpx; font-size: 22rpx; color: #999; }
.fav-del { font-size: 24rpx; color: #D96666; flex-shrink: 0; }
.empty { text-align: center; padding: 120rpx 0; font-size: 28rpx; color: #999; }
</style>
