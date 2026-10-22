<template>
  <view class="page">
    <!-- Hot Topics -->
    <view class="section-title">🔥 热门话题</view>
    <scroll-view scroll-x class="hot-topic-bar" :show-scrollbar="false">
      <view v-for="t in hotTopics" :key="t.id" class="hot-topic-item" @tap="goTopic(t)">
        <text class="ht-name">{{ t.name }}</text>
        <text class="ht-count">{{ t.postCount }} 帖</text>
      </view>
    </scroll-view>

    <!-- My Followed Topics -->
    <view class="section-title" style="margin-top: 10rpx;">📌 我的关注</view>
    <view v-if="myTopics.length === 0" class="no-follow">
      <text class="nf-text">还没有关注任何话题，去看看热门的吧~</text>
    </view>
    <view v-else class="my-topic-grid">
      <view v-for="t in myTopics" :key="t.id" class="my-topic-item" @tap="goTopic(t)">
        <text class="mt-name">{{ t.name }}</text>
      </view>
    </view>

    <!-- All Topics -->
    <view class="section-title" style="margin-top: 10rpx;">📋 全部话题</view>
    <view v-for="t in allTopics" :key="t.id" class="topic-card" @tap="goTopic(t)">
      <view class="tc-left">
        <text class="tc-name">{{ t.name }}</text>
        <text class="tc-desc" v-if="t.description">{{ t.description }}</text>
        <view class="tc-stats">
          <text>{{ t.postCount }} 帖子</text>
          <text>·</text>
          <text>{{ t.followCount }} 关注</text>
        </view>
      </view>
      <view class="tc-right" @tap.stop="toggleFollow(t)">
        <text class="follow-btn" :class="{ following: t.isFollowed }">{{ t.isFollowed ? '已关注' : '+ 关注' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { topicApi } from '../../api/topic'

const hotTopics = ref([])
const myTopics = ref([])
const allTopics = ref([])

async function loadData() {
  try {
    const [hotRes, allRes] = await Promise.all([
      topicApi.listHot(8),
      topicApi.listAll()
    ])
    hotTopics.value = hotRes.data || []
    allTopics.value = allRes.data || []

    // Load followed status
    try {
      const myRes = await topicApi.myFollows()
      const followedIds = new Set((myRes.data || []).map(t => t.id))
      myTopics.value = myRes.data || []
      allTopics.value.forEach(t => { t.isFollowed = followedIds.has(t.id) })
      hotTopics.value.forEach(t => { t.isFollowed = followedIds.has(t.id) })
    } catch (e) { /* not logged in */ }
  } catch (e) { /* ignore */ }
}

async function toggleFollow(topic) {
  try {
    if (topic.isFollowed) {
      await topicApi.unfollow(topic.id)
      topic.isFollowed = false
      topic.followCount = Math.max(0, (topic.followCount || 1) - 1)
    } else {
      await topicApi.follow(topic.id)
      topic.isFollowed = true
      topic.followCount = (topic.followCount || 0) + 1
    }
  } catch (e) { /* ignore */ }
}

function goTopic(topic) {
  uni.navigateTo({ url: `/pages/topic/detail/detail?id=${topic.id}&name=${encodeURIComponent(topic.name)}` })
}

onMounted(loadData)
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #EEF8F0; }
.section-title { font-size: 30rpx; font-weight: 700; color: #173B2F; margin-bottom: 16rpx; padding: 0 8rpx; }

.hot-topic-bar { white-space: nowrap; padding: 4rpx 0 16rpx; }
.hot-topic-item { display: inline-flex; flex-direction: column; align-items: center; padding: 18rpx 28rpx; margin-right: 16rpx; background: linear-gradient(135deg, #FF8C42, #FFB347); border-radius: 16rpx; color: #fff; min-width: 140rpx; box-shadow: 0 8rpx 20rpx rgba(255,140,66,0.25); }
.ht-name { font-size: 26rpx; font-weight: 600; }
.ht-count { font-size: 22rpx; margin-top: 6rpx; opacity: 0.85; }

.no-follow { padding: 30rpx; text-align: center; }
.nf-text { font-size: 26rpx; color: #8DA49A; }

.my-topic-grid { display: flex; flex-wrap: wrap; gap: 14rpx; margin-bottom: 20rpx; }
.my-topic-item { padding: 14rpx 24rpx; background: #fff; border-radius: 30rpx; border: 1rpx solid #4FAE84; }
.mt-name { font-size: 25rpx; color: #4FAE84; font-weight: 600; }

.topic-card { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; background: #fff; border-radius: 16rpx; margin-bottom: 12rpx; }
.tc-left { flex: 1; }
.tc-name { font-size: 28rpx; font-weight: 700; color: #173B2F; }
.tc-desc { font-size: 23rpx; color: #8DA49A; display: block; margin-top: 6rpx; }
.tc-stats { font-size: 22rpx; color: #8DA49A; margin-top: 8rpx; display: flex; gap: 8rpx; }
.follow-btn { font-size: 24rpx; color: #4FAE84; border: 1rpx solid #4FAE84; padding: 10rpx 22rpx; border-radius: 24rpx; font-weight: 600; }
.follow-btn.following { background: #E8F5EC; color: #2F8F68; border-color: #2F8F68; }
</style>
