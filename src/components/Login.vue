<template>
  <PageLayout title="系统登录">
    <div class="login-card">
      <div class="role-selector">
        <label>
          <input type="radio" v-model="currentRole" value="student"
                 @change="changeRole('student')">
          学生
        </label>
        <label>
          <input type="radio" v-model="currentRole" value="teacher"
                 @change="changeRole('teacher')">
          教师
        </label>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>{{ currentRole === 'student' ? '学号' : '工号' }}</label>
          <input
            type="text"
            v-model="userId"
            :placeholder="currentRole === 'student' ? '请输入学号' : '请输入教师工号'"
            required
          >
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
            required
          >
        </div>

        <BaseButton type="success" native-type="submit">登录</BaseButton>
      </form>
    </div>
  </PageLayout>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import MD5 from 'crypto-js/md5'
import { useRouter, useRoute } from 'vue-router'
// 引入 BaseButton 组件
import BaseButton from '@/components/BaseButton.vue'

export default {
  name: 'LoginView',
  // 注册组件
  components: {
    BaseButton
  },
  props: {
    role: {
      type: String,
      default: 'student'
    }
  },
  setup(props) {
    const router = useRouter()
    const route = useRoute()
    const currentRole = ref(props.role)
    const userId = ref('')
    const password = ref('')

    onMounted(() => {
      if (route.params.role && ['student', 'teacher'].includes(route.params.role)) {
        currentRole.value = route.params.role
      }
    })
    watch(() => route.params.role, (newRole) => {
      if (newRole && ['student', 'teacher'].includes(newRole)) {
        currentRole.value = newRole
      }
    })

    const changeRole = (newRole) => {
      if (newRole !== route.params.role) {
        router.push(`/login/${newRole}`)
      }
    }

    const handleLogin = async () => {
      // 登录逻辑完全不变
      try {
        const apiUrl = currentRole.value === 'student' ? '/student/login' : '/teacher/login'
        const payload = currentRole.value === 'student'
          ? { studentId: userId.value, password: MD5(password.value).toString() }
          : { teacherId: userId.value, password: MD5(password.value).toString() }
        const response = await axios.post(apiUrl, payload)
        console.log('登录响应：', response)
        if (response.data.code === '200') {
          const userData = response.data.data
          const token = userData.token
          const userInfo = userData[currentRole.value]
          if (token) {
            localStorage.setItem('auth_token', token)
            axios.defaults.headers.common['Authorization'] = token
            localStorage.setItem('user_role', currentRole.value)
            localStorage.setItem('user_name', userInfo.name)
            localStorage.setItem('user_id', currentRole.value === 'student' ? userInfo.studentId : userInfo.teacherId)
            router.push(currentRole.value === 'student' ? '/student/dashboard' : '/teacher/dashboard')
            console.log('登录成功，已保存用户信息和token')
          } else {
            throw new Error('登录响应中没有包含token')
          }
        } else {
          throw new Error(response.data.msg || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        alert(error.message || '登录失败，请检查您的账号和密码')
      }
    }

    return {
      currentRole,
      userId,
      password,
      handleLogin,
      changeRole
    }
  }
}
</script>

<style scoped>
.login-card {
  width: 400px;
  padding: var(--spacing-large);
  margin: 2rem auto;
}

.role-selector {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
}
</style>