<template>
  <PageLayout title="我的成长记录">
    <div class="controls">
      <div class="semester-selector">
        <div class="selector-header">
          <label>选择学期:</label>
          <BaseButton 
            type="primary" 
            @click="toggleSelectAll" 
            :class-name="'select-all-btn'"
          >
            {{ allSelected ? '取消全选' : '全选' }}
          </BaseButton>
        </div>
        <div class="semester-pills">
          <BaseButton
            v-for="semester in availableSemesters"
            :key="semester"
            type="primary"
            :class="['pill-btn', { selected: isSelected(semester) }]"
            @click="toggleSemester(semester)"
          >
            {{ semester }}
          </BaseButton>
          <p v-if="availableSemesters.length === 0" class="no-semester-info">暂无可用学期信息</p>
        </div>
      </div>
      <BaseButton 
        type="success" 
        @click="handleQuery" 
        :disabled="isLoading || selectedSemesters.length === 0"
      >
        {{ isLoading ? '查询中...' : '查询' }}
      </BaseButton>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loader"></div>
      <p>正在努力加载数据中...</p>
    </div>

    <div v-if="errorMessage" class="error-message">
      <p>抱歉，加载失败：{{ errorMessage }}</p>
    </div>

    <div v-if="!isLoading && !errorMessage && Object.keys(recordsData).length === 0" class="initial-prompt">
      <p>请选择学期后，点击"查询"按钮以查看记录。</p>
    </div>

    <div v-if="!isLoading && Object.keys(recordsData).length > 0" class="results-container">
      <div v-for="(data, semester) in recordsData" :key="semester" class="semester-card">
        <h2>{{ semester }} 学期记录</h2>
        <div class="record-section" v-if="data.courses && data.courses.length > 0">
          <h3>课程成绩</h3>
          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>课程名称</th>
                  <th>学分</th>
                  <th>最终成绩</th>
                  <th>绩点</th>
                  <th>教师评语</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="course in data.courses" :key="course.courseName">
                  <td>{{ course.courseName }}</td>
                  <td>{{ course.credit }}</td>
                  <td>{{ course.finalGrade }}</td>
                  <td>{{ course.gradePoint }}</td>
                  <td>{{ course.comments || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="record-section" v-if="data.competitions && data.competitions.length > 0">
          <h3>竞赛经历</h3>
          <ul class="record-list">
            <li v-for="comp in data.competitions" :key="comp.name">
              <div class="record-title"><strong>{{ comp.name }}</strong> ({{ comp.level }}) - <span class="award">{{ comp.award }}</span></div>
              <div class="record-details">
                <span><strong>角色:</strong> {{ comp.role }}</span> | <span><strong>类别:</strong> {{ comp.category }}</span>
              </div>
              <div class="record-details"><strong>时间:</strong> {{ formatDate(comp.startDate) }} ~ {{ formatDate(comp.endDate) }}</div>
              <p class="record-description">{{ comp.description }}</p>
            </li>
          </ul>
        </div>
        <div class="record-section" v-if="data.activities && data.activities.length > 0">
          <h3>参与活动</h3>
          <ul class="record-list">
            <li v-for="activity in data.activities" :key="activity.name">
              <div class="record-title"><strong>{{ activity.name }}</strong></div>
              <div class="record-details">
                <span><strong>角色:</strong> {{ activity.role }}</span> | <span><strong>类别:</strong> {{ activity.category }}</span>
              </div>
              <p class="record-description">{{ activity.description }}</p>
            </li>
          </ul>
        </div>
        <div v-if="!data.courses?.length && !data.competitions?.length && !data.activities?.length" class="no-data-prompt">
            <p>该学期暂无相关记录。</p>
        </div>
      </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
// 引入新组件
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

// setup 内的业务逻辑完全不变
const studentId = ref(null);
const availableSemesters = ref([]);
const selectedSemesters = ref([]);
const recordsData = ref({});
const isLoading = ref(false);
const errorMessage = ref('');

const isSelected = (semester) => selectedSemesters.value.includes(semester);

const toggleSemester = (semester) => {
  const index = selectedSemesters.value.indexOf(semester);
  if (index > -1) {
    selectedSemesters.value.splice(index, 1);
  } else {
    selectedSemesters.value.push(semester);
  }
};

const allSelected = computed(() => {
  return availableSemesters.value.length > 0 && availableSemesters.value.length === selectedSemesters.value.length;
});

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedSemesters.value = [];
  } else {
    selectedSemesters.value = [...availableSemesters.value];
  }
};

