<template>
  <PageLayout title="课程管理">
    <div class="card">
      <h2>我的课程</h2>
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>课程名称</th>
              <th>开课学期</th>
              <th>学分</th>
              <th>学时</th>
              <th>课程容量</th>
              <th>已选人数</th>
              <th>选课时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="courses.length === 0">
              <td colspan="8" class="no-data">暂无课程数据</td>
            </tr>
            <tr v-for="course in courses" :key="course.courseId">
              <td>
                <a href="#" @click.prevent="showCourseDetails(course)">{{ course.courseName }}</a>
              </td>
              <td>{{ course.semester }}</td>
              <td>{{ course.credit }}</td>
              <td>{{ course.hours }}</td>
              <td>{{ course.maxStudents }}</td>
              <td>{{ course.currentStudents }}</td>
              <td>{{ formatDate(course.registerStart) }} - {{ formatDate(course.registerEnd) }}</td>
              <td><span :class="['status', getStatusClass(course.status)]">{{ formatStatus(course.status) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h2>创建新课程</h2>
      <form @submit.prevent="handleCreateCourse">
        <div class="form-grid">
          <div class="form-group">
            <label for="courseName">课程名称</label>
            <input id="courseName" v-model="newCourse.courseName" type="text" required>
          </div>
          <div class="form-group">
            <label for="credit">学分</label>
            <input id="credit" v-model.number="newCourse.credit" type="number" step="0.5" required>
          </div>
          <div class="form-group">
            <label for="hours">学时</label>
            <input id="hours" v-model.number="newCourse.hours" type="number" required>
          </div>
           <div class="form-group">
            <label for="gradeRequirement">年级要求</label>
            <input id="gradeRequirement" v-model="newCourse.gradeRequirement" type="text" placeholder="例如: 2022" required>
          </div>
          <div class="form-group">
            <label for="category">课程类别</label>
            <input id="category" v-model="newCourse.category" type="text" placeholder="例如: 计算机" required>
          </div>
          <div class="form-group">
            <label for="maxStudents">课程容量</label>
            <input id="maxStudents" v-model.number="newCourse.maxStudents" type="number" required>
          </div>
          <div class="form-group form-group-full">
            <label for="description">课程描述</label>
            <textarea id="description" v-model="newCourse.description" rows="4" required></textarea>
          </div>
          <div class="form-group">
            <label for="registerStart">选课开始时间</label>
            <input id="registerStart" v-model="newCourse.registerStart" type="datetime-local" required>
          </div>
          <div class="form-group">
            <label for="registerEnd">选课结束时间</label>
            <input id="registerEnd" v-model="newCourse.registerEnd" type="datetime-local" required>
          </div>
        </div>
        <BaseButton type="primary" native-type="submit" class="submit-btn-override">
          确认创建
        </BaseButton>
      </form>
    </div>

    <div v-if="isModalVisible" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <button class="modal-close" @click="closeModal">&times;</button>
        <h3 v-if="selectedCourse">{{ selectedCourse.courseName }} - 详细信息</h3>
        <div v-if="selectedCourse" class="modal-details">
          <p><strong>课程描述:</strong> {{ selectedCourse.description }}</p>
          <p><strong>年级要求:</strong> {{ selectedCourse.gradeRequirement }}</p>
          <p><strong>课程类别:</strong> {{ selectedCourse.category }}</p>
        </div>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import PageLayout from '@/layouts/PageLayout.vue';
// 【新增】: 引入 BaseButton 组件
import BaseButton from '@/components/BaseButton.vue';

// --- script 部分的业务逻辑保持不变 ---

// 响应式状态
const courses = ref([]);
// newCourse 定义中已移除 semester
const newCourse = ref({
  courseName: '',
  credit: null,
  hours: null,
  gradeRequirement: '',
  category: '',
  description: '',
  maxStudents: null,
  registerStart: '',
  registerEnd: ''
});
const isModalVisible = ref(false);
const selectedCourse = ref(null);

// 获取教师ID
const teacherId = localStorage.getItem('user_id');

// 获取课程列表
const fetchCourses = async () => {
  if (!teacherId) {
    alert('无法获取教师信息，请重新登录');
    return;
  }
  try {
    const response = await axios.get('/teacher/getcourse', {
      params: { teacherId }
    });
    if (response.data && response.data.code === "200") {
      courses.value = response.data.data;
    } else {
      console.error('获取课程列表失败:', response.data.message);
    }
  } catch (error) {
    console.error('请求课程列表时出错:', error);
    alert('无法加载课程列表，请检查网络或联系管理员。');
  }
};

// 处理课程创建（简化后的最终版本）
const handleCreateCourse = async () => {
  if (!teacherId) {
    alert('无法获取教师信息，请重新登录');
    return;
  }

  // 直接使用不包含 semester 的 newCourse.value 构建最终数据
  const finalPayload = {
    ...newCourse.value,
    teacherId: parseInt(teacherId),
    // 默认值
    currentStudents: 0,
    status: 0 // 默认创建的课程状态为0:未开始
  };

  try {
    // 使用构建好的 finalPayload 发送请求
    const response = await axios.post('/teacher/addcourse', finalPayload);

    if (response.data && response.data.code === "200") {
      alert('课程创建成功！');
      // 清空表单
      Object.keys(newCourse.value).forEach(key => newCourse.value[key] = (typeof newCourse.value[key] === 'number') ? null : '');
      // 重新加载课程列表
      await fetchCourses();
    } else {
      alert('课程创建失败: ' + response.data.message);
    }
  } catch (error) {
    console.error('创建课程请求出错:', error);
    alert('创建失败，请检查填写的数据或联系管理员。');
  }
};

// 弹窗相关方法
const showCourseDetails = (course) => {
  selectedCourse.value = course;
  isModalVisible.value = true;
};

const closeModal = () => {
  isModalVisible.value = false;
  selectedCourse.value = null;
};

// 格式化方法
const formatStatus = (status) => {
  // VVVV --- 修改这里 --- VVVV
  const statusMap = {
    0: '选课未开始',
    1: '选课中',
    2: '在修中',
    3: '已结课',
  };
  // ^^^^ --- 修改这里 --- ^^^^
  return statusMap[status] || '未知状态';
};

const getStatusClass = (status) => {
  // VVVV --- 修改这里 --- VVVV
  const classMap = {
    0: 'status-not-started', // 未开始
    1: 'status-selecting',   // 选课中
    2: 'status-in-progress', // 在修中
    3: 'status-finished'     // 已结课
  };
  // ^^^^ --- 修改这里 --- ^^^^
  return classMap[status] || 'status-unknown';
}

const formatDate = (datetime) => {
    if (!datetime) return 'N/A';
    return datetime.replace('T', ' ');
}

// 组件挂载后执行
onMounted(() => {
  fetchCourses();
});
</script>

<style scoped>
/* 原有的样式保持不变 */
.create-course-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 2rem;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

h2 {
  color: #333;
}

.card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 1.5rem 2rem;
  margin-bottom: 2.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

tbody tr:hover {
  background-color: #f1f3f5;
}

a {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
}

a:hover {
  text-decoration: underline;
}

.no-data {
  text-align: center;
  color: #868e96;
  padding: 2rem;
}

.status {
  padding: 0.25em 0.6em;
  font-size: 0.85em;
  font-weight: bold;
  border-radius: 10px;
  color: #fff;
  white-space: nowrap;
}
.status-not-started { background-color: #6c757d; }
.status-selecting { background-color: #ffc107; }
.status-in-progress { background-color: #007bff; }
.status-finished { background-color: #28a745; }
.status-unknown { background-color: #343a40; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem; /* 为按钮留出空间 */
}

.form-group-full {
  grid-column: 1 / -1;
}

.form-group {
  display: flex;
  flex-direction: column;
}

/* 【修改】: 统一 label 样式 */
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--text-color-primary);
}

/* 【修改】: 统一 input/textarea 样式，使其与 EditProfile 一致 */
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  font-size: 1rem;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
  font-family: inherit; /* 继承字体 */
}

/* 【修改】: 统一焦点样式 */
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

.form-group textarea {
  resize: vertical;
}

/* 【修改】: 这是一个 override class，专门用于布局这个替换后的 BaseButton */
.submit-btn-override {
  margin-top: 1.5rem;
  float: right; /* 保持按钮在右侧 */
  width: auto; /* 覆盖 BaseButton 中可能存在的宽度设置 */
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  position: relative;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.modal-close {
  position: absolute;
  top: 10px;
  right: 15px;
  border: none;
  background: transparent;
  font-size: 2rem;
  cursor: pointer;
  color: #888;
}

.modal-details p {
  line-height: 1.7;
  color: #333;
}

.modal-details p strong {
  color: #000;
}
</style>