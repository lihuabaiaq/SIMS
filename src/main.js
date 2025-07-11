import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios'
import './styles/global.css'

// 环境变量会在构建时被直接替换成对应的字符串
console.log("当前环境:", process.env.NODE_ENV);
console.log("Axios Base URL:", process.env.VUE_APP_AXIOS_BASE_URL);

// axios请求拦截器 (保持不变)
axios.interceptors.request.use(config => {
  if (!config.url.includes('/login')) {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 直接使用环境变量设置 Axios 的 baseURL
axios.defaults.baseURL = process.env.VUE_APP_AXIOS_BASE_URL;

const app = createApp(App);

app.use(router);
app.mount('#app');