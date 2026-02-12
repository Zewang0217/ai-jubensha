import {Outlet} from 'react-router-dom'
import Header from './Header'
import Footer from './Footer'

function MainLayout({children}) {
    return (
        <div className="min-h-screen flex flex-col">
            <Header/>
            <main className="flex-1">
                {children || <Outlet/>}
            </main>
            <Footer/>
        </div>
    )
}

export default MainLayout
