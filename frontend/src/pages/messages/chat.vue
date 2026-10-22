<template>
  <view class="page">
    <view class="chat-list" id="chat-list">
      <view v-for="m in msgs" :key="m.id" class="msg-row" :class="{ mine: m.fromUserId === myId }">
        <image v-if="m.fromUserId !== myId" class="msg-avatar" :src="friendAvatar" mode="aspectFill" />
        <view class="msg-bubble" :class="{ mine: m.fromUserId === myId }">
          <text class="msg-text">{{ m.content }}</text>
        </view>
        <image v-if="m.fromUserId === myId" class="msg-avatar" :src="myAvatar" mode="aspectFill" />
      </view>
    </view>
    <view class="input-bar">
      <input class="msg-input" v-model="text" placeholder="输入消息..." confirm-type="send" @confirm="doSend" />
      <button class="send-btn" @tap="doSend" :disabled="!text.trim()">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { messageApi } from '../../api/message'

const text = ref('')
const msgs = ref([])
const user = JSON.parse(uni.getStorageSync('user') || '{}')
const myId = user.id || 0
const myAvatar = user.avatar || '/static/default-avatar.svg'
const friendId = ref(0)
const friendAvatar = ref('/static/default-avatar.svg')

onMounted(() => {
  const pages = getCurrentPages()
  const opts = pages[pages.length - 1].options
  friendId.value = parseInt(opts.userId)
  friendAvatar.value = opts.avatar || '/static/default-avatar.svg'
  loadMessages()
  messageApi.markRead(friendId.value)
})

async function loadMessages() {
  try {
    const res = await messageApi.getMessages(friendId.value)
    msgs.value = res.data.records || []
    nextTick(() => {
      // scroll to bottom
      const query = uni.createSelectorQuery()
      query.select('#chat-list').boundingClientRect()
      query.exec(() => {
        uni.pageScrollTo({ scrollTop: 99999, duration: 100 })
      })
    })
  } catch (e) {}
}

async function doSend() {
  if (!text.value.trim()) return
  try {
    await messageApi.send(friendId.value, text.value.trim())
    text.value = ''
    loadMessages()
  } catch (e) {}
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #EEF8F0; }
.chat-list { flex: 1; padding: 20rpx 24rpx; overflow-y: auto; }
.msg-row { display: flex; align-items: flex-start; margin-bottom: 24rpx; gap: 12rpx; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 70%; padding: 18rpx 24rpx; border-radius: 20rpx; background: #fff; font-size: 28rpx; line-height: 1.6; }
.msg-bubble.mine { background: #4FAE84; }
.msg-text { color: #333; }
.msg-bubble.mine .msg-text { color: #fff; }
.msg-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; flex-shrink: 0; }
.input-bar { display: flex; align-items: center; gap: 12rpx; padding: 16rpx 20rpx; background: #fff; border-top: 1rpx solid #eee; padding-bottom: env(safe-area-inset-bottom); }
.msg-input { flex: 1; height: 72rpx; background: #F1F2F6; border-radius: 36rpx; padding: 0 24rpx; font-size: 26rpx; }
.send-btn { height: 72rpx; padding: 0 32rpx; background: #4FAE84; color: #fff; border-radius: 36rpx; font-size: 26rpx; border: none; line-height: 72rpx; }
</style>
