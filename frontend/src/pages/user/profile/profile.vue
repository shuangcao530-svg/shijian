<template>
  <view class="page">
    <view class="user-header">
      <image class="u-avatar" :src="user?.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
      <text class="u-name">{{ user?.nickname || user?.username }}</text>
      <view class="u-level-row">
        <text class="u-badge">{{ badgeName }}</text>
        <text class="u-level">Lv.{{ userLevel }}</text>
        <text class="u-points">{{ userPoints }} 积分</text>
      </view>
      <view class="checkin-btn" :class="{ checked: hasCheckedIn }" @tap="doCheckin">
        <text>📅 {{ hasCheckedIn ? '今日已签到' : '签到领积分' }}</text>
      </view>
      <text class="u-bio">{{ user?.bio || '这个人很懒，什么都没写...' }}</text>
      <view class="u-stats">
        <view class="u-stat-item" @tap="goMyPosts"><text class="u-stat-num">{{ myPostCount }}</text><text class="u-stat-label">帖子</text></view>
        <view class="u-stat-item" @tap="goFavorites"><text class="u-stat-num">{{ favCount }}</text><text class="u-stat-label">收藏</text></view>
        <view class="u-stat-item" @tap="goFriends"><text class="u-stat-num">{{ friendCount }}</text><text class="u-stat-label">好友</text></view>
      </view>
    </view>

    <view class="menu-card">
      <view class="menu-item" @tap="goMyPosts">
        <text class="menu-icon">📝</text><text class="menu-label">我的帖子</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goFavorites">
        <text class="menu-icon">⭐</text><text class="menu-label">我的收藏</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goFriends">
        <text class="menu-icon">👥</text><text class="menu-label">好友管理</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTopics">
        <text class="menu-icon">🏷️</text><text class="menu-label">话题广场</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goRank">
        <text class="menu-icon">🏆</text><text class="menu-label">积分排行</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="editProfile">
        <text class="menu-icon">✏️</text><text class="menu-label">编辑资料</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="changePassword">
        <text class="menu-icon">🔒</text><text class="menu-label">修改密码</text><text class="menu-arrow">›</text>
      </view>
      <view v-if="isAdmin" class="menu-item" @tap="goAdmin">
        <text class="menu-icon">⚙️</text><text class="menu-label">管理后台</text><text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="doLogout">
        <text class="menu-icon">🚪</text><text class="menu-label">退出登录</text><text class="menu-arrow">›</text>
      </view>
    </view>

    <view v-if="editVisible" class="modal-mask" @tap="editVisible = false">
      <view class="modal-box" @tap.stop>
        <text class="modal-title">编辑资料</text>
        <view class="avatar-upload" @tap="changeAvatar">
          <image class="avatar-preview" :src="editForm.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
          <text class="avatar-tip">点击更换头像</text>
        </view>
        <input class="m-input" v-model="editForm.nickname" placeholder="昵称" />
        <input class="m-input" v-model="editForm.phone" placeholder="手机号" />
        <input class="m-input" v-model="editForm.email" placeholder="邮箱" />
        <view class="m-gender">
          <text class="m-label">性别：</text>
          <view class="gender-opt" :class="{ active: editForm.gender === 0 }" @tap="editForm.gender = 0">未知</view>
          <view class="gender-opt" :class="{ active: editForm.gender === 1 }" @tap="editForm.gender = 1">男</view>
          <view class="gender-opt" :class="{ active: editForm.gender === 2 }" @tap="editForm.gender = 2">女</view>
        </view>
        <textarea class="m-textarea" v-model="editForm.bio" placeholder="个人简介" />
        <view class="m-btns">
          <button class="m-btn cancel" @tap="editVisible = false">取消</button>
          <button class="m-btn confirm" @tap="saveProfile">保存</button>
        </view>
      </view>
    </view>

    <view v-if="pwVisible" class="modal-mask" @tap="pwVisible = false">
      <view class="modal-box" @tap.stop>
        <text class="modal-title">修改密码</text>
        <input class="m-input" v-model="pwForm.oldPassword" type="password" placeholder="旧密码" />
        <input class="m-input" v-model="pwForm.newPassword" type="password" placeholder="新密码" />
        <input class="m-input" v-model="pwForm.confirmPassword" type="password" placeholder="确认密码" />
        <view class="m-btns">
          <button class="m-btn cancel" @tap="pwVisible = false">取消</button>
          <button class="m-btn confirm" @tap="doChangePw">确认</button>
        </view>
      </view>
    </view>
    <view class="tab-bar">
      <view class="t-item" @tap="goTab(0, '/pages/index/index')"><text class="t-icon">🏠</text><text class="t-text" :class="{ on: tabIdx===0 }">首页</text></view>
      <view class="t-item" @tap="goTab(1, '/pages/moments/moments')"><text class="t-icon">🌿</text><text class="t-text" :class="{ on: tabIdx===1 }">好友圈</text></view>
      <view class="t-item" @tap="goTab(2, '/pages/post/create/create')"><view class="t-plus"><text class="t-plus-text">＋</text></view></view>
      <view class="t-item" @tap="goTab(3, '/pages/messages/messages')"><text class="t-icon">💬</text><text class="t-text" :class="{ on: tabIdx===3 }">消息</text></view>
      <view class="t-item" @tap="goTab(4, '/pages/user/profile/profile')"><text class="t-icon">👤</text><text class="t-text" :class="{ on: tabIdx===4 }">我的</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../../store/user'
