import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { WebSocketProvider } from './context/WebSocketContext';
import Home from './pages/Home/Home';
import GameList from './pages/GameList/GameList';
import ScriptList from './pages/ScriptList/ScriptList';
import GameRoom from './pages/GameRoom/GameRoom';
import Scene from './pages/Scene/Scene';
import Character from './pages/Character/Character';
import Clue from './pages/Clue/Clue';
import Settings from './pages/Settings/Settings';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {WebSocketProvider} from './context/WebSocketContext'
import AppRouter from './router'

// 创建QueryClient实例
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5分钟
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <WebSocketProvider>
        <Router>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/games" element={<GameList />} />
            <Route path="/scripts" element={<ScriptList />} />
            <Route path="/game/:id" element={<GameRoom />} />
            <Route path="/scene/:id" element={<Scene />} />
            <Route path="/character/:id" element={<Character />} />
            <Route path="/clue/:id" element={<Clue />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </WebSocketProvider>
    </QueryClientProvider>
  );
}

export default App;