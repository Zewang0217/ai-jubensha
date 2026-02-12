import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom'
import {lazy, Suspense} from 'react'
import MainLayout from '../components/layout/MainLayout'
import Loading from '../components/common/Loading'

// 懒加载页面组件
const Home = lazy(() => import('../pages/Home/Home'))
const GameList = lazy(() => import('../pages/GameList/GameList'))
const GameRoom = lazy(() => import('../pages/GameRoom/GameRoom'))
const Scene = lazy(() => import('../pages/Scene/Scene'))
const Character = lazy(() => import('../pages/Character/Character'))
const Clue = lazy(() => import('../pages/Clue/Clue'))
const Settings = lazy(() => import('../pages/Settings/Settings'))
const NotFound = lazy(() => import('../pages/Error/NotFound'))

function AppRouter() {
    return (
        <Router>
            <Suspense fallback={<Loading fullScreen/>}>
                <Routes>
                    <Route path="/" element={<MainLayout/>}>
                        <Route index element={<Home/>}/>
                        <Route path="games" element={<GameList/>}/>
                        <Route path="game/:id" element={<GameRoom/>}/>
                        <Route path="scene/:id" element={<Scene/>}/>
                        <Route path="character/:id" element={<Character/>}/>
                        <Route path="clue/:id" element={<Clue/>}/>
                        <Route path="settings" element={<Settings/>}/>
                    </Route>
                    <Route path="/404" element={<NotFound/>}/>
                    <Route path="*" element={<Navigate to="/404" replace/>}/>
                </Routes>
            </Suspense>
        </Router>
    )
}

export default AppRouter
