/**
 * @fileoverview Settings 组件 - 专业设置面板界面
 * @description 游戏设置页面，支持音频、通知、游戏等多项设置
 * @author zewang
 * 
 * 设计特点：
 * - 专业清晰的设置面板风格
 * - 自定义动画开关和滑块组件
 * - 分组卡片式布局
 * - 细腻的交互动画
 */

import {useState, useCallback} from 'react'
import {motion, AnimatePresence} from 'framer-motion'
import {
    Volume2,
    VolumeX,
    Music,
    Music2,
    Bell,
    BellOff,
    Monitor,
    Moon,
    Sun,
    Languages,
    Save,
    RotateCcw,
    Check,
    Settings as SettingsIcon,
    Sparkles
} from 'lucide-react'

function Settings() {
    const [settings, setSettings] = useState({
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

    const [toast, setToast] = useState(null)

    const handleChange = useCallback((key, value) => {
        setSettings(prev => ({...prev, [key]: value}))
    }, [])

    const showToast = useCallback((message, type = 'success') => {
        setToast({message, type})
        setTimeout(() => setToast(null), 3000)
    }, [])

    const handleSave = useCallback(() => {
        localStorage.setItem('gameSettings', JSON.stringify(settings))
        showToast('设置已保存', 'success')
    }, [settings, showToast])

    const handleReset = useCallback(() => {
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
            showToast('设置已重置', 'success')
        }
    }, [showToast])

    return (
        <div className="min-h-screen bg-gradient-to-br from-[var(--color-primary-50)] via-white to-[var(--color-primary-100)]/30 relative overflow-hidden">
            <BackgroundDecoration/>
            
            <div className="relative z-10 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
                <HeaderSection/>
                
                <div className="space-y-6">
                    <AudioSettings settings={settings} onChange={handleChange}/>
                    <NotificationSettings settings={settings} onChange={handleChange}/>
                    <GameSettings settings={settings} onChange={handleChange}/>
                    <ActionButtons onSave={handleSave} onReset={handleReset}/>
                </div>
            </div>
            
            <ToastNotification toast={toast}/>
        </div>
    )
}

function BackgroundDecoration() {
    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-[var(--color-primary-50)] via-white to-[var(--color-primary-100)]/30"/>
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5}}
                className="absolute top-0 -left-32 w-96 h-96 bg-[var(--color-primary-400)]/10 rounded-full blur-3xl"
            />
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5, delay: 0.2}}
                className="absolute bottom-0 -right-32 w-96 h-96 bg-[var(--color-primary-600)]/10 rounded-full blur-3xl"
            />
            
            <div className="absolute inset-0 bg-[linear-gradient(rgba(59,130,246,0.02)_1px,transparent_1px),linear-gradient(90deg,rgba(59,130,246,0.02)_1px,transparent_1px)] bg-[size:60px_60px]"/>
        </div>
    )
}

function HeaderSection() {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
            className="text-center mb-12"
        >
            <motion.div
                initial={{opacity: 0, scale: 0.9}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.5}}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-[var(--color-primary-100)]/80 backdrop-blur-sm border border-[var(--color-primary-200)] mb-6"
            >
                <Sparkles className="w-4 h-4 text-[var(--color-primary-600)]"/>
                <span className="text-[var(--color-primary-700)] text-sm font-medium">个性化游戏体验</span>
            </motion.div>
            
            <div className="flex items-center justify-center gap-3 mb-4">
                <SettingsIcon className="w-8 h-8 text-[var(--color-primary-600)]"/>
                <h1 className="text-4xl sm:text-5xl font-bold">
                    <span className="bg-gradient-to-r from-[var(--color-primary-600)] via-[var(--color-primary-500)] to-[var(--color-primary-700)] bg-clip-text text-transparent">
                        游戏设置
                    </span>
                </h1>
            </div>
            
            <p className="text-lg text-[var(--color-secondary-600)] max-w-2xl mx-auto">
                自定义您的游戏体验，打造专属推理之旅
            </p>
        </motion.div>
    )
}

