function Loading({fullScreen = false, size = 'md', text = '加载中...'}) {
    const sizeClasses = {
        sm: 'w-5 h-5',
        md: 'w-8 h-8',
        lg: 'w-12 h-12',
        xl: 'w-16 h-16'
    }

    const spinner = (
        <div className={`${sizeClasses[size]} animate-spin`}>
            <svg
                className="text-(--color-primary-600)"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
            >
                <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                />
                <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
            </svg>
        </div>
    )

    if (fullScreen) {
        return (
            <div
                className="fixed inset-0 flex flex-col items-center justify-center bg-(--color-secondary-50) z-50">
                {spinner}
                {text && (
                    <p className="mt-4 text-(--color-secondary-600) font-medium">
                        {text}
                    </p>
                )}
            </div>
        )
    }

    return (
        <div className="flex flex-col items-center justify-center p-8">
            {spinner}
            {text && (
                <p className="mt-3 text-sm text-(--color-secondary-500)">
                    {text}
                </p>
            )}
        </div>
    )
}

export default Loading
