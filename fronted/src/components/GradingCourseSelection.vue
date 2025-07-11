<template>
  <div class="course-selection-container">
    <div class="header">
      <h1 class="title">选择课程进行评分</h1>
      <button @click="goBack" class="back-btn">返回控制面板</button>
    </div>
    <div v-if="isLoading" class="loading-state">正在加载课程列表...</div>
    <div v-if="!isLoading && displayCourses.length === 0" class="empty-state">
      <p>没有找到状态为“在修”的课程。</p>
    </div>
    <div v-if="!isLoading" class="course-grid">
      <div
        v-for="course in displayCourses"
        :key="course.courseId"
        class="course-card"
        @click="selectCourse(course)"
      >
        <div class="card-header">
          <span class="course-icon">📚</span>
          <h3 class="course-name">{{ course.courseName }}</h3>
        </div>
        <div class="card-body">
          <p><strong>课程ID:</strong> {{ course.courseId }}</p>
          <p><strong>学期:</strong> {{ course.semester }}</p>
        </div>
        <div class="card-footer">
          <span>点击进入评分</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const allCourses = ref([]);
const isLoading = ref(true);
const teacherId = localStorage.getItem('user_id');

onMounted(async () => {
  if (!teacherId) {
    alert('无法获取教师ID，请重新登录。');
    router.push('/login/teacher');
    return;
  }
  try {
    const response = await axios.get(`/teacher/getcourse?teacherId=${teacherId}`);
    if (response.data && response.data.code === '200') {
      allCourses.value = response.data.data;
    } else {
      throw new Error(response.data.message || '获取课程列表失败');
    }
  } catch (error) {
    console.error('获取课程失败:', error);
    alert('获取课程列表时出错，请稍后重试。');
  } finally {
    isLoading.value = false;
  }
});

// 使用计算属性筛选出状态为2（在修）的课程
const displayCourses = computed(() => {
  if (!allCourses.value) return [];
  // 根据提供的 Course.java 文件，status 字段为 Integer
  return allCourses.value.filter(course => course.status === 2);
});

const selectCourse = (course) => {
  router.push({
    name: 'GradingPage',
    params: { courseId: course.courseId },
    // 将课程名称通过 query 传递，避免在下一页重复请求
    query: { courseName: course.courseName }
  });
};

const goBack = () => {
  router.push('/teacher/dashboard'); // 假设教师控制台的路由是这个
};
</script>

<style scoped>
.course-selection-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}
.title {
  color: #2c3e50;
}
.back-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.back-btn:hover {
  background-color: #2980b9;
}
.loading-state, .empty-state {
  text-align: center;
  color: #7f8c8d;
  font-size: 1.2rem;
  padding: 3rem;
}
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}
.course-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}
.card-header {
  padding: 1.5rem;
  background-color: #ecf0f1;
  display: flex;
  align-items: center;
  gap: 1rem;
}
.course-icon {
  font-size: 1.5rem;
}
.course-name {
  margin: 0;
  color: #34495e;
}
.card-body {
  padding: 1.5rem;
  flex-grow: 1;
}
.card-body p {
  margin: 0 0 0.5rem;
  color: #555;
}
.card-footer {
  text-align: center;
  padding: 1rem;
  background-color: #f8f9f9;
  color: #3498db;
  font-weight: 500;
  border-top: 1px solid #e0e0e0;
}
</style>