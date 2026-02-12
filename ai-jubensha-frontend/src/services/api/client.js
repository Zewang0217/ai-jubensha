import axios from 'axios'

/**
 * API 客户端配置
 * 创建 axios 实例并配置拦截器
 */

// 创建 axios 实例
const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        // 添加认证 token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器
apiClient.interceptors.response.use(
    (response) => response.data,
    (error) => {
        // 统一错误处理
        if (error.response) {
            const {status, data} = error.response

            switch (status) {
                case 401:
                    localStorage.removeItem('token')
                    window.location.href = '/login'
                    break
                case 403:
                    console.error('没有权限访问该资源')
                    break
                case 404:
                    console.error('请求的资源不存在')
                    break
                case 500:
                    console.error('服务器内部错误')
                    break
                default:
                    console.error(`请求失败: ${data?.message || error.message}`)
            }
        } else if (error.request) {
            console.error('网络错误，请检查网络连接')
        } else {
            console.error('请求配置错误:', error.message)
        }

        return Promise.reject(error)
    }
)

export default apiClient
