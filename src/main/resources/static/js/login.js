// 登录和注册相关的Vue应用
const app = new Vue({
    el: '#app',
    data() {
        return {
            activeTab: 'login',
            loginForm: {
                username: '',
                password: ''
            },
            registerForm: {
                username: '',
                password: '',
                confirmPassword: '',
                email: ''
            },
            loginRules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 4, max: 20, message: '用户名长度在4到20个字符之间', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
                ]
            },
            registerRules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 4, max: 20, message: '用户名长度在4到20个字符之间', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
                ],
                confirmPassword: [
                    { required: true, message: '请确认密码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            if (value !== this.registerForm.password) {
                                callback(new Error('两次输入的密码不一致'));
                            } else {
                                callback();
                            }
                        },
                        trigger: 'blur'
                    }
                ],
                email: [
                    { required: false, message: '请输入邮箱地址', trigger: 'blur' },
                    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
                ]
            },
            loading: false,
            errorMessage: ''
        };
    },
    methods: {
        // 切换标签页
        handleTabClick(tab) {
            this.activeTab = tab;
            this.errorMessage = '';
        },
        
        // 登录请求
        submitLogin() {
            this.$refs.loginForm.validate((valid) => {
                if (valid) {
                    this.loading = true;
                    this.errorMessage = '';
                    
                    axios.post('/api/auth/login', this.loginForm)
                        .then(response => {
                            this.loading = false;
                            // 使用新的R响应格式
                            const resp = response.data;
                            
                            if (resp.success) {
                                const data = resp.data;
                                
                                // 保存令牌到localStorage
                                localStorage.setItem('token', data.token);
                                localStorage.setItem('userId', data.userId);
                                localStorage.setItem('username', data.username);
                                localStorage.setItem('expireTime', new Date().getTime() + data.expireTime * 1000);
                                
                                // 显示成功消息
                                this.$message.success(resp.message || '登录成功');
                                
                                // 跳转到首页
                                setTimeout(() => {
                                    window.location.href = '/index.html';
                                }, 1000);
                            } else {
                                this.errorMessage = resp.message || '登录失败';
                            }
                        })
                        .catch(error => {
                            this.loading = false;
                            if (error.response && error.response.data) {
                                const resp = error.response.data;
                                this.errorMessage = resp.message || '登录失败，请稍后重试';
                            } else {
                                this.errorMessage = '登录失败，请稍后重试';
                            }
                        });
                }
            });
        },
        
        // 注册请求
        submitRegister() {
            this.$refs.registerForm.validate((valid) => {
                if (valid) {
                    if (this.registerForm.password !== this.registerForm.confirmPassword) {
                        this.errorMessage = '两次输入的密码不一致';
                        return;
                    }
                    
                    this.loading = true;
                    this.errorMessage = '';
                    
                    // 创建注册数据对象，不包含确认密码字段
                    const registerData = {
                        username: this.registerForm.username,
                        password: this.registerForm.password,
                        email: this.registerForm.email
                    };
                    
                    axios.post('/api/auth/register', registerData)
                        .then(response => {
                            this.loading = false;
                            // 使用新的R响应格式
                            const resp = response.data;
                            
                            if (resp.success) {
                                // 显示成功消息
                                this.$message.success(resp.message || '注册成功，请登录');
                                
                                // 切换到登录标签页
                                this.activeTab = 'login';
                                this.loginForm.username = this.registerForm.username;
                                this.loginForm.password = '';
                                
                                // 清空注册表单
                                this.$refs.registerForm.resetFields();
                            } else {
                                this.errorMessage = resp.message || '注册失败';
                            }
                        })
                        .catch(error => {
                            this.loading = false;
                            if (error.response && error.response.data) {
                                const resp = error.response.data;
                                this.errorMessage = resp.message || '注册失败，请稍后重试';
                            } else {
                                this.errorMessage = '注册失败，请稍后重试';
                            }
                        });
                }
            });
        }
    }
});
