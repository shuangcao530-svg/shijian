<script>
// 需要登录才能访问的页面
const AUTH_PAGES = [
  '/pages/post/create/create',
  '/pages/post/edit/edit',
  '/pages/friend/friend',
  '/pages/favorite/favorite',
  '/pages/user/profile/profile',
  '/pages/user/my-posts/my-posts',
  '/pages/admin/admin'
]

// Auth-required tab pages
const AUTH_TABS = [
  '/pages/post/create/create',
  '/pages/friend/friend',
  '/pages/favorite/favorite',
  '/pages/user/profile/profile'
]

export default {
  onLaunch() {
    console.log('校园日常分享 App 启动')

    // Intercept page navigation
    uni.addInterceptor('navigateTo', {
      invoke(args) {
        const token = uni.getStorageSync('token')
        if (!token && AUTH_PAGES.some(p => args.url.startsWith(p))) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          uni.navigateTo({ url: '/pages/login/login' })
          return false
        }
      }
    })

    // 拦截 tab 切换
    uni.addInterceptor('switchTab', {
      invoke(args) {
        const token = uni.getStorageSync('token')
        if (!token && AUTH_TABS.some(p => args.url === p)) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          uni.navigateTo({ url: '/pages/login/login' })
          return false
        }
      }
    })
  }
}
</script>

<style lang="scss">
@import "uni.scss";

page {
  background-color: #EEF8F0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  color: #173B2F;
}

view, text, image, scroll-view {
  box-sizing: border-box;
}
</style>
