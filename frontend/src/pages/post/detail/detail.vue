<template>
  <view class="page" v-if="post">
    <!-- Post Content -->
    <view class="post-block">
      <view class="author-row">
        <image class="avatar" :src="post.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="author-info">
          <text class="author-name">{{ post.nickname }}</text>
          <text class="post-time">{{ post.createTime }}</text>
        </view>
        <view v-if="post.userId === userId || isAdmin" class="owner-actions">
          <text v-if="post.userId === userId" class="action-link" @tap="editPost">编辑</text>
          <text class="action-link danger" @tap="deletePost">删除</text>
        </view>
        <text v-else class="report-btn" @tap="showReport">举报</text>
      </view>

      <text class="post-title">{{ post.title }}</text>

      <view class="tag-row">
        <text class="tag" :class="'tag-' + levelColor(post.level)">{{ levelText(post.level) }}</text>
        <text class="tag tag-info">{{ post.categoryName }}</text>
        <text class="tag tag-vis">{{ visibilityText(post.visibility) }}</text>
      </view>

      <view v-if="post.topics?.length" class="topic-row">
        <text v-for="t in post.topics" :key="t" class="topic-tag">{{ t }}</text>
      </view>
      <view v-if="post.location" class="location-tag">📍 {{ post.location }}</view>

      <text class="post-content">{{ post.content }}</text>

      <view class="post-images-detail" v-if="post.images?.length">
        <image v-for="(img, i) in post.images" :key="i" :src="img" mode="aspectFill" class="detail-img" @tap="previewImage(i)" />
      </view>

      <view class="action-bar">
        <view class="action-btn" :class="{ active: post.isLiked }" @tap="toggleLike">
          <text>👍 {{ post.likeCount }}</text>
        </view>
        <view class="action-btn" :class="{ active: post.isFavorited }" @tap="toggleFavorite">
          <text>⭐ {{ post.favoriteCount }}</text>
        </view>
        <view class="action-btn">
          <text>👁 {{ post.viewCount }}</text>
        </view>
        <view class="action-btn">
          <text>💬 {{ post.commentCount }}</text>
        </view>
      </view>
    </view>

    <!-- Comments -->
    <view class="comment-block">
      <text class="section-title">评论 ({{ comments.length }})</text>

      <view class="comment-input-row">
        <input class="comment-input" v-model="commentText" :placeholder="replyPlaceholder" />
        <text class="img-upload-btn" @tap="uploadCommentImage">📷</text>
        <button class="send-btn" @tap="submitComment" :disabled="!commentText.trim() && !commentImage">发送</button>
      </view>
      <view v-if="commentImage" class="comment-image-preview">
        <image :src="commentImage" mode="aspectFill" class="cimg-preview" />
        <text class="cimg-del" @tap="commentImage = ''">×</text>
      </view>

      <view v-if="comments.length === 0" class="no-comment">
        <text>暂无评论，来第一个评论吧~</text>
      </view>

      <view v-for="c in comments" :key="c.id" class="comment-item">
        <image class="c-avatar" :src="c.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="c-body">
          <view class="c-header">
            <text class="c-name">{{ c.nickname }}</text>
            <text class="c-time">{{ c.createTime }}</text>
          </view>
          <text class="c-content">{{ c.content }}</text>
          <image v-if="c.image" :src="c.image" mode="widthFix" class="c-image" @tap="previewCommentImage(c.image)" />
          <view class="c-actions">
            <text class="c-action" @tap="likeComment(c)">👍 {{ c.likeCount }}</text>
            <text class="c-action" @tap="replyComment(c)">回复</text>
            <text v-if="c.userId === userId || isAdmin" class="c-action danger" @tap="delComment(c.id)">删除</text>
          </view>
          <!-- Replies -->
          <view v-if="c.replies?.length" class="replies">
            <view v-for="r in c.replies" :key="r.id" class="reply-item">
              <text class="reply-name">{{ r.nickname }}</text>
              <text>：{{ r.content }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Report Modal -->
    <view v-if="reportVisible" class="modal-mask" @tap="reportVisible = false">
      <view class="modal-box" @tap.stop>
        <text class="modal-title">举报内容</text>
        <textarea class="modal-textarea" v-model="reportReason" placeholder="请描述举报原因..." />
        <view class="modal-btns">
          <button class="modal-btn cancel" @tap="reportVisible = false">取消</button>
          <button class="modal-btn confirm" @tap="submitReport">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { postApi } from '../../../api/post'
import { commentApi } from '../../../api/comment'
import { likeApi } from '../../../api/like'
import { favoriteApi } from '../../../api/favorite'
import { reportApi } from '../../../api/report'
import { uploadApi } from '../../../api/upload'

const post = ref(null)
const comments = ref([])
const commentText = ref('')
const replyParentId = ref(null)
const replyPlaceholder = ref('写下你的评论...')
const reportVisible = ref(false)
const reportReason = ref('')
const commentImage = ref('')
const userId = ref(0)
const isAdmin = ref(false)

const levelColors = { 1: 'green', 2: 'orange', 3: 'orange', 4: 'red', 5: 'red' }
const levelLabels = { 1: '⭐', 2: '⭐⭐', 3: '⭐⭐⭐', 4: '⭐⭐⭐⭐', 5: '⭐⭐⭐⭐⭐' }
function levelColor(l) { return levelColors[l] || 'gray' }
function levelText(l) { return levelLabels[l] || '未知' }
function visibilityText(v) { return v === 'PUBLIC' ? '公开' : v === 'FRIENDS' ? '好友可见' : '仅自己' }

onMounted(() => {
  const pages = getCurrentPages()
  const id = pages[pages.length - 1].options.id
  const user = JSON.parse(uni.getStorageSync('user') || '{}')
  userId.value = user.id || 0
  isAdmin.value = user.role === 'ADMIN'
  fetchPost(id)
  fetchComments(id)
})

async function fetchPost(id) {
  try {
    const res = await postApi.getById(id)
    post.value = res.data
  } catch (e) {}
}
async function fetchComments(id) {
  try {
    const res = await commentApi.getByPostId(id)
    comments.value = res.data
  } catch (e) {}
}
async function toggleLike() {
  if (post.value.isLiked) {
    await likeApi.unlikePost(post.value.id); post.value.likeCount--
  } else {
    await likeApi.likePost(post.value.id); post.value.likeCount++
  }
  post.value.isLiked = !post.value.isLiked
}
async function toggleFavorite() {
  if (post.value.isFavorited) {
    await favoriteApi.remove(post.value.id); post.value.favoriteCount--
  } else {
    await favoriteApi.add(post.value.id); post.value.favoriteCount++
  }
  post.value.isFavorited = !post.value.isFavorited
}
async function submitComment() {
  if (!commentText.value.trim() && !commentImage.value) return
  await commentApi.create({ postId: post.value.id, content: commentText.value || '', image: commentImage.value, parentId: replyParentId.value })
  uni.showToast({ title: '评论成功' })
  commentText.value = ''
  commentImage.value = ''
  replyParentId.value = null
  replyPlaceholder.value = '写下你的评论...'
  fetchComments(post.value.id)
}

async function uploadCommentImage() {
  const res = await new Promise(r => uni.chooseImage({ count: 1, sizeType: ['compressed'], success: r, fail: () => r(null) }))
  if (!res || !res.tempFilePaths?.length) return
  uni.showLoading({ title: '上传中...', mask: true })
  try {
    const r = await uploadApi.upload(res.tempFilePaths[0])
    commentImage.value = r.data.url
  } catch (e) {
    uni.showToast({ title: '上传失败，请重试', icon: 'none' })
  } finally { uni.hideLoading() }
}

function previewCommentImage(url) { uni.previewImage({ urls: [url] }) }
function replyComment(c) {
  replyParentId.value = c.id
  replyPlaceholder.value = `回复 @${c.nickname}`
  commentText.value = ''
}
async function likeComment(c) {
  if (c.isLiked) { await commentApi.unlike(c.id); c.likeCount-- }
  else { await commentApi.like(c.id); c.likeCount++ }
  c.isLiked = !c.isLiked
}
async function delComment(id) {
  const res = await new Promise(r => uni.showModal({ title: '确认删除', content: '确定删除这条评论？', success: e => r(e.confirm) }))
  if (!res) return
  await commentApi.delete(id)
  uni.showToast({ title: '已删除' })
  fetchComments(post.value.id)
}
function editPost() { uni.navigateTo({ url: `/pages/post/edit/edit?id=${post.value.id}` }) }
async function deletePost() {
  const res = await new Promise(r => uni.showModal({ title: '确认删除', content: '确定删除这条内容？', success: e => r(e.confirm) }))
  if (!res) return
  await postApi.delete(post.value.id)
  uni.showToast({ title: '已删除' })
  uni.navigateBack()
}
function showReport() { reportVisible.value = true; reportReason.value = '' }
async function submitReport() {
  if (!reportReason.value.trim()) return
  await reportApi.submit({ targetType: 'POST', targetId: post.value.id, reason: reportReason.value })
  uni.showToast({ title: '举报已提交' })
  reportVisible.value = false
}
function previewImage(idx) {
  uni.previewImage({ urls: post.value.images, current: idx })
}
</script>

<style scoped>
.page { padding-bottom: 40rpx; }
.post-block { background: #fff; padding: 24rpx; }
.author-row { display: flex; align-items: center; gap: 16rpx; }
.avatar { width: 80rpx; height: 80rpx; border-radius: 50%; }
.author-info { flex: 1; }
.author-name { font-weight: 600; font-size: 30rpx; display: block; }
.post-time { font-size: 22rpx; color: #999; }
.owner-actions { display: flex; gap: 16rpx; }
.action-link { font-size: 26rpx; color: #4FAE84; }
.action-link.danger { color: #D96666; }
.report-btn { font-size: 24rpx; color: #e6a23c; }
.post-title { font-size: 36rpx; font-weight: bold; display: block; margin: 20rpx 0 16rpx; }
.tag-row { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.tag { padding: 6rpx 20rpx; border-radius: 8rpx; font-size: 22rpx; }
.tag-green { background: #e8f5e9; color: #4caf50; }
.tag-orange { background: #fff3e0; color: #ff9800; }
.tag-red { background: #fce4ec; color: #f44336; }
.tag-gray { background: #f5f5f5; color: #999; }
.tag-info { background: #E7F7EC; color: #4FAE84; }
.tag-vis { background: #e3f2fd; color: #2196f3; }
.topic-row { display: flex; flex-wrap: wrap; gap: 10rpx; margin-bottom: 14rpx; }
.topic-tag { font-size: 23rpx; color: #FF6B35; background: #FFF3E6; padding: 6rpx 16rpx; border-radius: 16rpx; font-weight: 600; }
.location-tag { font-size: 24rpx; color: #4FAE84; background: #E7F7EC; padding: 8rpx 20rpx; border-radius: 20rpx; display: inline-block; margin-bottom: 16rpx; }
.post-content { font-size: 28rpx; line-height: 1.8; color: #333; display: block; white-space: pre-wrap; }
.post-images-detail { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 20rpx; }
.detail-img { width: calc((100% - 16rpx) / 3); height: 200rpx; border-radius: 12rpx; }
.action-bar { display: flex; justify-content: space-around; margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #f0f0f0; }
.action-btn { display: flex; align-items: center; gap: 6rpx; font-size: 26rpx; color: #666; }
.action-btn.active { color: #4FAE84; }
.comment-block { background: #fff; margin-top: 16rpx; padding: 24rpx; }
.section-title { font-size: 30rpx; font-weight: bold; display: block; margin-bottom: 20rpx; }
.comment-input-row { display: flex; gap: 12rpx; margin-bottom: 24rpx; }
.comment-input { flex: 1; height: 72rpx; background: #E2F0E7; border-radius: 36rpx; padding: 0 24rpx; font-size: 26rpx; }
.send-btn { height: 72rpx; padding: 0 28rpx; background: #4FAE84; color: #fff; border-radius: 36rpx; font-size: 26rpx; border: none; line-height: 72rpx; }
.img-upload-btn { font-size: 40rpx; padding: 0 8rpx; }
.comment-image-preview { position: relative; margin-bottom: 16rpx; display: inline-block; }
.cimg-preview { width: 180rpx; height: 180rpx; border-radius: 12rpx; }
.cimg-del { position: absolute; top: -12rpx; right: -12rpx; width: 36rpx; height: 36rpx; background: #D96666; color: #fff; border-radius: 50%; text-align: center; line-height: 36rpx; font-size: 24rpx; }
.c-image { width: 300rpx; border-radius: 12rpx; margin-top: 8rpx; }
.no-comment { text-align: center; padding: 40rpx 0; font-size: 26rpx; color: #999; }
.comment-item { display: flex; gap: 16rpx; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.c-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; flex-shrink: 0; }
.c-body { flex: 1; }
.c-header { display: flex; gap: 16rpx; align-items: center; }
.c-name { font-weight: 600; font-size: 26rpx; }
.c-time { font-size: 22rpx; color: #999; }
.c-content { font-size: 27rpx; line-height: 1.6; display: block; margin: 8rpx 0; }
.c-actions { display: flex; gap: 24rpx; }
.c-action { font-size: 23rpx; color: #999; }
.c-action.danger { color: #D96666; }
.replies { background: #f9f9f9; padding: 16rpx; border-radius: 12rpx; margin-top: 8rpx; }
.reply-item { font-size: 25rpx; line-height: 1.6; padding: 4rpx 0; }
.reply-name { color: #4FAE84; font-weight: 500; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-box { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 32rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 20rpx; }
.modal-textarea { width: 100%; height: 200rpx; background: #E2F0E7; border-radius: 12rpx; padding: 16rpx; font-size: 26rpx; }
.modal-btns { display: flex; gap: 16rpx; margin-top: 24rpx; }
.modal-btn { flex: 1; height: 72rpx; border-radius: 12rpx; font-size: 28rpx; border: none; line-height: 72rpx; }
.modal-btn.cancel { background: #f5f5f5; color: #666; }
.modal-btn.confirm { background: #D96666; color: #fff; }
</style>
