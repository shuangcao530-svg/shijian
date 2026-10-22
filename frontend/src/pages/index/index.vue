<template>
  <view class="home">
    <!-- Search Bar -->
    <view class="search-bar" @tap="goSearch">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索校园日常内容...</text>
      </view>
    </view>

    <!-- Category Tabs -->
    <scroll-view scroll-x class="category-bar" :show-scrollbar="false">
      <view class="cat-item" :class="{ active: !currentCat }" @tap="switchCat(null)">
        <text class="cat-emoji">✨</text><text>全部</text>
      </view>
      <view v-for="(cat, idx) in categories" :key="cat.id" class="cat-item" :class="{ active: currentCat === cat.id }" @tap="switchCat(cat.id)">
        <text class="cat-emoji">{{ catEmojis[idx] || '📌' }}</text><text>{{ cat.name }}</text>
      </view>
    </scroll-view>

    <!-- Hot Topics -->
    <scroll-view v-if="hotTopics.length > 0" scroll-x class="topic-bar" :show-scrollbar="false">
      <view v-for="t in hotTopics" :key="t.id" class="topic-item" @tap="goTopic(t)">
        <text class="topic-name">{{ t.name }}</text>
      </view>
    </scroll-view>

    <!-- Sort -->
    <view class="sort-bar">
      <view class="sort-item" :class="{ active: sort === 'latest' }" @tap="changeSort('latest')">最新</view>
      <view class="sort-item" :class="{ active: sort === 'hot' }" @tap="changeSort('hot')">热门</view>
      <view class="sort-item" :class="{ active: sort === 'popular' }" @tap="changeSort('popular')">最多浏览</view>
    </view>

    <!-- Post List -->
    <view v-if="posts.length === 0 && !loading" class="empty">
      <text class="empty-icon">📝</text>
      <text class="empty-text">还没有内容，快来发布第一条吧</text>
    </view>

    <view v-for="post in posts" :key="post.id" class="post-card" @tap="goDetail(post.id)">
      <!-- Level accent strip -->
      <view class="card-accent" :class="'accent-' + levelColor(post.level)"></view>

      <view class="card-body">
        <view class="post-header">
          <image class="avatar" :src="post.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" @tap.stop="goUser(post.userId)" />
          <view class="post-meta" @tap.stop="goUser(post.userId)">
            <text class="post-author">{{ post.nickname }}<text v-if="post.level >= 4" class="verified"> 🔥</text></text>
            <text class="post-time">{{ post.createTime }}</text>
          </view>
          <view class="level-badge" :class="'lvl-' + levelColor(post.level)">
            {{ levelText(post.level) }}
          </view>
        </view>

        <text class="post-title">{{ post.title }}</text>
        <text class="post-excerpt" :class="{ clamp: !expanded[post.id] }">{{ post.content }}</text>
        <text v-if="post.content?.length > 120" class="expand-btn" @tap.stop="toggleExpand(post.id)">{{ expanded[post.id] ? '收起' : '展开全文' }}</text>

        <view class="post-images" v-if="post.images?.length">
          <view class="img-grid" :class="'grid-' + Math.min(post.images.length, 3)">
            <image v-for="(img, i) in post.images.slice(0, 3)" :key="i" :src="img" mode="aspectFill" class="post-img" @tap.stop="previewPostImages(post.images, i)" />
          </view>
          <text v-if="post.images.length > 3" class="img-more">+{{ post.images.length - 3 }}</text>
        </view>

        <view class="post-footer">
          <view class="footer-tag">{{ post.categoryName }}</view>
          <text v-if="post.location" class="footer-loc">📍 {{ post.location }}</text>
          <view class="post-stats">
            <view class="stat-item"><text class="stat-icon">❤️</text><text>{{ post.likeCount }}</text></view>
            <view class="stat-item"><text class="stat-icon">💬</text><text>{{ post.commentCount }}</text></view>
            <view class="stat-item"><text class="stat-icon">👁</text><text>{{ post.viewCount }}</text></view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="hasMore" class="load-more" @tap="loadMore"><text class="load-more-text">{{ loading ? '加载中...' : '点击加载更多' }}</text></view>
  </view>

  <!-- 底部导航 -->
  <view class="tab-bar">
    <view class="t-item" @tap="goTab(0, '/pages/index/index')"><text class="t-icon">🏠</text><text class="t-text" :class="{ on: tabIdx===0 }">首页</text></view>
    <view class="t-item" @tap="goTab(1, '/pages/moments/moments')"><text class="t-icon">🌿</text><text class="t-text" :class="{ on: tabIdx===1 }">好友圈</text></view>
    <view class="t-item" @tap="goTab(2, '/pages/post/create/create')">
      <view class="t-plus"><text class="t-plus-text">＋</text></view>
    </view>
    <view class="t-item" @tap="goTab(3, '/pages/messages/messages')">
      <view class="t-icon-wrap">
        <text class="t-icon">💬</text>
        <text v-if="unreadCount > 0" class="t-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
      </view>
      <text class="t-text" :class="{ on: tabIdx===3 }">消息</text>
    </view>
    <view class="t-item" @tap="goTab(4, '/pages/user/profile/profile')"><text class="t-icon">👤</text><text class="t-text" :class="{ on: tabIdx===4 }">我的</text></view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { postApi } from '../../api/post'
