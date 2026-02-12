// 格式化日期
export const formatDate = (date, format = 'YYYY-MM-DD HH:mm') => {
    if (!date) return '-'

    const d = new Date(date)
    if (isNaN(d.getTime())) return '-'

    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')

    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds)
}

// 格式化相对时间
export const formatRelativeTime = (date) => {
    if (!date) return '-'

    const d = new Date(date)
    const now = new Date()
    const diff = now.getTime() - d.getTime()

    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 30) return `${days}天前`

    return formatDate(date, 'YYYY-MM-DD')
}

// 截断文本
export const truncateText = (text, maxLength = 100, suffix = '...') => {
    if (!text || text.length <= maxLength) return text
    return text.substring(0, maxLength).trim() + suffix
}

// 生成随机 ID
export const generateId = (length = 8) => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
}

// 防抖函数
export const debounce = (func, wait = 300) => {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

// 节流函数
export const throttle = (func, limit = 300) => {
    let inThrottle
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args)
            inThrottle = true
            setTimeout(() => (inThrottle = false), limit)
        }
    }
}

// 深拷贝
export const deepClone = (obj) => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj.getTime())
    if (Array.isArray(obj)) return obj.map(item => deepClone(item))

    const cloned = {}
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            cloned[key] = deepClone(obj[key])
        }
    }
    return cloned
}

// 本地存储封装
export const storage = {
    get: (key, defaultValue = null) => {
        try {
            const item = localStorage.getItem(key)
            return item ? JSON.parse(item) : defaultValue
        } catch (error) {
            console.error('Error reading from localStorage:', error)
            return defaultValue
        }
    },

    set: (key, value) => {
        try {
            localStorage.setItem(key, JSON.stringify(value))
            return true
        } catch (error) {
            console.error('Error writing to localStorage:', error)
            return false
        }
    },

    remove: (key) => {
        try {
            localStorage.removeItem(key)
            return true
        } catch (error) {
            console.error('Error removing from localStorage:', error)
            return false
        }
    },

    clear: () => {
        try {
            localStorage.clear()
            return true
        } catch (error) {
            console.error('Error clearing localStorage:', error)
            return false
        }
    },
}

// 判断对象是否为空
export const isEmpty = (obj) => {
    if (obj === null || obj === undefined) return true
    if (typeof obj === 'string') return obj.trim() === ''
    if (Array.isArray(obj)) return obj.length === 0
    if (typeof obj === 'object') return Object.keys(obj).length === 0
    return false
}

// 睡眠函数
export const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// 文件大小格式化
export const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 复制到剪贴板
export const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(text)
        return true
    } catch (error) {
        console.error('Failed to copy:', error)
        return false
    }
}

// 获取首字母
export const getInitials = (name) => {
    if (!name) return '?'
    return name
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
        .substring(0, 2)
}
