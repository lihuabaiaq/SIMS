<template>
  <PageLayout :title="'为《' + courseName + '》评分'">
    <div class="header">
      <p class="subtitle">课程ID: {{ courseId }}</p>
      <button @click="goBack" class="back-btn">返回课程列表</button>
    </div>

    <div v-if="isLoading" class="loading-state">正在加载学生成绩数据...</div>
    <div v-if="!isLoading && grades.length === 0" class="empty-state">
      <p>该课程当前没有学生可选。</p>
    </div>

    <div v-if="!isLoading && grades.length > 0" class="table-container">
      <table class="grades-table">
        <thead>
          <tr>
            <th>学生ID</th>
            <th>平时成绩</th>
            <th>考试成绩</th>
            <th>总成绩</th>
            <th>评语</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="grade in grades" :key="grade.id" :class="{ 'is-disabled': !grade.isEditable }">
            <td>{{ grade.studentId }}</td>
            <td>
              <input type="number" v-model.number="grade.regularGrade" placeholder="输入平时分" :disabled="!grade.isEditable" />
            </td>
            <td>
              <input type="number" v-model.number="grade.examGrade" placeholder="输入考试分" :disabled="!grade.isEditable" />
            </td>
            <td>
              <input type="number" v-model.number="grade.finalGrade" placeholder="输入总成绩" :disabled="!grade.isEditable" />
            </td>
            <td>
              <textarea v-model="grade.comments" placeholder="输入评语" :disabled="!grade.isEditable"></textarea>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="action-bar">
        <BaseButton @click="handleEndCourse" type="danger">结束课程</BaseButton>
        <div class="right-actions">
          <button @click="handleSaveDraft" class="btn-secondary-custom">暂时保存</button>
          <BaseButton @click="handleSubmit" :disabled="!isSubmittable" type="primary">统一提交</BaseButton>
        </div>
      </div>
       <div v-if="!isSubmittable" class="validation-message">
          <p>请确保所有可编辑学生的“平时成绩”、“考试成绩”、“总成绩”和“评语”都已填写完整，才能提交。</p>
       </div>
    </div>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
// 【新增】: 引入所需组件
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';


// --- script 部分的业务逻辑保持不变 ---

// 从路由获取参数和查询
const route = useRoute();
const router = useRouter();
const props = defineProps({
  courseId: {
    type: [String, Number],
    required: true,
  },
});

const courseName = ref(route.query.courseName || '课程');
const grades = ref([]);
const isLoading = ref(true);

const DRAFT_STORAGE_KEY = computed(() => `grading-draft-${props.courseId}`);

onMounted(async () => {
  try {
    const response = await axios.get(`/teacher/getgrade?courseId=${props.courseId}`);
    if (response.data && response.data.code === '200') {
      const originalGrades = response.data.data;
      const draft = localStorage.getItem(DRAFT_STORAGE_KEY.value);
      const draftGrades = draft ? JSON.parse(draft) : [];
      grades.value = originalGrades.map(originalGrade => {
        const isAlreadyGraded = originalGrade.finalGrade !== null && originalGrade.finalGrade !== undefined;
        const isEditable = !isAlreadyGraded;
        const draftGrade = draftGrades.find(d => d.id === originalGrade.id);

        if (isEditable && draftGrade) {
          return { ...draftGrade, isEditable: true };
        } else {
          return { ...originalGrade, isEditable: isEditable };
        }
      });
    } else {
      throw new Error(response.data.message || '获取成绩列表失败');
    }
  } catch (error) {
    console.error('获取成绩列表失败:', error);
    alert('获取成绩列表时出错，请稍后重试。');
  } finally {
    isLoading.value = false;
  }
});

// 计算属性，用于判断是否所有可编辑的成绩都已填写
const isSubmittable = computed(() => {
  const editableGrades = grades.value.filter(grade => grade.isEditable);
  if (editableGrades.length === 0) return false;
  
  return editableGrades.every(grade => {
    const hasRegularGrade = grade.regularGrade !== null && grade.regularGrade !== undefined;
    const hasExamGrade = grade.examGrade !== null && grade.examGrade !== undefined;
    const hasFinalGrade = grade.finalGrade !== null && grade.finalGrade !== undefined;
    const hasComments = grade.comments && grade.comments.trim() !== '';
    return hasRegularGrade && hasExamGrade && hasFinalGrade && hasComments;
  });
});

// “暂时保存”按钮的逻辑
const handleSaveDraft = () => {
  try {
    localStorage.setItem(DRAFT_STORAGE_KEY.value, JSON.stringify(grades.value));
    alert('草稿已保存！');
  } catch (e) {
    alert('保存草稿失败，可能是存储已满。');
    console.error(e);
  }
};

