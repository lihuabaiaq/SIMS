import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios'
import './styles/global.css'

// �����������ڹ���ʱ��ֱ���滻�ɶ�Ӧ���ַ���
console.log("��ǰ����:", process.env.NODE_ENV);
console.log("Axios Base URL:", process.env.VUE_APP_AXIOS_BASE_URL);

// axios���������� (���ֲ���)
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

// ֱ��ʹ�û����������� Axios �� baseURL
axios.defaults.baseURL = process.env.VUE_APP_AXIOS_BASE_URL;

const app = createApp(App);

app.use(router);
app.mount('#app');