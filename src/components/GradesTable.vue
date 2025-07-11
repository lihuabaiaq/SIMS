<template>
  <div class="grades-table-container">
    <div class="header-flex">
      <h2>个人成绩单</h2>
      <form @submit.prevent="handleSearch" class="search-form">
        <input type="text" v-model="searchParams.course_name" placeholder="课程名称" class="search-input">
        <input type="text" v-model="searchParams.course_category" placeholder="课程类别" class="search-input">
        <input type="number" v-model.number="searchParams.credits" placeholder="学分" class="search-input credits-input">
        <button type="submit" class="search-btn">查询</button>
        <button type="button" @click="resetSearch" class="reset-btn">重置</button>
      </form>
    </div>

    <div v-if="isSearching" class="search-status">正在查询...</div>
    <div v-if="searchError" class="search-status error">{{ searchError }}</div>

    <table class="grades-table">
      <thead>
        <tr>
          <th @click="sortBy('courseYear')" class="sortable">
            开课学年
            <span v-if="sortKey === 'courseYear'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th @click="sortBy('courseName')" class="sortable">
            课程名称
            <span v-if="sortKey === 'courseName'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th>课程类别</th>
          <th @click="sortBy('credit')" class="sortable">
            学分
            <span v-if="sortKey === 'credit'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th @click="sortBy('finalGrade')" class="sortable">
            最终成绩
            <span v-if="sortKey === 'finalGrade'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th @click="sortBy('regularGrade')" class="sortable">
            平时成绩
            <span v-if="sortKey === 'regularGrade'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th @click="sortBy('examGrade')" class="sortable">
            考试成绩
            <span v-if="sortKey === 'examGrade'">{{ sortDirection === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th>教师评语</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="sortedAndDisplayedGrades.length === 0 && !isSearching">
          <td colspan="8">没有找到符合条件的成绩</td>
        </tr>
        <tr v-for="(grade, index) in sortedAndDisplayedGrades" :key="index">
          <td>{{ grade.courseYear }}</td>
          <td>{{ grade.courseName }}</td>
          <td>{{ grade.courseCategory }}</td>
          <td>{{ grade.credit }}</td>
          <td>
            <strong v-if="grade.finalGrade !== null" :class="getGradeClass(grade.finalGrade)">
              {{ grade.finalGrade }}
            </strong>
            <span v-else :class="getGradeClass(grade.finalGrade)">
              未出分
            </span>
          </td>
          <td>{{ grade.regularGrade }}</td>
          <td>{{ grade.examGrade }}</td>
          <td>{{ grade.comments }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, watch, defineProps, computed } from 'vue';
import axios from 'axios';

const props = defineProps({
  grades: {
    type: Array,
    required: true,
    default: () => []
  }
});

const searchParams = ref({ course_name: '', credits: null, course_category: '' });
const displayGrades = ref([]);
const isSearching = ref(false);
const searchError = ref('');

const sortKey = ref('finalGrade');
const sortDirection = ref('desc');

watch(() => props.grades, (newGrades) => {
  displayGrades.value = [...newGrades];
}, { immediate: true });

const handleSearch = async () => {
  isSearching.value = true;
  searchError.value = '';
  sortKey.value = '';
  try {
    const student_id = parseInt(localStorage.getItem('user_id'), 10);
    if (!student_id) throw new Error('无法获取用户ID');
    
    const payload = { studentId: student_id };
    if (searchParams.value.course_name) {
      payload.courseName = searchParams.value.course_name;
    }
    if (searchParams.value.course_category) {
      payload.courseCategory = searchParams.value.course_category;
    }
    if (searchParams.value.credits !== null && searchParams.value.credits > 0) {
      payload.credit = searchParams.value.credits;
    }
    const response = await axios.post('/student/grade/select', payload);

    if (response.data.code === 200||response.data.code === "200") {
      displayGrades.value = response.data.data || [];
    } else {
      throw new Error(response.data.msg || '查询失败');
    }
  } catch (error) {
    console.error("查询成绩失败:", error);
    searchError.value = error.message;
    displayGrades.value = [];
  } finally {
    isSearching.value = false;
  }
};

const resetSearch = () => {
  searchParams.value = { course_name: '', credits: null, course_category: '' };
  searchError.value = '';
  sortKey.value = 'finalGrade';
  sortDirection.value = 'desc';
  displayGrades.value = [...props.grades];
};

const sortBy = (key) => {
  if (sortKey.value === key) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortKey.value = key;
    sortDirection.value = 'desc';
  }
};

const sortedAndDisplayedGrades = computed(() => {
  let gradesToSort = [...displayGrades.value];
  if (sortKey.value) {
    gradesToSort.sort((a, b) => {
      let valA = a[sortKey.value];
      let valB = b[sortKey.value];
      
      // 当排序的字段是最终成绩时，将 null 视为一个极小值
      if (sortKey.value === 'finalGrade') {
        valA = valA === null ? -Infinity : valA;
        valB = valB === null ? -Infinity : valB;
      }
      
      const numericKeys = ['credit', 'finalGrade', 'regularGrade', 'examGrade'];
      if (numericKeys.includes(sortKey.value)) {
        valA = parseFloat(valA) || 0;
        valB = parseFloat(valB) || 0;
      }
      
      if (typeof valA === 'string') {
        return sortDirection.value === 'asc' ? valA.localeCompare(valB, 'zh-Hans-CN') : valB.localeCompare(valA, 'zh-Hans-CN');
      } else {
        return sortDirection.value === 'asc' ? valA - valB : valB - valA;
      }
    });
  }
  return gradesToSort;
});

// 【修改点 2】为 getGradeClass 函数增加对 null 值的处理
const getGradeClass = (grade) => {
  if (grade === null || grade === undefined) return 'grade-pending';
  if (grade >= 90) return 'grade-excellent';
  if (grade >= 80) return 'grade-good';
  if (grade >= 60) return 'grade-pass';
  return 'grade-fail';
};
</script>

<style scoped>
.sortable {
  cursor: pointer;
  user-select: none;
  transition: color 0.2s;
}
.sortable:hover {
  color: #3498db;
}

.header-flex { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
.search-form { display: flex; gap: 0.5rem; align-items: center; }
.search-input { padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; }
.credits-input { width: 80px; }
.search-btn, .reset-btn { padding: 8px 16px; border: none; border-radius: 4px; color: white; cursor: pointer; transition: background-color 0.3s; }
.search-btn { background-color: #3498db; }
.search-btn:hover { background-color: #2980b9; }
.reset-btn { background-color: #95a5a6; }
.reset-btn:hover { background-color: #7f8c8d; }
.search-status { text-align: center; padding: 1rem; color: #555; }
.search-status.error { color: #e74c3c; }
.grades-table-container { margin-top: 2rem; }
h2 { margin-bottom: 1rem; color: #333; }
.grades-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border-radius: 8px; overflow: hidden; }
th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
thead th { background-color: #fafafa; font-weight: 600; color: #333; }
tbody tr:last-child td { border-bottom: none; }
tbody tr:hover { background-color: #f5faff; }
.grade-excellent { color: #4CAF50; }
.grade-good { color: #2196F3; }
.grade-pass { color: #333; }
.grade-fail { color: #f44336; font-weight: bold; }

/* 【修改点 3】新增 '未出分' 状态的样式 */
.grade-pending {
  color: #7f8c8d;
  font-style: italic;
  font-weight: normal;
}
</style>