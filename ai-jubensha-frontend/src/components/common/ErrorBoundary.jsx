import {Component} from 'react'

class ErrorBoundary extends Component {
    constructor(props) {
        super(props)
        this.state = {hasError: false, error: null, errorInfo: null}
    }

    static getDerivedStateFromError(error) {
        return {hasError: true}
    }

    componentDidCatch(error, errorInfo) {
        this.setState({error, errorInfo})
        console.error('Error caught by boundary:', error, errorInfo)
    }

    handleReset = () => {
        this.setState({hasError: false, error: null, errorInfo: null})
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen flex items-center justify-center bg-[var(--color-secondary-50)] p-4">
                    <div className="card max-w-lg w-full text-center">
                        <div className="text-6xl mb-4">😵</div>
                        <h2 className="text-2xl font-bold text-[var(--color-secondary-800)] mb-2">
                            出错了
                        </h2>
                        <p className="text-[var(--color-secondary-600)] mb-6">
                            应用程序遇到了意外错误，请刷新页面重试。
                        </p>
                        {this.state.error && (
                            <div className="bg-[var(--color-secondary-100)] rounded-lg p-4 mb-6 text-left">
                                <p className="text-sm font-mono text-[var(--color-error)]">
                                    {this.state.error.toString()}
                                </p>
                            </div>
                        )}
                        <div className="flex justify-center space-x-4">
                            <button
                                onClick={this.handleReset}
                                className="btn-secondary"
                            >
                                重试
                            </button>
                            <button
                                onClick={() => window.location.reload()}
                                className="btn-primary"
                            >
                                刷新页面
                            </button>
                        </div>
                    </div>
                </div>
            )
        }

        return this.props.children
    }
}

export default ErrorBoundary
