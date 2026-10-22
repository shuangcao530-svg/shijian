<template>
  <view class="page">
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索校园日常内容..." confirm-type="search" @confirm="doSearch" />
      <button class="search-btn" @tap="doSearch">搜索</button>
    </view>

    <!-- Hot Search -->
    <view v-if="!searched" class="section">
      <view class="section-header">
        <text class="section-title">🔥 热门搜索</text>
      </view>
      <view class="hot-tags">
        <text v-for="(tag, i) in hotTags" :key="i" class="hot-tag" @tap="quickSearch(tag)">
          {{ tag }}
        </text>
      </view>
    </view>

    <!-- Search History -->
    <view v-if="!searched && history.length > 0" class="section">
      <view class="section-header">
        <text class="section-title">🕐 搜索历史</text>
        <text class="clear-btn" @tap="clearHistory">清空</text>
      </view>
      <view class="history-tags">
        <text v-for="(h, i) in history" :key="i" class="history-tag" @tap="quickSearch(h)">
          {{ h }}
        </text>
      </view>
    </view>

    <!-- Results -->
    <view v-if="searched">
      <view class="result-header">
        <text class="result-count">找到 {{ posts.length }} 条结果</text>
      </view>

      <view v-if="posts.length === 0" class="empty">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">没有找到相关内容</text>
      </view>

      <view v-for="post in posts" :key="post.id" class="post-item" @tap="goDetail(post.id)">
        <view class="p-header">
          <image class="p-avatar" :src="post.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" />
          <view class="p-meta">
            <text class="p-author">{{ post.nickname }}</text>
            <text class="p-time">{{ post.createTime }}</text>
          </view>
        </view>
        <text class="p-title" v-html="highlight(post.title)"></text>
        <text class="p-excerpt" v-html="highlight((post.content || '').substring(0, 120))"></text>
        <view v-if="post.topics?.length" class="p-topics">
          <text v-for="t in post.topics" :key="t" class="p-topic-tag">{{ t }}</text>
        </view>
        <view class="p-footer">
          <text class="p-cat">{{ post.categoryName }}</text>
          <view class="p-stats">
            <text>👁 {{ post.viewCount }}</text>
            <text>👍 {{ post.likeCount }}</text>
            <text>💬 {{ post.commentCount }}</text>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="load-more" @tap="loadMore">
        <text class="load-text">{{ loading ? '加载中...' : '加载更多' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { postApi } from '../../api/post'

const keyword = ref('')
const posts = ref([])
const searched = ref(false)
const loading = ref(false)
const page = ref(1)
const hasMore = ref(false)

const hotTags = ['校园生活', '美食分享', '学习经验', '社团活动', '期末复习', '新生攻略', '校园风景', '兼职实习']
const HISTORY_KEY = 'search_history'

function getHistory() {
  try {
    return JSON.parse(uni.getStorageSync(HISTORY_KEY) || '[]')
  } catch (e) { return [] }
}
const history = ref(getHistory())

function saveHistory(kw) {
  let list = getHistory().filter(h => h !== kw)
  list.unshift(kw)
  if (list.length > 10) list = list.slice(0, 10)
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(list))
  history.value = list
}

function clearHistory() {
  uni.removeStorageSync(HISTORY_KEY)
  history.value = []
}

function quickSearch(tag) {
  keyword.value = tag
  doSearch()
}

function highlight(text) {
  if (!keyword.value) return text
  const kw = keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${kw})`, 'gi'), '<span style="color:#FF6B35;font-weight:600;">$1</span>')
}

async function doSearch() {
  if (!keyword.value.trim()) return
  saveHistory(keyword.value.trim())
  searched.value = true
  page.value = 1
  posts.value = []
  await loadData()
}

async function loadData() {
  if (loading.value) return
  loading.value = true
  try {
    const res = await postApi.page({ keyword: keyword.value, page: page.value, size: 10 })
    posts.value = [...posts.value, ...(res.data.records || [])]
    hasMore.value = res.data.records?.length >= 10
    page.value++
  } finally { loading.value = false }
}

function loadMore() { loadData() }
function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #EEF8F0; }
.search-bar { display: flex; gap: 12rpx; margin-bottom: 24rpx; }
.search-input { flex: 1; height: 72rpx; background: #fff; border-radius: 36rpx; padding: 0 28rpx; font-size: 26rpx; border: 1rpx solid #E0E8E3; }
.search-btn { width: 120rpx; height: 72rpx; background: linear-gradient(135deg, #4FAE84, #66C99A); color: #fff; border-radius: 36rpx; font-size: 26rpx; border: none; line-height: 72rpx; text-align: center; }

.section { margin-bottom: 28rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-size: 28rpx; font-weight: 700; color: #173B2F; }
.clear-btn { font-size: 24rpx; color: #8DA49A; }

.hot-tags { display: flex; flex-wrap: wrap; gap: 14rpx; }
.hot-tag { padding: 12rpx 22rpx; background: linear-gradient(135deg, #FFF3E6, #FFE8D6); border-radius: 24rpx; font-size: 24rpx; color: #FF6B35; font-weight: 600; }

.history-tags { display: flex; flex-wrap: wrap; gap: 14rpx; }
.history-tag { padding: 10rpx 22rpx; background: #F0F2F5; border-radius: 24rpx; font-size: 24rpx; color: #5D7168; }

.result-header { margin-bottom: 16rpx; }
.result-count { font-size: 25rpx; color: #8DA49A; }

.post-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; }
.p-header { display: flex; align-items: center; gap: 12rpx; }
.p-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; }
.p-meta { flex: 1; }
.p-author { font-weight: 600; font-size: 26rpx; display: block; }
.p-time { font-size: 22rpx; color: #999; }
.p-title { font-size: 30rpx; font-weight: bold; margin: 12rpx 0 6rpx; display: block; }
.p-excerpt { font-size: 25rpx; color: #666; display: block; }
.p-topics { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 8rpx; }
.p-topic-tag { font-size: 22rpx; color: #4FAE84; background: #E8F5EC; padding: 4rpx 12rpx; border-radius: 12rpx; }
.p-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12rpx; }
.p-cat { font-size: 22rpx; color: #4FAE84; background: #E7F7EC; padding: 4rpx 12rpx; border-radius: 6rpx; }
.p-stats { display: flex; gap: 16rpx; font-size: 22rpx; color: #999; }

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #8DA49A; }

.load-more { text-align: center; padding: 24rpx; }
.load-text { font-size: 26rpx; color: #4FAE84; }
</style>
