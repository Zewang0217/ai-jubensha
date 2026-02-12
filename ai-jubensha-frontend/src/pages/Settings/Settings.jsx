import {useState} from 'react'

const SettingItem = ({label, description, children}) => (
    <div>
        <div>
            <h3>{label}</h3>
            {description && <p>{description}</p>}
        </div>
        <div>{children}</div>
    </div>
)

const Toggle = ({checked, onChange}) => (
    <button onClick={() => onChange(!checked)}>
        {checked ? '开' : '关'}
    </button>
)

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

    const handleChange = (key, value) => {
        setSettings(prev => ({...prev, [key]: value}))
    }

    const handleSave = () => {
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

    return (
        <div>
            <div>
                <h1>游戏设置</h1>
                <p>自定义您的游戏体验</p>
            </div>

            <div>
                <h2>音频设置</h2>
                <SettingItem label="音效" description="启用游戏音效">
                    <Toggle checked={settings.soundEnabled} onChange={(v) => handleChange('soundEnabled', v)}/>
                </SettingItem>
                {settings.soundEnabled && (
                    <SettingItem label="音效音量" description="调整音效音量大小">
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={settings.soundVolume}
                            onChange={(e) => handleChange('soundVolume', parseInt(e.target.value))}
                        />
                        <span>{settings.soundVolume}%</span>
                    </SettingItem>
                )}
                <SettingItem label="背景音乐" description="启用背景音乐">
                    <Toggle checked={settings.musicEnabled} onChange={(v) => handleChange('musicEnabled', v)}/>
                </SettingItem>
                {settings.musicEnabled && (
                    <SettingItem label="音乐音量" description="调整音乐音量大小">
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={settings.musicVolume}
                            onChange={(e) => handleChange('musicVolume', parseInt(e.target.value))}
                        />
                        <span>{settings.musicVolume}%</span>
                    </SettingItem>
                )}
            </div>

            <div>
                <h2>通知设置</h2>
                <SettingItem label="游戏通知" description="接收游戏内通知">
                    <Toggle checked={settings.notifications} onChange={(v) => handleChange('notifications', v)}/>
                </SettingItem>
                <SettingItem label="桌面通知" description="接收桌面推送通知">
                    <Toggle checked={settings.desktopNotifications}
                            onChange={(v) => handleChange('desktopNotifications', v)}/>
                </SettingItem>
            </div>

            <div>
                <h2>游戏设置</h2>
                <SettingItem label="自动滚动" description="对话自动滚动到底部">
                    <Toggle checked={settings.autoScroll} onChange={(v) => handleChange('autoScroll', v)}/>
                </SettingItem>
                <SettingItem label="动画效果" description="启用界面动画效果">
                    <Toggle checked={settings.showAnimations} onChange={(v) => handleChange('showAnimations', v)}/>
                </SettingItem>
                <SettingItem label="深色模式" description="使用深色主题">
                    <Toggle checked={settings.darkMode} onChange={(v) => handleChange('darkMode', v)}/>
                </SettingItem>
                <SettingItem label="语言" description="选择界面语言">
                    <select value={settings.language} onChange={(e) => handleChange('language', e.target.value)}>
                        <option value="zh-CN">简体中文</option>
                        <option value="zh-TW">繁體中文</option>
                        <option value="en">English</option>
                    </select>
                </SettingItem>
            </div>

            <div>
                <button onClick={handleSave}>保存设置</button>
                <button onClick={handleReset}>重置设置</button>
            </div>
        </div>
    )
}

export default Settings
