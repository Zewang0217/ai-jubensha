import {useCallback, useEffect, useState} from 'react'
import {storage} from '../utils/helpers'

/**
 * 本地存储 Hook
 * @param {string} key - 存储键名
 * @param {*} defaultValue - 默认值
 */
export const useLocalStorage = (key, defaultValue = null) => {
    // 获取初始值
    const [value, setValue] = useState(() => {
        return storage.get(key, defaultValue)
    })

    // 更新存储
    const setStoredValue = useCallback((newValue) => {
        setValue(newValue)
        storage.set(key, newValue)
    }, [key])

    // 移除存储
    const removeStoredValue = useCallback(() => {
        setValue(defaultValue)
        storage.remove(key)
    }, [key, defaultValue])

    // 监听其他窗口的变化
    useEffect(() => {
        const handleStorageChange = (e) => {
            if (e.key === key) {
                setValue(e.newValue ? JSON.parse(e.newValue) : defaultValue)
            }
        }

        window.addEventListener('storage', handleStorageChange)
        return () => window.removeEventListener('storage', handleStorageChange)
    }, [key, defaultValue])

    return [value, setStoredValue, removeStoredValue]
}

/**
 * 游戏设置 Hook
 */
export const useGameSettings = () => {
    const defaultSettings = {
        soundEnabled: true,
        soundVolume: 80,
        musicEnabled: true,
        musicVolume: 50,
        notifications: true,
        desktopNotifications: false,
        autoScroll: true,
        showAnimations: true,
        darkMode: false,
        language: 'zh-CN',
    }

    const [settings, setSettings, resetSettings] = useLocalStorage('gameSettings', defaultSettings)

    const updateSetting = useCallback((key, value) => {
        setSettings(prev => ({
            ...prev,
            [key]: value,
        }))
    }, [setSettings])

    return {
        settings,
        setSettings,
        updateSetting,
        resetSettings,
    }
}

/**
 * 用户会话 Hook
 */
export const useUserSession = () => {
    const [user, setUser, removeUser] = useLocalStorage('user', null)
    const [token, setToken, removeToken] = useLocalStorage('token', null)

    const isLoggedIn = !!token && !!user

    const login = useCallback((userData, authToken) => {
        setUser(userData)
        setToken(authToken)
    }, [setUser, setToken])

    const logout = useCallback(() => {
        removeUser()
        removeToken()
    }, [removeUser, removeToken])

    return {
        user,
        token,
        isLoggedIn,
        login,
        logout,
    }
}
