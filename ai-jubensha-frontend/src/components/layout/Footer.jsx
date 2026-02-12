function Footer() {
    const currentYear = new Date().getFullYear()

    return (
        <footer>
            <div>
                <span>AI剧本杀</span>
                <div>© {currentYear} AI-ScriptKill. All rights reserved.</div>
                <div>
                    <a href="#">关于我们</a>
                    <a href="#">使用帮助</a>
                    <a href="#">隐私政策</a>
                </div>
            </div>
        </footer>
    )
}

export default Footer