function AudioSettings({settings, onChange}) {
    return (
        <SettingsCard
            title="音频设置"
            icon={<Volume2 className="w-5 h-5"/>}
            delay={0.1}
        >
            <SettingItem
                icon={settings.soundEnabled ? <Volume2 className="w-5 h-5"/> : <VolumeX className="w-5 h-5"/>}
                label="音效"
                description="启用游戏音效"
            >
                <Toggle
                    checked={settings.soundEnabled}
                    onChange={(v) => onChange('soundEnabled', v)}
                />
            </SettingItem>
            
            {settings.soundEnabled && (
                <motion.div
                    initial={{opacity: 0, height: 0}}
                    animate={{opacity: 1, height: 'auto'}}
                    exit={{opacity: 0, height: 0}}
                >
                    <SettingItem
                        label="音效音量"
                        description="调整音效音量大小"
                    >
                        <Slider
                            value={settings.soundVolume}
                            onChange={(v) => onChange('soundVolume', v)}
                        />
                    </SettingItem>
                </motion.div>
            )}
            
            <div className="border-t border-[var(--color-secondary-200)] my-4"/>
            
            <SettingItem
                icon={settings.musicEnabled ? <Music className="w-5 h-5"/> : <Music2 className="w-5 h-5"/>}
                label="背景音乐"
                description="启用背景音乐"
            >
                <Toggle
                    checked={settings.musicEnabled}
                    onChange={(v) => onChange('musicEnabled', v)}
                />
            </SettingItem>
            
            {settings.musicEnabled && (
                <motion.div
                    initial={{opacity: 0, height: 0}}
                    animate={{opacity: 1, height: 'auto'}}
                    exit={{opacity: 0, height: 0}}
                >
                    <SettingItem
                        label="音乐音量"
                        description="调整音乐音量大小"
                    >
                        <Slider
                            value={settings.musicVolume}
                            onChange={(v) => onChange('musicVolume', v)}
                        />
                    </SettingItem>
                </motion.div>
            )}
        </SettingsCard>
    )
}

function NotificationSettings({settings, onChange}) {
    return (
        <SettingsCard
            title="通知设置"
            icon={<Bell className="w-5 h-5"/>}
            delay={0.2}
        >
            <SettingItem
                icon={settings.notifications ? <Bell className="w-5 h-5"/> : <BellOff className="w-5 h-5"/>}
                label="游戏通知"
                description="接收游戏内通知"
            >
                <Toggle
                    checked={settings.notifications}
                    onChange={(v) => onChange('notifications', v)}
                />
            </SettingItem>
            
            <div className="border-t border-[var(--color-secondary-200)] my-4"/>
            
            <SettingItem
                label="桌面通知"
                description="接收桌面推送通知"
            >
                <Toggle
                    checked={settings.desktopNotifications}
                    onChange={(v) => onChange('desktopNotifications', v)}
                />
            </SettingItem>
        </SettingsCard>
    )
}

function GameSettings({settings, onChange}) {
    return (
        <SettingsCard
            title="游戏设置"
            icon={<Monitor className="w-5 h-5"/>}
            delay={0.3}
        >
            <SettingItem
                label="自动滚动"
                description="对话自动滚动到底部"
            >
                <Toggle
                    checked={settings.autoScroll}
                    onChange={(v) => onChange('autoScroll', v)}
                />
            </SettingItem>
            
            <div className="border-t border-[var(--color-secondary-200)] my-4"/>
            
            <SettingItem
                label="动画效果"
                description="启用界面动画效果"
            >
                <Toggle
                    checked={settings.showAnimations}
                    onChange={(v) => onChange('showAnimations', v)}
                />
            </SettingItem>
            
            <div className="border-t border-[var(--color-secondary-200)] my-4"/>
            
            <SettingItem
                icon={settings.darkMode ? <Moon className="w-5 h-5"/> : <Sun className="w-5 h-5"/>}
                label="深色模式"
                description="使用深色主题"
            >
                <Toggle
                    checked={settings.darkMode}
                    onChange={(v) => onChange('darkMode', v)}
                />
            </SettingItem>
            
            <div className="border-t border-[var(--color-secondary-200)] my-4"/>
            
            <SettingItem
                icon={<Languages className="w-5 h-5"/>}
                label="语言"
                description="选择界面语言"
            >
                <select
                    value={settings.language}
                    onChange={(e) => onChange('language', e.target.value)}
                    className="px-4 py-2 bg-white/80 backdrop-blur-sm border border-[var(--color-secondary-200)] rounded-xl text-[var(--color-secondary-800)] focus:outline-none focus:border-[var(--color-primary-400)] focus:ring-2 focus:ring-[var(--color-primary-400)]/20 transition-all cursor-pointer"
                >
                    <option value="zh-CN">简体中文</option>
                    <option value="zh-TW">繁體中文</option>
                    <option value="en">English</option>
                </select>
            </SettingItem>
        </SettingsCard>
    )
}

function SettingsCard({title, icon, delay, children}) {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.5, delay}}
            className="relative group"
        >
            <div className="absolute inset-0 bg-gradient-to-br from-white/90 to-white/70 backdrop-blur-xl rounded-2xl border border-white/60 shadow-lg shadow-[var(--color-primary-900)]/5 transition-all duration-500 group-hover:shadow-xl group-hover:shadow-[var(--color-primary-900)]/10"/>
            
            <div className="relative p-6">
                <div className="flex items-center gap-3 mb-6">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[var(--color-primary-500)] to-[var(--color-primary-700)] flex items-center justify-center text-white shadow-lg shadow-[var(--color-primary-500)]/30">
                        {icon}
                    </div>
                    <h2 className="text-xl font-bold text-[var(--color-secondary-800)]">{title}</h2>
                </div>
                
                <div className="space-y-4">
                    {children}
                </div>
            </div>
            
            <div className="absolute top-0 right-0 w-20 h-20 overflow-hidden rounded-tr-2xl">
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-[var(--color-primary-100)]/50 to-transparent transform translate-x-8 -translate-y-8 group-hover:translate-x-4 group-hover:-translate-y-4 transition-transform duration-500"/>
            </div>
        </motion.div>
    )
}

