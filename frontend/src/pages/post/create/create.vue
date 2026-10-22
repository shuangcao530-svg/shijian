<template>
  <view class="page">
    <view class="top-bar">
      <text class="back-btn" @tap="goBack">← 返回</text>
      <text class="top-title">发布日常</text>
      <text class="placeholder"></text>
    </view>
    <view class="form-card">
      <input class="input" v-model="form.title" placeholder="请输入标题（必填）" />
      <view class="picker-row" @tap="showCatPicker">
        <text class="picker-label">{{ form.categoryName || '选择分类（必填）' }}</text>
        <text class="picker-arrow">›</text>
      </view>
      <view class="level-row">
        <text class="level-label">星级：</text>
        <view class="stars">
          <text v-for="i in 5" :key="i" class="star" :class="{ active: i <= form.level }" @tap="form.level = i">{{ i <= form.level ? '★' : '☆' }}</text>
        </view>
        <text class="level-text">{{ levelTexts[form.level] }}</text>
      </view>
      <view class="vis-row">
        <text class="vis-label">可见范围：</text>
        <view class="vis-options">
          <view class="vis-item" :class="{ active: form.visibility === 'PUBLIC' }" @tap="form.visibility = 'PUBLIC'">公开</view>
          <view class="vis-item" :class="{ active: form.visibility === 'FRIENDS' }" @tap="form.visibility = 'FRIENDS'">好友</view>
          <view class="vis-item" :class="{ active: form.visibility === 'PRIVATE' }" @tap="form.visibility = 'PRIVATE'">私密</view>
        </view>
      </view>
      <view class="location-row">
        <text class="loc-label">📍 位置：</text>
        <input class="loc-input" v-model="form.location" placeholder="选填，输入或自动获取" />
        <text class="loc-btn" @tap="getLocation">获取</text>
      </view>
      <textarea class="textarea" v-model="form.content" placeholder="描述你的校园日常...（必填）" :maxlength="5000" />
      <view class="upload-row">
        <text class="upload-label">上传图片（最多9张）</text>
        <view class="image-list">
          <view v-for="(img, i) in images" :key="i" class="img-item">
            <image :src="img" mode="aspectFill" class="img-preview" />
            <text class="img-del" @tap="removeImg(i)">×</text>
          </view>
          <view v-if="images.length < 9" class="img-add" @tap="chooseImage">
            <text class="add-icon">+</text>
          </view>
        </view>
      </view>
      <button class="submit-btn" :loading="submitting" @tap="onSubmit">发布</button>
    </view>

    <!-- Category Picker -->
    <view v-if="catVisible" class="picker-mask" @tap="catVisible = false">
      <view class="picker-box" @tap.stop>
        <text class="picker-title">选择分类</text>
        <view v-for="cat in categories" :key="cat.id" class="picker-item" @tap="selectCat(cat)">{{ cat.name }}</view>
        <button class="picker-cancel" @tap="catVisible = false">取消</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { postApi } from '../../../api/post'
import { categoryApi } from '../../../api/category'
import { uploadApi } from '../../../api/upload'

const form = reactive({ title: '', categoryId: null, categoryName: '', level: 3, visibility: 'PUBLIC', content: '', location: '' })
const categories = ref([])
const images = ref([])

function goBack() { uni.reLaunch({ url: '/pages/index/index' }) }

function getLocation() {
  // 优先尝试地图选点（H5 也支持）
  uni.chooseLocation({
    success(res) {
      form.location = res.name || res.address || `${res.latitude}, ${res.longitude}`
    },
    fail() {
      // 回退到纯 GPS
      uni.getLocation({
        type: 'gcj02',
        success(r) {
          form.location = `${r.longitude.toFixed(4)}, ${r.latitude.toFixed(4)}`
        },
        fail() {
          uni.showToast({ title: '请在系统设置中开启定位权限', icon: 'none' })
        }
      })
    }
  })
}
const submitting = ref(false)
const catVisible = ref(false)
const levelTexts = ['', '1星', '2星', '3星', '4星', '5星']

onMounted(async () => {
  const res = await categoryApi.list()
  categories.value = res.data
})

