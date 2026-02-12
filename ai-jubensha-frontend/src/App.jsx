import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {WebSocketProvider} from './context/WebSocketContext'
import AppRouter from './router'

// 创建 QueryClient 实例
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5分钟
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <WebSocketProvider>
        <AppRouter/>
      </WebSocketProvider>
    </QueryClientProvider>
  )
}

export default App
