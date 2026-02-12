import {useState} from 'react'
import {motion} from 'framer-motion'

function Settings() {
    const [settings, setSettings] = useState({
        // 音频设置
        soundEnabled: true,
        soundVolume: 80,
        musicEnabled: true,
        musicVolume: 50,

        // 通知设置
        notifications: true,
        desktopNotifications: false,

        // 游戏设置
        autoScroll: true,
        showAnimations: true,
        darkMode: false,

        // 语言设置
        language: 'zh-CN',
    })

    const handleChange = (key, value) => {
        setSettings(prev => ({
            ...prev,
            [key]: value
        }))
    }

    const handleSave = () => {
        // TODO: 保存设置到本地存储或服务器
        localStorage.setItem('gameSettings', JSON.stringify(settings))
        alert('设置已保存')
    }

    const handleReset = () => {
        if (confirm('确定要重置所有设置吗？')) {
            setSettings({
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
            })
        }
    }

    const SettingItem = ({label, description, children}) => (
        <div
            className="flex flex-col md:flex-row md:items-center md:justify-between py-4 border-b border-[var(--color-secondary-200)] last:border-0">
            <div className="mb-2 md:mb-0">
                <h3 className="font-medium text-[var(--color-secondary-800)]">{label}</h3>
                {description && (
                    <p className="text-sm text-[var(--color-secondary-500)]">{description}</p>
                )}
            </div>
            <div>{children}</div>
        </div>
    )

    const Toggle = ({checked, onChange}) => (
        <button
            onClick={() => onChange(!checked)}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                checked ? 'bg-[var(--color-primary-600)]' : 'bg-[var(--color-secondary-300)]'
            }`}
        >
      <span
          className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
              checked ? 'translate-x-6' : 'translate-x-1'
          }`}
      />
        </button>
    )

    return (
        <div className="space-y-6">
            {/* Header */}
            <motion.div
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 0}}
            >
                <h1 className="text-2xl font-bold text-[var(--color-secondary-800)]">
                    游戏设置
                </h1>
                <p className="text-[var(--color-secondary-600)]">
                    自定义您的游戏体验
                </p>
            </motion.div>

            {/* Audio Settings */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.1}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4 flex items-center">
                    <span className="mr-2">🔊</span>
                    音频设置
                </h2>

                <SettingItem
                    label="音效"
                    description="启用游戏音效"
                >
                    <Toggle
                        checked={settings.soundEnabled}
                        onChange={(value) => handleChange('soundEnabled', value)}
                    />
                </SettingItem>

                {settings.soundEnabled && (
                    <SettingItem
                        label="音效音量"
                        description="调整音效音量大小"
                    >
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={settings.soundVolume}
                            onChange={(e) => handleChange('soundVolume', parseInt(e.target.value))}
                            className="w-32 h-2 bg-[var(--color-secondary-200)] rounded-lg appearance-none cursor-pointer accent-[var(--color-primary-600)]"
                        />
                        <span className="ml-2 text-sm text-[var(--color-secondary-600)]">
              {settings.soundVolume}%
            </span>
                    </SettingItem>
                )}

                <SettingItem
                    label="背景音乐"
                    description="启用背景音乐"
                >
                    <Toggle
                        checked={settings.musicEnabled}
                        onChange={(value) => handleChange('musicEnabled', value)}
                    />
                </SettingItem>

                {settings.musicEnabled && (
                    <SettingItem
                        label="音乐音量"
                        description="调整音乐音量大小"
                    >
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={settings.musicVolume}
                            onChange={(e) => handleChange('musicVolume', parseInt(e.target.value))}
                            className="w-32 h-2 bg-[var(--color-secondary-200)] rounded-lg appearance-none cursor-pointer accent-[var(--color-primary-600)]"
                        />
                        <span className="ml-2 text-sm text-[var(--color-secondary-600)]">
              {settings.musicVolume}%
            </span>
                    </SettingItem>
                )}
            </motion.div>

            {/* Notification Settings */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.2}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4 flex items-center">
                    <span className="mr-2">🔔</span>
                    通知设置
                </h2>

                <SettingItem
                    label="游戏通知"
                    description="接收游戏内通知"
                >
                    <Toggle
                        checked={settings.notifications}
                        onChange={(value) => handleChange('notifications', value)}
                    />
                </SettingItem>

                <SettingItem
                    label="桌面通知"
                    description="接收桌面推送通知"
                >
                    <Toggle
                        checked={settings.desktopNotifications}
                        onChange={(value) => handleChange('desktopNotifications', value)}
                    />
                </SettingItem>
            </motion.div>

            {/* Game Settings */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.3}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4 flex items-center">
                    <span className="mr-2">🎮</span>
                    游戏设置
                </h2>

                <SettingItem
                    label="自动滚动"
                    description="对话自动滚动到底部"
                >
                    <Toggle
                        checked={settings.autoScroll}
                        onChange={(value) => handleChange('autoScroll', value)}
                    />
                </SettingItem>

                <SettingItem
                    label="动画效果"
                    description="启用界面动画效果"
                >
                    <Toggle
                        checked={settings.showAnimations}
                        onChange={(value) => handleChange('showAnimations', value)}
                    />
                </SettingItem>

                <SettingItem
                    label="深色模式"
                    description="使用深色主题"
                >
                    <Toggle
                        checked={settings.darkMode}
                        onChange={(value) => handleChange('darkMode', value)}
                    />
                </SettingItem>

                <SettingItem
                    label="语言"
                    description="选择界面语言"
                >
                    <select
                        value={settings.language}
                        onChange={(e) => handleChange('language', e.target.value)}
                        className="input w-40"
                    >
                        <option value="zh-CN">简体中文</option>
                        <option value="zh-TW">繁體中文</option>
                        <option value="en">English</option>
                    </select>
                </SettingItem>
            </motion.div>

            {/* Actions */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.4}}
                className="flex flex-col sm:flex-row gap-4"
            >
                <button
                    onClick={handleSave}
                    className="btn-primary flex-1"
                >
                    <span className="mr-2">💾</span>
                    保存设置
                </button>
                <button
                    onClick={handleReset}
                    className="btn-secondary flex-1"
                >
                    <span className="mr-2">🔄</span>
                    重置设置
                </button>
            </motion.div>
        </div>
    )
}

export default Settings