// “统一提交”按钮的逻辑
const handleSubmit = async () => {
  if (!isSubmittable.value) {
    alert('仍有未填写的项目，请检查。');
    return;
  }

  const scoreList = grades.value
    .filter(grade => grade.isEditable)
    .map(grade => ({
      studentId: grade.studentId,
      courseId: props.courseId,
      regularGrade: grade.regularGrade,
      examGrade: grade.examGrade,
      finalGrade: grade.finalGrade,
      comments: grade.comments,
    }));
  
  if (scoreList.length === 0) {
    alert("没有可以提交的新增成绩。");
    return;
  }

  try {
    const response = await axios.put('/teacher/scores', scoreList);
    if (response.data && response.data.code === '200') {
      alert('所有成绩提交成功！');
      localStorage.removeItem(DRAFT_STORAGE_KEY.value);
      router.push('/grading-courses');
    } else {
      throw new Error(response.data.message || '提交失败');
    }
  } catch (error) {
    console.error('提交成绩失败:', error);
    alert(`提交成绩时发生错误: ${error.message}`);
  }
};

// 修改点 2: 新增 handleEndCourse 方法
const handleEndCourse = async () => {
  // 弹窗确认，防止误操作
  if (!confirm(`您确定要结束《${courseName.value}》这门课程吗？这是一个不可逆的操作。`)) {
    return;
  }

  try {
    // 调用后端结课接口
    const response = await axios.post(`/teacher/endCourse`, null, {
      params: { courseId: props.courseId }
    });

    if (response.data && response.data.code === '200') {
      alert('课程已成功结束！');
      // 课程结束后，清除本地草稿
      localStorage.removeItem(DRAFT_STORAGE_KEY.value);
      // 跳转回课程列表页面
      router.push('/grading-courses');
    } else {
      throw new Error(response.data.message || '结束课程失败');
    }
  } catch (error) {
    console.error('结束课程失败:', error);
    alert(`结束课程时发生错误: ${error.message || '未知错误'}`);
  }
};

const goBack = () => {
  router.push('/grading-courses');
};
</script>

<style scoped>
/* 【修改】: 调整/新增 header 相关样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center; /* 垂直居中对齐 */
  margin-bottom: 2rem;
  /* 移除原有的 align-items: flex-start 以更好地对齐按钮 */
}

/* 【修改】: 调整 subtitle 样式使其在 header 中正确对齐 */
.subtitle {
  margin: 0; /* 移除原有 margin */
  color: var(--text-color-secondary);
  font-size: 1rem;
}

/* 【修改】: 特别为“返回”按钮定制样式，使其与主题一致 */
.back-btn {
  background-color: var(--text-color-secondary); /* 使用全局变量的次要颜色 */
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.3s;
}
.back-btn:hover {
  background-color: #6c7a89; /* 一个略深的悬浮色 */
}

.loading-state, .empty-state {
  text-align: center;
  color: #7f8c8d;
  font-size: 1.2rem;
  padding: 3rem;
  background-color: #f9fafb;
  border-radius: 8px;
}
.table-container {
  background-color: white;
  border-radius: var(--border-radius-base);
  box-shadow: var(--box-shadow-base);
  overflow-x: auto;
}
.grades-table {
  width: 100%;
  border-collapse: collapse;
}
.grades-table th, .grades-table td {
  padding: 1rem 1.25rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}
.grades-table th {
  background-color: #f5f7fa;
  font-weight: 600;
  color: var(--text-color-primary);
}
.grades-table td {
  vertical-align: middle;
}

/* 【修改】: 统一表格内输入框的样式 */
.grades-table input, .grades-table textarea {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  font-size: 1rem;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
}

/* 【修改】: 统一表格内输入框的焦点样式 */
.grades-table input:focus, .grades-table textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

.grades-table textarea {
  min-height: 40px;
  resize: vertical;
}

.is-disabled {
  background-color: #f8f9fa;
  color: #adb5bd;
}
.is-disabled input, .is-disabled textarea {
  background-color: #e9ecef;
  cursor: not-allowed;
  border-color: #dee2e6;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background-color: #f9fafb;
  border-top: 1px solid var(--border-color);
  border-bottom-left-radius: var(--border-radius-base);
  border-bottom-right-radius: var(--border-radius-base);
}

.right-actions {
  display: flex;
  gap: 1rem;
}

/* 【新增】: 为“暂时保存”按钮定制样式，使其与 BaseButton 尺寸和感觉类似 */
.btn-secondary-custom {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem; /* 匹配 BaseButton 尺寸 */
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem; /* 匹配 BaseButton 字体大小 */
  font-weight: 500;
  transition: background-color 0.3s ease;
}
.btn-secondary-custom:hover {
  background-color: #7f8c8d;
}

.validation-message {
  padding: 0 1.5rem 1.5rem;
  text-align: right;
  color: var(--danger-color);
  font-size: 0.9rem;
  background-color: #f9fafb;
}
</style>