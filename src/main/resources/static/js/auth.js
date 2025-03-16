// 认证相关的工具函数
const auth = {
    // 获取JWT令牌
    getToken() {
        return localStorage.getItem('token');
    },
    
    // 获取用户ID
    getUserId() {
        return localStorage.getItem('userId');
    },
    
    // 获取用户名
    getUsername() {
        return localStorage.getItem('username');
    },
    
    // 检查是否已登录
    isLoggedIn() {
        const token = this.getToken();
        const expireTime = localStorage.getItem('expireTime');
        
        if (!token || !expireTime) {
            return false;
        }
        
        // 检查令牌是否过期
        return new Date().getTime() < parseInt(expireTime);
    },
    
    // 退出登录
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('expireTime');
        window.location.href = '/login.html';
    },
    
    // 添加请求拦截器，自动添加Authorization头
    setupAxiosInterceptors() {
        // 请求拦截器
        axios.interceptors.request.use(
            config => {
                const token = this.getToken();
                if (token) {
                    config.headers['Authorization'] = 'Bearer ' + token;
                }
                return config;
            },
            error => {
                return Promise.reject(error);
            }
        );
        
        // 响应拦截器
        axios.interceptors.response.use(
            response => {
                // 检查响应是否成功
                const data = response.data;
                if (data && data.code !== 200 && !data.success) {
                    // 如果响应不成功，但HTTP状态码是200，转换为错误
                    return Promise.reject({
                        response: {
                            data: data
                        }
                    });
                }
                return response;
            },
            error => {
                // 处理401未授权错误
                if (error.response) {
                    const resp = error.response.data;
                    if (error.response.status === 401 || (resp && resp.code === 401)) {
                        // 未授权，清除令牌并跳转到登录页
                        this.logout();
                    }
                }
                return Promise.reject(error);
            }
        );
    },
    
    // 检查登录状态，未登录则跳转到登录页
    checkAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    }
};

// 设置axios拦截器
auth.setupAxiosInterceptors();
