<template>
  <PageLayout title="教师控制面板" :show-back-button="false">
    <div class="welcome-card">
      <div>
        <h2>欢迎您, {{ userName }} 老师</h2>
        <p>工号: {{ userId }}</p>
      </div>
      <BaseButton type="danger" @click="handleLogout">退出登录</BaseButton>
    </div>

    <div class="action-buttons-grid">
      <BaseButton type="primary" @click="handleEditInfo">
        <span class="btn-icon">⚙️</span>
        <span class="btn-text">修改信息</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleGrading">
        <span class="btn-icon">📝</span>
        <span class="btn-text">学生评分</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleCreateCourse">
        <span class="btn-icon">➕</span>
        <span class="btn-text">创建课程</span>
      </BaseButton>
    </div>
  </PageLayout>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
// 引入新组件
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

export default {
  name: 'TeacherDashboard',
  // 注册新组件
  components: {
    PageLayout,
    BaseButton,
  },
  setup() {
    const router = useRouter();
    const userName = ref('');
    const userId = ref('');

    onMounted(() => {
      userName.value = localStorage.getItem('user_name') || '老师';
      userId.value = localStorage.getItem('user_id') || '';
    });

    const handleLogout = () => {
      // 退出登录逻辑不变
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_role');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_id');
      delete axios.defaults.headers.common['Authorization'];
      router.push('/login/teacher');
    };

    // 按钮对应的处理函数不变
    const handleEditInfo = () => router.push('/edit-profile');
    const handleGrading = () => router.push('/grading-courses');
    const handleCreateCourse = () => router.push('/create-course');

    return {
      userName,
      userId,
      handleLogout,
      handleEditInfo,
      handleGrading,
      handleCreateCourse,
    };
  },
};
</script>

<style scoped>
/* 页面特有的样式保持不变，但移除了已在 global.css 中定义的通用样式 */
</style>