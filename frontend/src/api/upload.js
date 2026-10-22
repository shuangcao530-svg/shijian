import axios from 'axios'

export const uploadApi = {
  upload(file) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: '/api/upload',
        filePath: file,
        name: 'file',
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        success(res) {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data)
          } else {
            uni.showToast({ title: data.message, icon: 'none' })
            reject(new Error(data.message))
          }
        },
        fail(err) {
          uni.showToast({ title: '上传失败', icon: 'none' })
          reject(err)
        }
      })
    })
  }
}
