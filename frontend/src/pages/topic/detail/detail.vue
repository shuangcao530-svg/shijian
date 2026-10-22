<template>
  <view class="page">
    <view class="topic-header">
      <text class="th-name">{{ topicName }}</text>
      <view class="th-actions">
        <text class="follow-btn" :class="{ following: isFollowed }" @tap="toggleFollow">
          {{ isFollowed ? '已关注' : '+ 关注' }}
        </text>
      </view>
    </view>

    <view v-if="posts.length === 0 && !loading" class="empty">
      <text class="empty-icon">📝</text>
      <text class="empty-text">该话题下还没有内容</text>
    </view>

    <view v-for="post in posts" :key="post.id" class="post-card" @tap="goDetail(post.id)">
      <view class="post-header">
        <image class="avatar" :src="post.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="post-meta">
          <text class="post-author">{{ post.nickname }}</text>
          <text class="post-time">{{ post.createTime }}</text>
        </view>
      </view>
      <text class="post-title">{{ post.title }}</text>
      <text class="post-excerpt">{{ (post.content || '').substring(0, 150) }}</text>
      <view class="post-images" v-if="post.images?.length">
        <view class="img-grid" :class="'grid-' + Math.min(post.images.length, 3)">
          <image v-for="(img, i) in post.images.slice(0, 3)" :key="i" :src="img" mode="aspectFill" class="post-img" />
        </view>
      </view>
      <view class="post-footer">
        <view class="p-stats">
          <text>❤️ {{ post.likeCount }}</text>
          <text>💬 {{ post.commentCount }}</text>
          <text>👁 {{ post.viewCount }}</text>
        </view>
      </view>
    </view>

    <view v-if="hasMore" class="load-more" @tap="loadMore">
      <text class="load-text">{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { topicApi } from '../../../api/topic'
import { postApi } from '../../../api/post'

const topicId = ref(null)
const topicName = ref('')
const isFollowed = ref(false)
const posts = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }

async function toggleFollow() {
  try {
    if (isFollowed.value) {
      await topicApi.unfollow(topicId.value)
      isFollowed.value = false
    } else {
      await topicApi.follow(topicId.value)
      isFollowed.value = true
    }
  } catch (e) { /* ignore */ }
}

async function loadPosts(reset = false) {
  if (loading.value) return
  if (reset) { page.value = 1; posts.value = [] }
  loading.value = true
  try {
    const res = await topicApi.getTopicPosts(topicId.value, { page: page.value, size: 10 })
    if (reset) posts.value = res.data.records
    else posts.value = [...posts.value, ...res.data.records]
    hasMore.value = res.data.records.length >= 10
    page.value++
  } finally { loading.value = false }
}

function loadMore() { loadPosts() }

onMounted(() => {
  const query = getCurrentPages()[getCurrentPages().length - 1]?.$page?.options || {}
  topicId.value = Number(query.id)
  // Fix: uni-app API for getting page options
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const options = page?.options || page?.$page?.options || {}
  topicId.value = Number(options.id)
  topicName.value = decodeURIComponent(options.name || '话题')
  loadPosts(true)
  topicApi.checkFollow(topicId.value).then(res => {
    isFollowed.value = res.data
  }).catch(() => {})
})
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #EEF8F0; }
.topic-header { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 8rpx; margin-bottom: 20rpx; }
.th-name { font-size: 36rpx; font-weight: 700; color: #173B2F; }
.follow-btn { font-size: 26rpx; color: #4FAE84; border: 1rpx solid #4FAE84; padding: 12rpx 28rpx; border-radius: 24rpx; }
.follow-btn.following { background: #E8F5EC; color: #2F8F68; }

.post-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; }
.post-header { display: flex; align-items: center; gap: 12rpx; }
.avatar { width: 64rpx; height: 64rpx; border-radius: 50%; }
.post-meta { flex: 1; }
.post-author { font-weight: 600; font-size: 26rpx; display: block; }
.post-time { font-size: 22rpx; color: #999; }
.post-title { font-size: 30rpx; font-weight: bold; margin: 12rpx 0 6rpx; display: block; }
.post-excerpt { font-size: 25rpx; color: #666; display: block; line-height: 1.6; }
.post-images { margin-top: 12rpx; }
.img-grid { display: flex; gap: 8rpx; }
.img-grid.grid-1 .post-img { width: 100%; height: 320rpx; }
.img-grid.grid-2 .post-img { width: calc(50% - 4rpx); height: 200rpx; }
.img-grid.grid-3 .post-img { width: calc(33.33% - 6rpx); height: 180rpx; }
.post-img { border-radius: 12rpx; }
.post-footer { margin-top: 14rpx; }
.p-stats { display: flex; gap: 24rpx; font-size: 22rpx; color: #999; }

.empty { text-align: center; padding: 160rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #8DA49A; }

.load-more { text-align: center; padding: 28rpx; }
.load-text { font-size: 26rpx; color: #8DA49A; }
</style>
