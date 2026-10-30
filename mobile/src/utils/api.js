import { API_BASE_URL } from '../config'
export function api(path, options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('accessToken')
    const header = { 'content-type':'application/json' }
    if (token && !options.skipAuth) header.Authorization = `Bearer ${token}`
    uni.request({ url: API_BASE_URL + path, method: options.method || 'GET', data: options.data, header, success: ({ statusCode, data }) => {
      if (statusCode >= 200 && statusCode < 300) return resolve(data)
      if (statusCode === 401 && !options.skipAuth) { uni.removeStorageSync('accessToken'); uni.reLaunch({ url:'/pages/auth/index' }) }
      const message = typeof data === 'string' ? data : (data?.message || data?.detail || '请求失败，请稍后重试')
      reject(new Error(message))
    }, fail: reject })
  })
}
