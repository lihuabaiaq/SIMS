<template>
  <PageLayout title="学情分析">
    <div class="analysis-container">
      <div class="loading-state" v-if="isLoading">
        <div class="spinner"></div>
        <p>正在努力加载您的学情数据...</p>
      </div>

      <div class="error-state" v-if="errorMsg">
        <p>😕 数据加载失败：{{ errorMsg }}</p>
        <BaseButton type="primary" @click="fetchData">重试</BaseButton>
      </div>

      <div v-if="!isLoading && !errorMsg" class="content-grid">
        <ComparisonRadarChart />
        <GradesTable :grades="grades" />
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// 引入新组件和子组件
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';
import GradesTable from './GradesTable.vue';
import ComparisonRadarChart from './ComparisonRadarChart.vue';


// 定义响应式状态
const grades = ref([]); // 只保留了成绩列表的状态
const isLoading = ref(true);
const errorMsg = ref('');

// fetchData 函数被简化，现在只负责获取成绩单的数据
const fetchData = async () => {
  isLoading.value = true;
  errorMsg.value = '';

  try {
    const student_id = parseInt(localStorage.getItem('user_id'), 10);
    if (!student_id) {
      throw new Error('无法获取用户ID，请重新登录。');
    }
    
    const requestConfig = { params: { studentId: student_id } }; 

    // 只需发起获取成绩列表的请求
    const gradesResponse = await axios.get('/student/grade', requestConfig);

    if (gradesResponse.data.code === 200 || gradesResponse.data.code === '200') {
      grades.value = Array.isArray(gradesResponse.data.data) ? gradesResponse.data.data : [];
    } else {
      throw new Error(gradesResponse.data.msg || '获取成绩列表失败');
    }

  } catch (error) {
    console.error("数据加载异常:", error);
    errorMsg.value = error.message || '网络请求出现未知错误。';
  } finally {
    isLoading.value = false;
  }
};

// 组件挂载后执行数据获取
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.analysis-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 4rem 0;
  color: #555;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
}
</style>