import { categoryApi } from '../../api/category'
import { topicApi } from '../../api/topic'
import { notificationApi } from '../../api/notification'

const tabIdx = ref(0)
const posts = ref([])
const categories = ref([])
const currentCat = ref(null)
const sort = ref('latest')
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)
const expanded = reactive({})
const hotTopics = ref([])
const unreadCount = ref(0)

const catEmojis = ['🍃', '📚', '💬', '🏠', '🎒', '🌈', '📌']
const levelColors = { 1: 'safe', 2: 'warn', 3: 'warn', 4: 'danger', 5: 'danger' }
const levelLabels = { 1: '⭐', 2: '⭐⭐', 3: '⭐⭐⭐', 4: '⭐⭐⭐⭐', 5: '⭐⭐⭐⭐⭐' }
function levelColor(l) { return levelColors[l] || 'safe' }
function levelText(l) { return levelLabels[l] || '未知' }

function toggleExpand(id) { expanded[id] = !expanded[id] }

function goTab(i, url) {
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
function goSearch() { uni.navigateTo({ url: '/pages/search/search' }) }
function goUser(id) { uni.navigateTo({ url: `/pages/user/info/info?id=${id}` }) }
function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
function goTopic(t) { uni.navigateTo({ url: `/pages/topic/detail/detail?id=${t.id}&name=${encodeURIComponent(t.name)}` }) }
function previewPostImages(urls, i) { uni.previewImage({ urls, current: i }) }

async function fetchPosts(reset = false) {
  if (loading.value) return
  if (reset) { page.value = 1; posts.value = [] }
  loading.value = true
  try {
    const res = await postApi.page({ categoryId: currentCat.value, sort: sort.value, page: page.value, size: 10 })
    if (reset) posts.value = res.data.records
    else posts.value = [...posts.value, ...res.data.records]
    hasMore.value = res.data.records.length >= 10
    page.value++
  } finally { loading.value = false }
}

function switchCat(id) { currentCat.value = id; fetchPosts(true) }
function changeSort(s) { sort.value = s; fetchPosts(true) }
function loadMore() { fetchPosts() }

onMounted(async () => {
  const res = await categoryApi.list()
  categories.value = res.data
  fetchPosts(true)
  // Load hot topics
  topicApi.listHot(6).then(r => { hotTopics.value = r.data || [] }).catch(() => {})
  // Load unread notifications
  const token = uni.getStorageSync('token')
  if (token) {
    notificationApi.getUnreadCount().then(r => { unreadCount.value = r.data || 0 }).catch(() => {})
  }
})
</script>

<style scoped>
.home {
  padding: 24rpx 24rpx 48rpx;
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(191, 231, 204, 0.8) 0%, rgba(238, 248, 240, 0.96) 280rpx, #EEF8F0 100%);
}

/* Search */
.search-bar { margin-bottom: 22rpx; }
.search-box { display: flex; align-items: center; background: rgba(255,255,255,0.92); border: 1rpx solid rgba(79,174,132,0.16); border-radius: 44rpx; padding: 22rpx 30rpx; box-shadow: 0 14rpx 34rpx rgba(39,95,70,0.08); }
.search-icon { font-size: 30rpx; margin-right: 14rpx; }
.search-placeholder { font-size: 26rpx; color: #8DA49A; }

/* Categories */
.category-bar { white-space: nowrap; padding: 8rpx 0; margin-bottom: 16rpx; }
.cat-item { display: inline-flex; align-items: center; gap: 6rpx; padding: 15rpx 28rpx; margin-right: 14rpx; border-radius: 40rpx; font-size: 26rpx; color: #48685B; background: rgba(255,255,255,0.86); border: 1rpx solid rgba(79,174,132,0.12); box-shadow: 0 8rpx 22rpx rgba(39,95,70,0.06); transition: all 0.2s; }
.cat-emoji { font-size: 26rpx; }
.cat-item.active { background: linear-gradient(135deg, #2F8F68, #66C99A); color: #FFF; box-shadow: 0 12rpx 28rpx rgba(47,143,104,0.26); font-weight: 600; }

/* Hot Topics */
.topic-bar { white-space: nowrap; padding: 8rpx 0; margin-bottom: 16rpx; }
.topic-item { display: inline-flex; align-items: center; padding: 12rpx 22rpx; margin-right: 12rpx; border-radius: 24rpx; font-size: 24rpx; color: #FF6B35; background: linear-gradient(135deg, #FFF3E6, #FFE8D6); border: 1rpx solid rgba(255,107,53,0.15); }
.topic-name { font-weight: 600; }

/* Sort */
.sort-bar { display: flex; gap: 24rpx; margin-bottom: 22rpx; padding: 0 8rpx; }
.sort-item { font-size: 25rpx; color: #8DA49A; padding: 6rpx 0; position: relative; }
.sort-item.active { color: #4FAE84; font-weight: 600; }
.sort-item.active::after { content: ''; position: absolute; bottom: -4rpx; left: 25%; right: 25%; height: 3rpx; background: #4FAE84; border-radius: 2rpx; }

/* Post Card */
.post-card { background: rgba(255,255,255,0.94); margin-bottom: 22rpx; border-radius: 24rpx; overflow: hidden; box-shadow: 0 16rpx 38rpx rgba(39,95,70,0.08); border: 1rpx solid rgba(79,174,132,0.11); position: relative; }
.card-accent { position: absolute; left: 0; top: 0; bottom: 0; width: 6rpx; border-radius: 3rpx 0 0 3rpx; }
.accent-safe { background: #52B788; }
.accent-warn { background: #E5A63A; }
.accent-danger { background: #D96666; }
.card-body { padding: 24rpx 24rpx 24rpx 30rpx; }

.post-header { display: flex; align-items: center; gap: 14rpx; }
.avatar { width: 76rpx; height: 76rpx; border-radius: 50%; border: 4rpx solid #FFFFFF; box-shadow: 0 6rpx 16rpx rgba(39,95,70,0.12); flex-shrink: 0; }
.post-meta { flex: 1; min-width: 0; }
.post-author { font-weight: 700; font-size: 28rpx; color: #173B2F; display: block; }
.verified { font-size: 24rpx; }
.post-time { font-size: 22rpx; color: #8DA49A; display: block; margin-top: 4rpx; }

.level-badge { flex-shrink: 0; padding: 6rpx 18rpx; border-radius: 20rpx; font-size: 22rpx; font-weight: 600; }
.lvl-safe { background: #E8F8F0; color: #52B788; }
.lvl-warn { background: #FFF8EC; color: #E5A63A; }
.lvl-danger { background: #FFEAED; color: #D96666; }

.post-title { font-size: 32rpx; font-weight: 700; color: #173B2F; margin: 18rpx 0 10rpx; display: block; line-height: 1.4; }
.post-excerpt { font-size: 27rpx; color: #5D7168; line-height: 1.7; display: block; }
.post-excerpt.clamp { display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.expand-btn { font-size: 26rpx; color: #4FAE84; margin-top: 6rpx; display: block; }

.post-images { margin-top: 16rpx; }
.img-grid { display: flex; gap: 8rpx; }
.img-grid.grid-1 .post-img { width: 100%; height: 360rpx; }
.img-grid.grid-2 .post-img { width: calc(50% - 4rpx); height: 220rpx; }
.img-grid.grid-3 .post-img { width: calc(33.33% - 6rpx); height: 200rpx; }
.post-img { border-radius: 16rpx; }
.img-more { font-size: 24rpx; color: #8DA49A; margin-top: 8rpx; display: block; }

.post-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 18rpx; padding-top: 18rpx; border-top: 1rpx solid #E2F0E7; }
.footer-tag { font-size: 22rpx; color: #2F8F68; background: #E7F7EC; padding: 7rpx 18rpx; border-radius: 20rpx; font-weight: 600; }
.footer-loc { font-size: 22rpx; color: #B2BEC3; margin-left: 8rpx; }
.post-stats { display: flex; gap: 24rpx; }
.stat-item { display: flex; align-items: center; gap: 4rpx; font-size: 23rpx; color: #8DA49A; }
.stat-icon { font-size: 22rpx; }

/* Empty & Load */
.empty { text-align: center; padding: 160rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #8DA49A; }
.load-more { text-align: center; padding: 28rpx 0 40rpx; }
.load-more-text { font-size: 26rpx; color: #8DA49A; }

/* TabBar */
.tab-bar { position: fixed; bottom: 0; left: 0; right: 0; height: 100rpx; display: flex; justify-content: space-around; align-items: center; background: #fff; border-top: 1rpx solid #eee; padding-bottom: env(safe-area-inset-bottom); z-index: 999; }
.t-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2rpx; }
.t-icon { font-size: 38rpx; }
.t-text { font-size: 20rpx; color: #999; }
.t-text.on { color: #4FAE84; font-weight: 600; }
.t-plus { width: 90rpx; height: 90rpx; border-radius: 50%; background: linear-gradient(135deg, #4FAE84, #7EC8A0); display: flex; align-items: center; justify-content: center; border: 5rpx solid #fff; box-shadow: 0 4rpx 16rpx rgba(79,174,132,0.3); margin-top: -16rpx; }
.t-plus-text { font-size: 48rpx; color: #fff; font-weight: bold; line-height: 1; }
.t-icon-wrap { position: relative; }
.t-badge { position: absolute; top: -8rpx; right: -16rpx; min-width: 32rpx; height: 32rpx; background: #FF4757; color: #fff; border-radius: 16rpx; font-size: 18rpx; text-align: center; line-height: 32rpx; padding: 0 6rpx; }
</style>
