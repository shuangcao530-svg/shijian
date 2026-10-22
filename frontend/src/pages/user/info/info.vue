<template>
  <view class="page" v-if="profile">
    <view class="user-header">
      <image class="u-avatar" :src="profile.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
      <text class="u-name">{{ profile.nickname || profile.username }}</text>
      <text class="u-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</text>
      <view class="u-actions">
        <button v-if="!profile.isFriend && profile.id !== myId" class="add-friend-btn" @tap="addFriend">添加好友</button>
        <view v-if="profile.isFriend" class="friend-actions">
          <text class="is-friend-tag">已是好友</text>
          <button class="chat-btn" @tap="goChat">发消息</button>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">TA的日常记录</text>
      <view v-if="posts.length === 0" class="empty"><text>暂无内容</text></view>
      <view v-for="post in posts" :key="post.id" class="post-item" @tap="goDetail(post.id)">
        <view class="p-top">
          <text class="p-title">{{ post.title }}</text>
          <text class="p-tag">{{ post.categoryName }}</text>
        </view>
        <text class="p-excerpt">{{ post.content?.substring(0, 80) }}</text>
        <view class="p-stats">
          <text>👁 {{ post.viewCount }}</text>
          <text>👍 {{ post.likeCount }}</text>
          <text>💬 {{ post.commentCount }}</text>
          <text class="p-date">{{ post.createTime }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../../api/user'
import { postApi } from '../../../api/post'
import { friendApi } from '../../../api/friend'

const profile = ref(null)
const posts = ref([])
const myId = JSON.parse(uni.getStorageSync('user') || '{}').id || 0

onMounted(async () => {
  const userId = getCurrentPages()[getCurrentPages().length - 1].options.id
  const [uRes, pRes] = await Promise.all([userApi.getUserById(userId), postApi.userPosts(userId)])
  profile.value = uRes.data
  posts.value = pRes.data.records
})

function addFriend() {
  friendApi.sendRequest(profile.value.id, '')
  uni.showToast({ title: '好友申请已发送' })
}
function goChat() {
  uni.navigateTo({ url: `/pages/messages/chat?userId=${profile.value.id}&avatar=${encodeURIComponent(profile.value.avatar || '')}` })
}
function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
</script>

<style scoped>
.page { padding-bottom: 40rpx; }
.user-header { background: linear-gradient(135deg, #4FAE84, #9AD9B8); padding: 48rpx 32rpx 32rpx; text-align: center; }
.u-avatar { width: 140rpx; height: 140rpx; border-radius: 50%; border: 4rpx solid rgba(255,255,255,0.5); }
.u-name { display: block; font-size: 36rpx; font-weight: bold; color: #fff; margin-top: 16rpx; }
.u-bio { display: block; font-size: 24rpx; color: rgba(255,255,255,0.7); margin-top: 8rpx; }
.u-actions { margin-top: 20rpx; }
.add-friend-btn { width: 300rpx; height: 64rpx; background: #fff; color: #4FAE84; border-radius: 32rpx; font-size: 26rpx; border: none; line-height: 64rpx; }
.is-friend-tag { display: inline-block; padding: 8rpx 32rpx; background: rgba(255,255,255,0.3); color: #fff; border-radius: 32rpx; font-size: 26rpx; }
.friend-actions { display: flex; align-items: center; gap: 16rpx; }
.chat-btn { height: 56rpx; padding: 0 24rpx; background: #fff; color: #4FAE84; border-radius: 28rpx; font-size: 24rpx; border: none; line-height: 56rpx; }
.section { background: #fff; margin: 16rpx 24rpx; border-radius: 16rpx; padding: 24rpx; }
.section-title { font-size: 30rpx; font-weight: bold; display: block; margin-bottom: 16rpx; }
.post-item { padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.post-item:last-child { border-bottom: none; }
.p-top { display: flex; justify-content: space-between; align-items: center; }
.p-title { font-size: 28rpx; font-weight: 600; flex: 1; }
.p-tag { font-size: 20rpx; color: #4FAE84; background: #E7F7EC; padding: 4rpx 12rpx; border-radius: 6rpx; }
.p-excerpt { font-size: 25rpx; color: #666; margin: 8rpx 0; display: block; }
.p-stats { display: flex; gap: 20rpx; font-size: 22rpx; color: #999; }
.p-date { margin-left: auto; }
.empty { text-align: center; padding: 40rpx; font-size: 26rpx; color: #999; }
</style>
