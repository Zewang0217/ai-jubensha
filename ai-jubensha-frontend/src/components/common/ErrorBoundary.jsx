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
                <div>
                    <h2>出错了</h2>
                    <p>应用程序遇到了意外错误，请刷新页面重试。</p>
                    {this.state.error && (
                        <div>
                            <p>{this.state.error.toString()}</p>
                        </div>
                    )}
                    <button onClick={this.handleReset}>重试</button>
                    <button onClick={() => window.location.reload()}>刷新页面</button>
                </div>
            )
        }

        return this.props.children
    }
}

export default ErrorBoundary