import { userApi } from '../../../api/user'
import { postApi } from '../../../api/post'
import { uploadApi } from '../../../api/upload'
import { favoriteApi } from '../../../api/favorite'
import { friendApi } from '../../../api/friend'
import { pointApi } from '../../../api/point'

const userStore = useUserStore()
const user = computed(() => userStore.user)
const isAdmin = computed(() => userStore.isAdmin)
const myPostCount = ref(0)
const favCount = ref(0)
const friendCount = ref(0)
const userPoints = ref(0)
const userLevel = ref(1)
const badgeName = ref('校园萌新')
const hasCheckedIn = ref(false)
const editVisible = ref(false)
const pwVisible = ref(false)
const editForm = ref({ nickname: '', phone: '', email: '', gender: 0, bio: '', avatar: '' })
const pwForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const tabIdx = ref(4)

onMounted(async () => {
  try {
    const [postsRes, favRes, friendRes] = await Promise.all([
      postApi.myPosts(1, 1), favoriteApi.list(1, 1), friendApi.getFriends(1, 1)
    ])
    myPostCount.value = postsRes.data?.total || 0
    favCount.value = favRes.data?.total || 0
    friendCount.value = friendRes.data?.total || 0
    userStore.fetchProfile()
    loadPoints()
  } catch (e) {}
})

async function loadPoints() {
  try {
    const res = await pointApi.myPoints()
    userPoints.value = res.data?.points || 0
    userLevel.value = res.data?.level || 1
    badgeName.value = res.data?.badge || '校园萌新'
    hasCheckedIn.value = res.data?.hasCheckedInToday || false
  } catch (e) { /* ignore */ }
}

async function doCheckin() {
  if (hasCheckedIn.value) return
  try {
    const res = await pointApi.checkin()
    uni.showToast({ title: res.message || '签到成功', icon: 'none' })
    hasCheckedIn.value = true
    userPoints.value = res.data?.totalPoints || (userPoints.value + 5)
    badgeName.value = res.data?.badge || badgeName.value
    userLevel.value = res.data?.level || userLevel.value
  } catch (e) { /* ignore */ }
}

function editProfile() {
  editForm.value = {
    nickname: user.value?.nickname || '',
    phone: user.value?.phone || '',
    email: user.value?.email || '',
    gender: user.value?.gender || 0,
    bio: user.value?.bio || '',
    avatar: user.value?.avatar || ''
  }
  editVisible.value = true
}

async function changeAvatar() {
  const res = await new Promise(r => uni.chooseImage({ count: 1, sizeType: ['compressed'], success: r, fail: () => r(null) }))
  if (!res || !res.tempFilePaths?.length) return
  uni.showLoading({ title: '上传中...', mask: true })
  try {
    const uploadRes = await uploadApi.upload(res.tempFilePaths[0])
    editForm.value.avatar = uploadRes.data.url
  } catch (e) { /* fail */ } finally { uni.hideLoading() }
}

async function saveProfile() {
  await userApi.updateProfile(editForm.value)
  await userStore.fetchProfile()
  uni.showToast({ title: '已更新' })
  editVisible.value = false
}

function changePassword() { pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }; pwVisible.value = true }

async function doChangePw() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) return uni.showToast({ title: '请填写完整', icon: 'none' })
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) return uni.showToast({ title: '两次密码不一致', icon: 'none' })
  try {
    await userApi.updatePassword({ oldPassword: pwForm.value.oldPassword, newPassword: pwForm.value.newPassword })
    uni.showToast({ title: '密码修改成功' })
    pwVisible.value = false
  } catch (e) {}
}

