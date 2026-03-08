import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom'
import {lazy, Suspense} from 'react'
import MainLayout from '../components/layout/MainLayout'
import Loading from '../components/common/Loading'

// 懒加载页面组件
const Home = lazy(() => import('../pages/Home/Home'))
const GameList = lazy(() => import('../pages/GameList/GameList'))
const GameRoom = lazy(() => import('../pages/GameRoom/GameRoom'))
const ScriptList = lazy(() => import('../pages/ScriptList/ScriptList'))
const ScriptDetail = lazy(() => import('../pages/ScriptDetail/ScriptDetail'))
const Scene = lazy(() => import('../pages/Scene/Scene'))
const Settings = lazy(() => import('../pages/Settings/Settings'))
const ScriptCharacters = lazy(() => import('../pages/ScriptCharacters/ScriptCharacters'))
const NotFound = lazy(() => import('../pages/Error/NotFound'))
// 真相揭晓预览页面 - 用于调试和预览
const SummaryPreview = lazy(() => import('../pages/SummaryPreview/SummaryPreview'))

function AppRouter() {
    return (
        <Router>
            <Suspense fallback={<Loading fullScreen/>}>
                <Routes>
                    <Route path="/" element={<MainLayout/>}>
                        <Route index element={<Home/>}/>
                        <Route path="games" element={<GameList/>}/>
                        <Route path="scripts" element={<ScriptList/>}/>
                        <Route path="scripts/:id" element={<ScriptDetail/>}/>
                        <Route path="scripts/:scriptId/characters" element={<ScriptCharacters/>}/>
                        <Route path="scene/:id" element={<Scene/>}/>
                        <Route path="settings" element={<Settings/>}/>
                    </Route>
                    {/* GameRoom 独立路由，不使用 MainLayout（无 Header/Footer） */}
                    <Route path="game/:id" element={<GameRoom/>}/>
                    {/* 真相揭晓预览页面 - 独立路由，可直接访问 */}
                    <Route path="summary-preview" element={<SummaryPreview/>}/>
                    <Route path="/404" element={<NotFound/>}/>
                    <Route path="*" element={<Navigate to="/404" replace/>}/>
                </Routes>
            </Suspense>
        </Router>
    )
}

export default AppRouter