function SettingItem({icon, label, description, children}) {
    return (
        <div className="flex items-center justify-between gap-4">
            <div className="flex items-start gap-3 flex-1">
                {icon && (
                    <div className="w-5 h-5 text-[var(--color-secondary-500)] mt-0.5">
                        {icon}
                    </div>
                )}
                <div>
                    <h3 className="font-medium text-[var(--color-secondary-800)]">{label}</h3>
                    {description && (
                        <p className="text-sm text-[var(--color-secondary-500)]">{description}</p>
                    )}
                </div>
            </div>
            <div className="flex-shrink-0">
                {children}
            </div>
        </div>
    )
}

function Toggle({checked, onChange}) {
    return (
        <motion.button
            onClick={() => onChange(!checked)}
            whileTap={{scale: 0.95}}
            className={`
                relative w-14 h-8 rounded-full transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-400)]/50
                ${checked 
                    ? 'bg-gradient-to-r from-[var(--color-primary-500)] to-[var(--color-primary-600)]' 
                    : 'bg-[var(--color-secondary-300)]'
                }
            `}
        >
            <motion.div
                initial={false}
                animate={{x: checked ? 24 : 2}}
                transition={{type: "spring", stiffness: 500, damping: 30}}
                className="absolute top-1 w-6 h-6 bg-white rounded-full shadow-md"
            />
        </motion.button>
    )
}

function Slider({value, onChange}) {
    return (
        <div className="flex items-center gap-3">
            <div className="relative w-32 h-2 bg-[var(--color-secondary-200)] rounded-full overflow-hidden">
                <motion.div
                    initial={{width: 0}}
                    animate={{width: `${value}%`}}
                    transition={{duration: 0.3}}
                    className="absolute inset-y-0 left-0 bg-gradient-to-r from-[var(--color-primary-400)] to-[var(--color-primary-600)] rounded-full"
                />
                <input
                    type="range"
                    min="0"
                    max="100"
                    value={value}
                    onChange={(e) => onChange(parseInt(e.target.value))}
                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                />
            </div>
            <span className="text-sm font-medium text-[var(--color-secondary-600)] w-12 text-right">
                {value}%
            </span>
        </div>
    )
}

function ActionButtons({onSave, onReset}) {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.5, delay: 0.4}}
            className="flex flex-col sm:flex-row gap-4 justify-center pt-4"
        >
            <motion.button
                whileHover={{scale: 1.02}}
                whileTap={{scale: 0.98}}
                onClick={onSave}
                className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-gradient-to-r from-[var(--color-primary-500)] to-[var(--color-primary-700)] text-white text-lg font-semibold rounded-2xl shadow-xl shadow-[var(--color-primary-500)]/30 hover:shadow-2xl hover:shadow-[var(--color-primary-500)]/40 transition-all duration-300"
            >
                <Save className="w-5 h-5"/>
                保存设置
            </motion.button>
            
            <motion.button
                whileHover={{scale: 1.02}}
                whileTap={{scale: 0.98}}
                onClick={onReset}
                className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-white/80 backdrop-blur-sm text-[var(--color-secondary-700)] text-lg font-semibold rounded-2xl border border-[var(--color-secondary-200)] hover:border-[var(--color-primary-300)] hover:text-[var(--color-primary-600)] shadow-lg shadow-slate-200/50 transition-all duration-300"
            >
                <RotateCcw className="w-5 h-5"/>
                重置设置
            </motion.button>
        </motion.div>
    )
}

function ToastNotification({toast}) {
    return (
        <AnimatePresence>
            {toast && (
                <motion.div
                    initial={{opacity: 0, y: 50, scale: 0.9}}
                    animate={{opacity: 1, y: 0, scale: 1}}
                    exit={{opacity: 0, y: 50, scale: 0.9}}
                    transition={{type: "spring", stiffness: 500, damping: 30}}
                    className="fixed bottom-8 left-1/2 -translate-x-1/2 z-50"
                >
                    <div className="flex items-center gap-3 px-6 py-4 bg-white/95 backdrop-blur-xl border border-[var(--color-primary-200)] rounded-2xl shadow-2xl">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center">
                            <Check className="w-5 h-5 text-white"/>
                        </div>
                        <span className="font-medium text-[var(--color-secondary-800)]">{toast.message}</span>
                    </div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}

export default Settings
