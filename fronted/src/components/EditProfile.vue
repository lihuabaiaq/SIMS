<template>
  <PageLayout title="修改个人信息">
    <div class="form-wrapper">
      <p class="subtitle">仅填写需要修改的字段即可</p>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="phone">手机号 (Phone)</label>
          <input type="tel" id="phone" v-model="form.phone" placeholder="请输入新的手机号">
        </div>
        <div class="form-group">
          <label for="email">邮箱 (Email)</label>
          <input type="email" id="email" v-model="form.email" placeholder="请输入新的邮箱地址">
        </div>
        <hr class="divider">
        <p class="subtitle">如需修改密码，请填写以下字段</p>
        <div class="form-group">
          <label for="origin-password">原密码 (Old Password)</label>
          <input type="password" id="origin-password" v-model="form.originPassword" placeholder="请输入原密码">
        </div>
        <div class="form-group">
          <label for="change-password">新密码 (New Password)</label>
          <input type="password" id="change-password" v-model="form.changePassword" placeholder="请输入新密码">
        </div>
        
        <BaseButton 
          type="primary" 
          native-type="submit" 
          :disabled="isLoading" 
          class="submit-button-override"
        >
          {{ isLoading ? '正在提交...' : '确认修改' }}
        </BaseButton>
      </form>

      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
// 引入新组件
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

// setup 内的业务逻辑完全不变
const form = ref({
  phone: '',
  email: '',
  originPassword: '',
  changePassword: ''
});

const isLoading = ref(false);
const message = ref('');
const messageType = ref('');

const handleSubmit = async () => {
  isLoading.value = true;
  message.value = '';

  const userId = localStorage.getItem('user_id');
  const userRole = localStorage.getItem('user_role');

  if (!userId || !userRole) {
    message.value = '无法获取您的用户信息，请尝试重新登录。';
    messageType.value = 'error';
    isLoading.value = false;
    return;
  }

  if (form.value.changePassword && !form.value.originPassword) {
    message.value = '要修改密码，必须提供原密码。';
    messageType.value = 'error';
    isLoading.value = false;
    return;
  }
  if (form.value.originPassword && !form.value.changePassword) {
    message.value = '请输入您的新密码。';
    messageType.value = 'error';
    isLoading.value = false;
    return;
  }
  
  const payload = {};
  if (form.value.phone) payload.phone = form.value.phone;
  if (form.value.email) payload.email = form.value.email;
  if (form.value.originPassword) payload.originPassword = form.value.originPassword;
  if (form.value.changePassword) payload.changePassword = form.value.changePassword;

  if (Object.keys(payload).length === 0) {
    message.value = '请输入至少一项需要修改的信息。';
    messageType.value = 'error';
    isLoading.value = false;
    return;
  }

  if (userRole === 'student') {
    payload.studentId = parseInt(userId, 10);
  } else if (userRole === 'teacher') {
    payload.teacherId = parseInt(userId, 10);
  }

  let apiUrl = userRole === 'teacher' ? '/teacher/changeInfo' : '/student/changeInfo';
  
  try {
    const response = await axios.patch(apiUrl, payload);

    if (response.data && (response.data.code === 200 || response.data.code === '200')) {
      message.value = response.data.message || '信息修改成功！'; // <-- 这里也从 msg 改为 message，保持统一
      messageType.value = 'success';
      form.value.phone = '';
      form.value.email = '';
      form.value.originPassword = '';
      form.value.changePassword = '';
    } else {
      //【关键修改 1】: 将 .msg 改为 .message
      message.value = response.data.message || '操作失败，请重试。';
      messageType.value = 'error';
    }
  } catch (error) {
    console.error(error);
    //【关键修改 2】: 将 .msg 改为 .message
    if (error.response && error.response.data && error.response.data.message) {
      message.value = error.response.data.message;
    } else {
      message.value = '请求失败，请检查网络或联系管理员。';
    }
    messageType.value = 'error';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 容器样式由 PageLayout 控制，这里不再需要 */
.form-wrapper {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.subtitle {
  text-align: center;
  color: var(--text-color-secondary);
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--text-color-primary);
}

.form-group input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

.divider {
  border: none;
  border-top: 1px solid #ecf0f1;
  margin: 2rem 0;
}

.message {
  margin-top: 1.5rem;
  padding: 1rem;
  border-radius: 5px;
  text-align: center;
}

.message.success {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.message.error {
  background-color: #ffebee;
  color: #c62828;
}
</style>