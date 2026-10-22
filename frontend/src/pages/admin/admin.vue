<template>
  <view class="page">
    <view class="stats-row">
      <view class="stat-card"><text class="sv">{{ stats.userCount }}</text><text class="sl">总用户</text></view>
      <view class="stat-card"><text class="sv">{{ stats.postCount }}</text><text class="sl">总帖子</text></view>
      <view class="stat-card"><text class="sv">{{ stats.pendingPosts }}</text><text class="sl">待审核</text></view>
      <view class="stat-card"><text class="sv">{{ stats.pendingReports }}</text><text class="sl">待处理举报</text></view>
    </view>

    <view class="tabs">
      <view class="tab-item" :class="{ active: tab === 0 }" @tap="tab = 0">用户</view>
      <view class="tab-item" :class="{ active: tab === 1 }" @tap="tab = 1">审核</view>
      <view class="tab-item" :class="{ active: tab === 2 }" @tap="tab = 2">举报</view>
      <view class="tab-item" :class="{ active: tab === 3 }" @tap="tab = 3">分类</view>
    </view>

    <view v-if="tab === 0">
      <view v-for="u in users" :key="u.id" class="list-item">
        <view class="li-info">
          <text class="li-name">{{ u.nickname || u.username }}</text>
          <text class="li-meta">@{{ u.username }} · {{ u.role }}</text>
        </view>
        <button v-if="u.role !== 'ADMIN'" size="mini" :class="u.status === 1 ? 'btn-danger' : 'btn-success'" @tap="toggleUser(u.id)">
          {{ u.status === 1 ? '禁用' : '启用' }}
        </button>
      </view>
    </view>

    <view v-if="tab === 1">
      <view v-if="pendingPosts.length === 0" class="empty"><text>暂无待审核内容</text></view>
      <view v-for="p in pendingPosts" :key="p.id" class="list-item">
        <view class="li-info">
          <text class="li-name">{{ p.title }}</text>
          <text class="li-meta">{{ p.nickname }} · {{ p.createTime }}</text>
        </view>
        <view class="btn-row">
          <button size="mini" class="btn-success" @tap="audit(p.id, 1)">通过</button>
          <button size="mini" class="btn-danger" @tap="audit(p.id, 2)">驳回</button>
        </view>
      </view>
    </view>

    <view v-if="tab === 2">
      <view v-if="reports.length === 0" class="empty"><text>暂无举报</text></view>
      <view v-for="r in reports" :key="r.id" class="list-item">
        <view class="li-info">
          <text class="li-name">{{ r.targetType }} #{{ r.targetId }}</text>
          <text class="li-meta">{{ r.reason }}</text>
        </view>
        <view v-if="r.status === 'PENDING'" class="btn-row">
          <button size="mini" class="btn-success" @tap="handleR(r.id, 'PROCESSED', '已处理')">通过</button>
          <button size="mini" class="btn-danger" @tap="handleR(r.id, 'DISMISSED', '已驳回')">驳回</button>
        </view>
        <text v-else class="li-status">{{ r.status }}</text>
      </view>
    </view>

    <view v-if="tab === 3">
      <view class="add-cat">
        <input class="cat-input" v-model="newCatName" placeholder="新分类名称" />
        <button size="mini" class="btn-primary" @tap="addCat">添加</button>
      </view>
      <view v-for="c in categories" :key="c.id" class="list-item">
        <view class="li-info"><text class="li-name">{{ c.name }}</text></view>
        <button size="mini" class="btn-danger" @tap="delCat(c.id)">删除</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { adminApi } from '../../api/admin'

const tab = ref(0)
const stats = ref({ userCount: 0, postCount: 0, pendingPosts: 0, pendingReports: 0 })
const users = ref([])
const pendingPosts = ref([])
const reports = ref([])
const categories = ref([])
const newCatName = ref('')

onMounted(() => { fetchStats(); loadTabData() })

watch(tab, () => loadTabData())

async function fetchStats() {
  try { const r = await adminApi.getStats(); stats.value = r.data } catch (e) {}
}

async function loadTabData() {
  try {
    if (tab.value === 0) { const r = await adminApi.getUsers(); users.value = r.data.records || [] }
    else if (tab.value === 1) { const r = await adminApi.getPendingPosts(); pendingPosts.value = r.data.records || [] }
    else if (tab.value === 2) { const r = await adminApi.getReports(); reports.value = r.data.records || [] }
    else { const r = await adminApi.getCategories(); categories.value = r.data || [] }
  } catch (e) {}
}

async function toggleUser(id) { await adminApi.toggleUserStatus(id); uni.showToast({ title: '操作成功' }); loadTabData() }
async function audit(id, status) { await adminApi.auditPost(id, status); uni.showToast({ title: status === 1 ? '已通过' : '已驳回' }); loadTabData(); fetchStats() }
async function handleR(id, result, msg) { await adminApi.handleReport(id, result, msg); uni.showToast({ title: '已处理' }); loadTabData(); fetchStats() }
async function addCat() { if (!newCatName.value.trim()) return; await adminApi.addCategory({ name: newCatName.value, sort: 0 }); uni.showToast({ title: '已添加' }); newCatName.value = ''; loadTabData() }
async function delCat(id) { await adminApi.deleteCategory(id); uni.showToast({ title: '已删除' }); loadTabData() }
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 40rpx; }
.stats-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 16rpx; }
.stat-card { flex: 1; min-width: 45%; background: #fff; border-radius: 12rpx; padding: 24rpx; text-align: center; }
.sv { display: block; font-size: 40rpx; font-weight: bold; color: #4FAE84; }
.sl { display: block; font-size: 22rpx; color: #999; margin-top: 4rpx; }
.tabs { display: flex; background: #fff; border-radius: 12rpx; margin-bottom: 16rpx; }
.tab-item { flex: 1; text-align: center; padding: 20rpx 0; font-size: 26rpx; color: #666; }
.tab-item.active { color: #4FAE84; font-weight: bold; border-bottom: 3rpx solid #4FAE84; }
.list-item { display: flex; align-items: center; background: #fff; padding: 20rpx; border-radius: 12rpx; margin-bottom: 8rpx; }
.li-info { flex: 1; }
.li-name { font-size: 28rpx; font-weight: 600; display: block; }
.li-meta { font-size: 22rpx; color: #999; }
.li-status { font-size: 22rpx; color: #999; }
.btn-row { display: flex; gap: 8rpx; flex-shrink: 0; }
.btn-danger { background: #f56c6c; color: #fff; border: none; }
.btn-success { background: #67c23a; color: #fff; border: none; }
.btn-primary { background: #4FAE84; color: #fff; border: none; }
.add-cat { display: flex; gap: 12rpx; margin-bottom: 16rpx; }
.cat-input { flex: 1; height: 72rpx; background: #fff; border-radius: 12rpx; padding: 0 20rpx; font-size: 26rpx; }
.empty { text-align: center; padding: 60rpx 0; font-size: 28rpx; color: #999; }
</style>