onMounted(() => {
  studentId.value = localStorage.getItem('user_id');
  if (studentId.value) {
    fetchAvailableSemesters();
  } else {
    errorMessage.value = "无法获取学生信息，请重新登录。";
  }
});

const fetchAvailableSemesters = async () => {
  try {
    const response = await axios.get(`/student/studentanalyze/getavailable?studentId=${studentId.value}`);
    if (response.data && response.data.code === '200') {
      availableSemesters.value = response.data.data;
    } else {
      throw new Error(response.data.message || '获取学期列表失败');
    }
  } catch (error) {
    errorMessage.value = error.message;
  }
};

const handleQuery = async () => {
  if (selectedSemesters.value.length === 0) {
    alert('请至少选择一个学期！');
    return;
  }
  isLoading.value = true;
  recordsData.value = {};
  errorMessage.value = '';
  try {
    const allQueries = selectedSemesters.value.map(async (semester) => {
      const studentIdentifier = studentId.value;
      const semesterPromises = [
        axios.get(`/record/course/${studentIdentifier}?semester=${semester}`),
        axios.get(`/record/competition/${studentIdentifier}?semester=${semester}`),
        axios.get(`/record/activity/${studentIdentifier}?semester=${semester}`)
      ];
      const responses = await Promise.all(semesterPromises);
      responses.forEach(res => {
        if (res.data.code !== '200') throw new Error(`获取${semester}数据失败`);
      });
      recordsData.value[semester] = {
        courses: responses[0].data.data || [],
        competitions: responses[1].data.data || [],
        activities: responses[2].data.data || []
      };
    });
    await Promise.all(allQueries);
  } catch (error) {
    errorMessage.value = error.message || '加载数据时发生未知错误';
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString();
};

</script>

<style scoped>
/* 旧的 header, back-btn 样式已移除 */

.controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--bg-color-card);
  border-radius: 12px;
  box-shadow: var(--box-shadow-base);
}

.semester-selector {
  width: 100%;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.selector-header label {
  font-size: 1.1rem;
  color: var(--text-color-primary);
  font-weight: bold;
}

.select-all-btn {
  background: none;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}
.select-all-btn:hover {
  background: var(--primary-color);
  color: var(--text-color-on-primary);
}

.semester-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.pill-btn {
  padding: 0.6rem 1.2rem;
  border-radius: 20px;
  border: 1px solid var(--border-color);
  background-color: #f4f4f5;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.pill-btn:hover {
  border-color: #c0c4cc;
}

.pill-btn.selected {
  background-color: var(--primary-color);
  color: var(--text-color-on-primary);
  border-color: var(--primary-color);
  font-weight: bold;
}

.no-semester-info {
  color: var(--text-color-secondary);
}

/* 已移除，样式由BaseButton统一处理 */

.initial-prompt, .loading-container, .error-message, .no-data-prompt {
  text-align: center;
  margin-top: 3rem;
  color: var(--text-color-secondary);
}

.error-message {
  color: var(--danger-color);
}

.loader {
  border: 5px solid #f3f3f3;
  border-top: 5px solid var(--primary-color);
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.semester-card {
  background: var(--bg-color-card);
  padding: 1.5rem 2rem;
  border-radius: 12px;
  box-shadow: var(--box-shadow-base);
  border-top: 4px solid var(--primary-color);
}

.semester-card h2 {
  color: var(--text-color-primary);
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
  margin-top: 0;
  font-size: 1.5rem;
}

.record-section {
  margin-top: 2rem;
}

.record-section h3 {
  color: #34495e; /* or var(--text-color-primary) */
  margin-bottom: 1rem;
  font-size: 1.2rem;
}

.table-responsive {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

th, td {
  padding: 0.8rem 1rem;
  border-bottom: 1px solid #eef2f7;
  vertical-align: middle;
}

th {
  background-color: #f9fafb;
  font-weight: bold;
  color: var(--text-color-secondary);
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background-color: #f5f7fa;
}

.record-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.record-list li {
  background-color: #f9fafb;
  padding: 1.2rem;
  border-left: 4px solid var(--primary-color);
  border-radius: 0 8px 8px 0;
}

.record-title {
  font-size: 1.1rem;
  color: var(--text-color-primary);
  margin-bottom: 0.25rem;
}
.record-title .award {
  color: #e67e22; /* highlight color */
  font-weight: bold;
}
.record-details {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  margin-bottom: 0.5rem;
}
.record-description {
  font-size: 0.95rem;
  color: #555;
  margin: 0.5rem 0 0;
}
</style>