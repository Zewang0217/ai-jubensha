import {Outlet} from 'react-router-dom'
import Header from './Header'
import Footer from './Footer'

function MainLayout({children}) {
    return (
        <div className="min-h-screen flex flex-col bg-secondary-50">
            <Header/>
            <main className="flex-1 container mx-auto px-4 py-6">
                {children || <Outlet/>}
            </main>
            <Footer/>
        </div>
    )
}

export default MainLayout
