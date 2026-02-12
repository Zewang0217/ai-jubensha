import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom'
import {Suspense, lazy} from 'react'
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

// 路由配置
const routes = [
    {
        path: '/',
        element: <Home/>,
        title: '首页'
    },
    {
        path: '/games',
        element: <GameList/>,
        title: '游戏列表'
    },
    {
        path: '/game/:id',
        element: <GameRoom/>,
        title: '游戏房间'
    },
    {
        path: '/scene/:id',
        element: <Scene/>,
        title: '场景详情'
    },
    {
        path: '/character/:id',
        element: <Character/>,
        title: '角色详情'
    },
    {
        path: '/clue/:id',
        element: <Clue/>,
        title: '线索详情'
    },
    {
        path: '/settings',
        element: <Settings/>,
        title: '设置'
    }
]

function AppRouter() {
    return (
        <Router>
            <MainLayout>
                <Suspense fallback={<Loading fullScreen/>}>
                    <Routes>
                        {routes.map((route) => (
                            <Route
                                key={route.path}
                                path={route.path}
                                element={route.element}
                            />
                        ))}
                        <Route path="/404" element={<NotFound/>}/>
                        <Route path="*" element={<Navigate to="/404" replace/>}/>
                    </Routes>
                </Suspense>
            </MainLayout>
        </Router>
    )
}

export default AppRouter
