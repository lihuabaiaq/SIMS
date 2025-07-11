<template>
  <PageLayout title="在线选课中心">
    <div class="controls-card">
      <div class="filter-group">
        <label for="search-input">课程或教师</label>
        <input id="search-input" type="text" v-model="searchText" placeholder="按名称或教师搜索...">
      </div>
      <div class="filter-group">
        <label for="status-filter">课程状态</label>
        <select id="status-filter" v-model="statusFilter">
          <option value="all">全部课程</option>
          <option value="selectable">仅看可选</option>
          <option value="registered">仅看已选</option>
          <option value="not_open">未开放</option>
        </select>
      </div>
    </div>

    <div v-if="isLoading" class="status-indicator">
      <div class="spinner"></div>
      <p>正在拼命加载课程数据中...</p>
    </div>

    <div v-if="error" class="status-indicator error">
      <p>抱歉，加载课程失败：{{ error }}</p>
      <button class="btn btn-primary" @click="fetchCourses">重试</button>
    </div>

    <div v-if="!isLoading && !error">
      <div v-if="filteredCourses.length > 0" class="content-card">
        <table class="course-table">
          <thead>
            <tr>
              <th>课程名称</th>
              <th>授课教师</th>
              <th>学分</th>
              <th>学时</th>
              <th>选课人数</th>
              <th>学期</th>
              <th>课程类别</th>
              <th>年级要求</th>
              <th>选课起止时间</th>
              <th class="description-col">课程描述</th>
              <th class="action-col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="course in filteredCourses" :key="course.courseId">
              <td data-label="课程名称">{{ course.courseName }}</td>
              <td data-label="授课教师">{{ course.teacherName }}</td>
              <td data-label="学分">{{ course.credit }}</td>
              <td data-label="学时">{{ course.hours }}</td>
              <td data-label="选课人数">
                <span :class="{'text-danger': course.currentStudents >= course.maxStudents}">
                  {{ course.currentStudents }}
                </span> / {{ course.maxStudents }}
              </td>
              <td data-label="学期">{{ course.semester }}</td>
              <td data-label="课程类别">{{ course.category }}</td>
              <td data-label="年级要求">{{ course.gradeRequirement || '无' }}</td>
              <td data-label="选课时间">{{ formatDateTime(course.registerStart) }} 至 {{ formatDateTime(course.registerEnd) }}</td>
              <td data-label="课程描述" class="description-col">{{ course.description }}</td>
              <td data-label="操作" class="action-col">
                <button v-if="course.ui_status === 'registered'" class="btn btn-danger" @click="dropCourse(course)">
                  退课
                </button>
                <template v-else-if="course.ui_status === 'selectable'">
                  <button v-if="course.currentStudents >= course.maxStudents" class="btn btn-disabled" disabled>
                    已满
                  </button>
                  <button v-else class="btn btn-primary" @click="selectCourse(course)">
                    抢课
                  </button>
                </template>
                <button v-else-if="course.ui_status === 'not_open'" class="btn btn-disabled" disabled>
                  未开放
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="status-indicator">
        <p>没有找到符合条件的课程。</p>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import PageLayout from '@/layouts/PageLayout.vue';

const allCourses = ref([]);
const isLoading = ref(true);
const error = ref(null);
const searchText = ref('');
const statusFilter = ref('all');

const filteredCourses = computed(() => {
  let courses = allCourses.value;
  if (statusFilter.value !== 'all') {
    courses = courses.filter(course => course.ui_status === statusFilter.value);
  }
  if (searchText.value.trim()) {
    const query = searchText.value.toLowerCase().trim();
    courses = courses.filter(course =>
      course.courseName.toLowerCase().includes(query) ||
      course.teacherName.toLowerCase().includes(query)
    );
  }
  return courses;
});

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return 'N/A';
  const dt = new Date(dateTimeString);
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')} ${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`;
};

const fetchCourses = async () => {
  isLoading.value = true;
  error.value = null;
  const studentId = localStorage.getItem('user_id');
  if (!studentId) {
    error.value = "无法获取学生ID，请重新登录。";
    isLoading.value = false;
    return;
  }
  try {
    const [havingRes, hadRes, behavingRes] = await Promise.all([
      axios.get('/course/list/having', { params: { studentId } }),
      axios.get('/course/list/had', { params: { studentId } }),
      axios.get('/course/list/behaving', { params: { studentId } })
    ]);
    const selectableCourses = (havingRes.data.data || []).map(c => ({ ...c, ui_status: 'selectable' }));
    const registeredCourses = (hadRes.data.data || []).map(c => ({ ...c, ui_status: 'registered' }));
    const notOpenCourses = (behavingRes.data.data || []).map(c => ({ ...c, ui_status: 'not_open' }));
    
    // [核心修改] 使用了新的、更健壮的排序逻辑
    allCourses.value = [
        ...registeredCourses,
        ...selectableCourses,
        ...notOpenCourses
    ].sort((a, b) => {
      // 1. 定义状态的优先级 (数字越小，优先级越高)
      const statusPriority = {
        'registered': 1, // 已选(退课)
        'selectable': 2, // 可选(抢课)
        'not_open': 3    // 未开放
      };

      const priorityA = statusPriority[a.ui_status] || 99; // 如果没有状态，则优先级最低
      const priorityB = statusPriority[b.ui_status] || 99;

      // 2. 首先根据状态优先级排序
      if (priorityA !== priorityB) {
        return priorityA - priorityB;
      }

      // 3. 如果状态相同，则按课程ID排序
      return a.courseId - b.courseId;
    });

  } catch (err) {
    console.error("加载课程数据失败:", err);
    error.value = err.response?.data?.message || err.message || "网络请求失败";
  } finally {
    isLoading.value = false;
  }
};

