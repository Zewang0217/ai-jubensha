import {Outlet} from 'react-router-dom'
import Header from './Header'
import Footer from './Footer'

function MainLayout({children}) {
    return (
        <div>
            <Header/>
            <main>{children || <Outlet/>}</main>
            <Footer/>
        </div>
    )
}

export default MainLayout