function showCatPicker() { catVisible.value = true }
function selectCat(cat) {
  form.categoryId = cat.id; form.categoryName = cat.name; catVisible.value = false
}

async function chooseImage() {
  const res = await new Promise(r => uni.chooseImage({ count: 9 - images.value.length, success: r, fail: () => r(null) }))
  if (!res) return
  for (const f of res.tempFiles) {
    try {
      uni.showLoading({ title: '上传中...' })
      const uploadRes = await uploadApi.upload(f.path)
      images.value.push(uploadRes.data.url)
      uni.hideLoading()
    } catch (e) { uni.hideLoading() }
  }
}
function removeImg(i) { images.value.splice(i, 1) }

async function onSubmit() {
  if (!form.title) return uni.showToast({ title: '请输入标题', icon: 'none' })
  if (!form.categoryId) return uni.showToast({ title: '请选择分类', icon: 'none' })
  if (!form.content) return uni.showToast({ title: '请输入内容', icon: 'none' })
  submitting.value = true
  try {
    await postApi.create({ title: form.title, categoryId: form.categoryId, level: form.level, visibility: form.visibility, content: form.content, images: images.value, location: form.location })
    uni.showToast({ title: '发布成功！' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 800)
  } catch (e) {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 0 24rpx; }
.back-btn { font-size: 28rpx; color: #4FAE84; }
.top-title { font-size: 32rpx; font-weight: bold; }
.placeholder { width: 80rpx; }
.form-card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.input { height: 88rpx; background: #E2F0E7; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.picker-row { display: flex; justify-content: space-between; align-items: center; height: 88rpx; background: #E2F0E7; border-radius: 12rpx; padding: 0 24rpx; margin-bottom: 20rpx; }
.picker-label { font-size: 28rpx; color: #8DA49A; }
.picker-label:not(:empty):not([style*="c0c4cc"]) { color: #333; }
.picker-arrow { font-size: 36rpx; color: #999; }
.level-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 20rpx; padding: 0 8rpx; }
.level-label { font-size: 26rpx; color: #666; }
.stars { display: flex; gap: 8rpx; }
.star { font-size: 40rpx; color: #ddd; }
.star.active { color: #ff9800; }
.level-text { font-size: 24rpx; color: #ff9800; margin-left: 8rpx; }
.vis-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 20rpx; }
.vis-label { font-size: 26rpx; color: #666; }
.vis-options { display: flex; gap: 12rpx; }
.vis-item { padding: 8rpx 24rpx; border-radius: 32rpx; font-size: 24rpx; background: #E2F0E7; color: #666; }
.vis-item.active { background: #4FAE84; color: #fff; }
.location-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 20rpx; }
.loc-label { font-size: 26rpx; color: #666; flex-shrink: 0; }
.loc-input { flex: 1; height: 72rpx; background: #E2F0E7; border-radius: 12rpx; padding: 0 16rpx; font-size: 24rpx; }
.loc-btn { font-size: 24rpx; color: #4FAE84; padding: 8rpx 16rpx; background: #E7F7EC; border-radius: 12rpx; }
.textarea { width: 100%; height: 300rpx; background: #E2F0E7; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.upload-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 12rpx; }
.image-list { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 24rpx; }
.img-item { position: relative; width: 160rpx; height: 160rpx; }
.img-preview { width: 100%; height: 100%; border-radius: 12rpx; }
.img-del { position: absolute; top: -12rpx; right: -12rpx; width: 40rpx; height: 40rpx; background: #D96666; color: #fff; border-radius: 50%; text-align: center; line-height: 40rpx; font-size: 28rpx; }
.img-add { width: 160rpx; height: 160rpx; border: 2rpx dashed #ddd; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
.add-icon { font-size: 60rpx; color: #ccc; }
.submit-btn { width: 100%; height: 88rpx; background: linear-gradient(135deg, #4FAE84, #9AD9B8); color: #fff; border-radius: 16rpx; font-size: 32rpx; font-weight: bold; border: none; margin-top: 16rpx; }
.picker-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 999; }
.picker-box { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx; }
.picker-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.picker-item { padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; }
.picker-cancel { margin-top: 24rpx; height: 80rpx; background: #f5f5f5; border: none; border-radius: 12rpx; font-size: 28rpx; }
</style>
