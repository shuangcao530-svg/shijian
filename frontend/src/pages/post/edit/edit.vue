<template>
  <view class="page" v-if="loaded">
    <view class="form-card">
      <input class="input" v-model="form.title" placeholder="标题" />
      <view class="picker-row" @tap="showCatPicker">
        <text class="picker-label">{{ form.categoryName || '选择分类' }}</text>
        <text class="picker-arrow">›</text>
      </view>
      <view class="level-row">
        <text class="level-label">星级：</text>
        <view class="stars">
          <text v-for="i in 5" :key="i" class="star" :class="{ active: i <= form.level }" @tap="form.level = i">{{ i <= form.level ? '★' : '☆' }}</text>
        </view>
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
        <input class="loc-input" v-model="form.location" placeholder="选填" />
      </view>
      <textarea class="textarea" v-model="form.content" placeholder="内容" :maxlength="5000" />
      <view class="upload-row">
        <text class="upload-label">图片（最多9张）</text>
        <view class="image-list">
          <view v-for="(img, i) in images" :key="i" class="img-item">
            <image :src="img" mode="aspectFill" class="img-preview" @tap="previewImg(i)" />
            <text class="img-del" @tap="removeImg(i)">×</text>
          </view>
          <view v-if="images.length < 9" class="img-add" @tap="chooseImage">
            <text class="add-icon">+</text>
          </view>
        </view>
      </view>
      <button class="submit-btn" :loading="submitting" @tap="onSubmit">保存修改</button>
    </view>

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

const loaded = ref(false)
const categories = ref([])
const submitting = ref(false)
const catVisible = ref(false)
const images = ref([])
const form = reactive({ title: '', categoryId: null, categoryName: '', level: 3, visibility: 'PUBLIC', content: '', location: '' })
let postId = null

onMounted(() => {
  postId = getCurrentPages()[getCurrentPages().length - 1].options.id
  loadData()
})

async function loadData() {
  const [postRes, catRes] = await Promise.all([postApi.getById(postId), categoryApi.list()])
  const p = postRes.data
  categories.value = catRes.data
  form.title = p.title; form.categoryId = p.categoryId; form.categoryName = p.categoryName
  form.level = p.level; form.visibility = p.visibility; form.content = p.content; form.location = p.location || ''
  images.value = p.images || []
  loaded.value = true
}

function showCatPicker() { catVisible.value = true }
function selectCat(cat) { form.categoryId = cat.id; form.categoryName = cat.name; catVisible.value = false }

async function onSubmit() {
  if (!form.title || !form.categoryId || !form.content) return uni.showToast({ title: '请完善信息', icon: 'none' })
  submitting.value = true
  try {
    await postApi.update(postId, { title: form.title, categoryId: form.categoryId, level: form.level, visibility: form.visibility, content: form.content, images: images.value, location: form.location })
    uni.showToast({ title: '修改成功' })
    uni.navigateBack()
  } catch (e) {} finally { submitting.value = false }
}

async function chooseImage() {
  const res = await new Promise(r => uni.chooseImage({ count: 9 - images.value.length, sizeType: ['compressed'], success: r, fail: () => r(null) }))
  if (!res || !res.tempFilePaths?.length) return
  uni.showLoading({ title: '上传中...', mask: true })
  for (const path of res.tempFilePaths) {
    try {
      const uploadRes = await uploadApi.upload(path)
      images.value.push(uploadRes.data.url)
    } catch (e) {}
  }
  uni.hideLoading()
}

function removeImg(i) { images.value.splice(i, 1) }

function previewImg(i) { uni.previewImage({ urls: images.value, current: i }) }
</script>

<style scoped>
.page { padding: 24rpx; }
.form-card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.input { height: 88rpx; background: #F1F2F6; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.picker-row { display: flex; justify-content: space-between; align-items: center; height: 88rpx; background: #F1F2F6; border-radius: 12rpx; padding: 0 24rpx; margin-bottom: 20rpx; }
.picker-label { font-size: 28rpx; color: #333; }
.picker-arrow { font-size: 36rpx; color: #999; }
.level-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 20rpx; }
.level-label { font-size: 26rpx; color: #666; }
.stars { display: flex; gap: 8rpx; }
.star { font-size: 40rpx; color: #ddd; }
.star.active { color: #ff9800; }
.vis-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 20rpx; }
.vis-label { font-size: 26rpx; color: #666; }
.vis-options { display: flex; gap: 12rpx; }
.vis-item { padding: 8rpx 24rpx; border-radius: 32rpx; font-size: 24rpx; background: #F1F2F6; color: #666; }
.vis-item.active { background: #4FAE84; color: #fff; }
.location-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 20rpx; }
.loc-label { font-size: 26rpx; color: #666; flex-shrink: 0; }
.loc-input { flex: 1; height: 72rpx; background: #F1F2F6; border-radius: 12rpx; padding: 0 16rpx; font-size: 24rpx; }
.textarea { width: 100%; height: 300rpx; background: #F1F2F6; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.upload-row { margin-bottom: 24rpx; }
.upload-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 12rpx; }
.image-list { display: flex; flex-wrap: wrap; gap: 12rpx; }
.img-item { position: relative; width: 160rpx; height: 160rpx; }
.img-preview { width: 100%; height: 100%; border-radius: 12rpx; }
.img-del { position: absolute; top: -12rpx; right: -12rpx; width: 40rpx; height: 40rpx; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 40rpx; font-size: 28rpx; }
.img-add { width: 160rpx; height: 160rpx; border: 2rpx dashed #ddd; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
.add-icon { font-size: 60rpx; color: #ccc; }
.submit-btn { width: 100%; height: 88rpx; background: linear-gradient(135deg, #4FAE84, #9AD9B8); color: #fff; border-radius: 16rpx; font-size: 32rpx; font-weight: bold; border: none; }
.picker-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 999; }
.picker-box { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx; }
.picker-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.picker-item { padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; }
.picker-cancel { margin-top: 24rpx; height: 80rpx; background: #f5f5f5; border: none; border-radius: 12rpx; font-size: 28rpx; }
</style>