function goMyPosts() { uni.navigateTo({ url: '/pages/user/my-posts/my-posts' }) }
function goFavorites() { uni.navigateTo({ url: '/pages/favorite/favorite' }) }
function goFriends() { uni.reLaunch({ url: '/pages/friend/friend' }) }
function goTopics() { uni.navigateTo({ url: '/pages/topic/topic' }) }
function goRank() { uni.navigateTo({ url: '/pages/rank/rank' }) }
function goAdmin() { uni.navigateTo({ url: '/pages/admin/admin' }) }

async function doLogout() {
  const res = await new Promise(r => uni.showModal({ title: '确认退出', content: '确定退出登录？', success: e => r(e.confirm) }))
  if (!res) return
  userStore.logout()
  uni.reLaunch({ url: '/pages/login/login' })
}
function goTab(i, url) {
  if (i === tabIdx.value) return
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
</script>

<style scoped>
.page { padding-bottom: 120rpx; }
.user-header { background: linear-gradient(135deg, #4FAE84, #9AD9B8); padding: 48rpx 32rpx 32rpx; text-align: center; }
.u-avatar { width: 140rpx; height: 140rpx; border-radius: 50%; border: 4rpx solid rgba(255,255,255,0.5); }
.u-name { display: block; font-size: 36rpx; font-weight: bold; color: #fff; margin-top: 16rpx; }
.u-level-row { display: flex; justify-content: center; align-items: center; gap: 16rpx; margin-top: 10rpx; }
.u-badge { font-size: 22rpx; background: rgba(255,255,255,0.25); color: #fff; padding: 4rpx 16rpx; border-radius: 16rpx; }
.u-level { font-size: 22rpx; color: #FFD700; font-weight: 700; }
.u-points { font-size: 22rpx; color: rgba(255,255,255,0.7); }
.checkin-btn { margin-top: 16rpx; display: inline-block; padding: 12rpx 36rpx; background: rgba(255,255,255,0.2); border-radius: 30rpx; color: #fff; font-size: 24rpx; }
.checkin-btn.checked { background: rgba(255,255,255,0.1); opacity: 0.6; }
.u-bio { display: block; font-size: 24rpx; color: rgba(255,255,255,0.7); margin-top: 8rpx; }
.u-stats { display: flex; justify-content: space-around; margin-top: 32rpx; }
.u-stat-item { text-align: center; }
.u-stat-num { display: block; font-size: 36rpx; font-weight: bold; color: #fff; }
.u-stat-label { display: block; font-size: 22rpx; color: rgba(255,255,255,0.7); margin-top: 4rpx; }
.menu-card { background: #fff; margin: 16rpx 24rpx; border-radius: 16rpx; overflow: hidden; }
.menu-item { display: flex; align-items: center; padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 36rpx; width: 60rpx; }
.menu-label { flex: 1; font-size: 28rpx; }
.menu-arrow { font-size: 32rpx; color: #ccc; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-box { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 32rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.avatar-upload { display: flex; flex-direction: column; align-items: center; margin-bottom: 24rpx; }
.avatar-preview { width: 120rpx; height: 120rpx; border-radius: 50%; background: #f0f0f0; }
.avatar-tip { font-size: 22rpx; color: #999; margin-top: 8rpx; }
.m-input { height: 80rpx; background: #F1F2F6; border-radius: 12rpx; padding: 0 20rpx; font-size: 26rpx; margin-bottom: 16rpx; }
.m-gender { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.m-label { font-size: 26rpx; color: #666; }
.gender-opt { padding: 8rpx 24rpx; border-radius: 32rpx; font-size: 24rpx; background: #F1F2F6; }
.gender-opt.active { background: #4FAE84; color: #fff; }
.m-textarea { width: 100%; height: 140rpx; background: #F1F2F6; border-radius: 12rpx; padding: 16rpx; font-size: 26rpx; margin-bottom: 16rpx; }
.m-btns { display: flex; gap: 16rpx; }
.m-btn { flex: 1; height: 76rpx; border-radius: 12rpx; font-size: 28rpx; border: none; line-height: 76rpx; }
.m-btn.cancel { background: #f5f5f5; color: #666; }
.m-btn.confirm { background: #4FAE84; color: #fff; }
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
