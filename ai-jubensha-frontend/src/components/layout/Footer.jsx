function Footer() {
    const currentYear = new Date().getFullYear()

    return (
        <footer className="bg-white border-t border-[var(--color-secondary-200)] py-6">
            <div className="container mx-auto px-4">
                <div className="flex flex-col md:flex-row items-center justify-between">
                    <div className="flex items-center space-x-2 mb-4 md:mb-0">
                        <span className="text-xl">🎭</span>
                        <span className="font-semibold text-[var(--color-secondary-700)]">
              AI剧本杀
            </span>
                    </div>

                    <div className="text-sm text-[var(--color-secondary-500)]">
                        © {currentYear} AI-ScriptKill. All rights reserved.
                    </div>

                    <div className="flex items-center space-x-4 mt-4 md:mt-0">
                        <a
                            href="#"
                            className="text-[var(--color-secondary-500)] hover:text-[var(--color-primary-600)] transition-colors"
                        >
                            关于我们
                        </a>
                        <a
                            href="#"
                            className="text-[var(--color-secondary-500)] hover:text-[var(--color-primary-600)] transition-colors"
                        >
                            使用帮助
                        </a>
                        <a
                            href="#"
                            className="text-[var(--color-secondary-500)] hover:text-[var(--color-primary-600)] transition-colors"
                        >
                            隐私政策
                        </a>
                    </div>
                </div>
            </div>
        </footer>
    )
}

export default Footer