const selectCourse = async (course) => {
  try {
    const response = await axios.post('/course/register', null, { params: { courseId: course.courseId } });
    if (response.data && response.data.code == 200) {
      alert('选课成功！');
      fetchCourses(); 
    } else {
      throw new Error(response.data.message || '发生未知错误');
    }
  } catch (err) {
    const errorMessage = err.message || err.response?.data?.message || "网络请求失败";
    console.error(`选课失败: ${course.courseName}`, errorMessage);
    alert(`操作失败：${errorMessage}`);
  }
};

const dropCourse = async (course) => {
  if (!confirm(`您确定要退选《${course.courseName}》吗？`)) return;
  try {
    const response = await axios.delete('/course/delete', { params: { courseId: course.courseId } });
    if (response.data && response.data.code == '200') {
      alert('退课成功！');
      fetchCourses();
    } else {
      throw new Error(response.data.message || '发生未知错误');
    }
  } catch (err) {
    const errorMessage = err.message || err.response?.data?.message || "网络请求失败";
    console.error(`退课失败: ${course.courseName}`, errorMessage);
    alert(`操作失败：${errorMessage}`);
  }
};

onMounted(() => {
  fetchCourses();
});
</script>

<style scoped>
/* 样式部分无需修改 */
.controls-card {
  display: flex;
  gap: 1.5rem;
  align-items: flex-end;
  margin-bottom: 2rem;
  padding: 1.5rem 2rem;
  background-color: var(--bg-color-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--box-shadow-base);
  flex-wrap: wrap;
}

.filter-group { display: flex; flex-direction: column; gap: 0.5rem; }
.filter-group label { font-size: var(--font-size-small); color: var(--text-color-secondary); font-weight: 500; }
.filter-group input,
.filter-group select { min-width: 250px; padding: 0.75rem 1rem; border-radius: var(--border-radius-base); border: 1px solid var(--border-color); font-size: var(--font-size-base); transition: border-color 0.2s, box-shadow 0.2s; }
.filter-group input:focus,
.filter-group select:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2); }

.status-indicator { text-align: center; padding: 4rem; color: var(--text-color-secondary); }
.status-indicator.error { color: var(--danger-color); }
.status-indicator button { margin-top: 1rem; }

.spinner { border: 4px solid #f3f3f3; border-top: 4px solid var(--primary-color); border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.content-card {
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 0; 
}

.course-table {
  width: 100%;
  border-collapse: collapse;
}

.course-table thead th {
  background-color: var(--bg-color-page);
  color: var(--text-color-secondary);
  font-weight: bold;
  white-space: nowrap;
}

.course-table th, .course-table td {
  padding: 1rem 1.25rem;
  text-align: left;
  vertical-align: middle;
  border-bottom: 1px solid var(--border-color);
}

.course-table tbody tr:hover { background-color: var(--bg-color-page); }

@media screen and (max-width: 1200px) {
  .content-card { background-color: transparent; }
  .course-table thead { display: none; }
  .course-table tr {
    display: block;
    margin-bottom: 1.5rem;
    border-radius: var(--border-radius-base);
    box-shadow: var(--box-shadow-base);
    background-color: var(--bg-color-card);
    border: 1px solid var(--border-color);
    padding: 0.5rem 1rem;
  }
  .course-table td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.8rem 0.5rem;
    border-bottom: 1px dashed var(--border-color);
    text-align: right;
  }
  .course-table tr td:last-child { border-bottom: none; }
  .course-table td::before {
    content: attr(data-label);
    font-weight: bold;
    color: var(--text-color-primary);
    text-align: left;
    margin-right: 1rem;
  }
  .course-table td.description-col {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
    text-align: left;
  }
   .course-table td.action-col {
    justify-content: center;
    padding-top: 1rem;
    padding-bottom: 0.5rem;
  }
  .course-table td.action-col::before {
    display: none;
  }
  .course-table td[data-label="选课时间"] {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

.text-danger { color: var(--danger-color); font-weight: bold; }
.btn { padding: 0.5rem 1rem; border: none; border-radius: 5px; cursor: pointer; font-size: 0.9rem; font-weight: 500; transition: all 0.2s; white-space: nowrap; }
.btn:hover:not(:disabled) { transform: translateY(-2px); filter: brightness(1.1); }
.btn-primary { background-color: var(--primary-color); color: var(--text-color-on-primary); }
.btn-danger { background-color: var(--danger-color); color: var(--text-color-on-primary); }
.btn-disabled { background-color: var(--text-color-secondary); color: var(--text-color-on-primary); cursor: not-allowed; opacity: 0.7; }
</style>