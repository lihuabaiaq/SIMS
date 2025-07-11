<template>
  <div class="radar-chart-container">
    <div class="chart-header">
      <h2>个人与同届学情对比</h2>
      <div class="controls-wrapper">
        <select v-model="selectedStartSemester" class="semester-select" aria-label="选择起始学期">
          <option disabled value="">请选择起始学期</option>
          <option v-for="semester in availableSemesters" :key="'start-' + semester" :value="semester">
            {{ semester }}
          </option>
        </select>

        <select v-model="selectedEndSemester" class="semester-select" aria-label="选择结束学期">
          <option disabled value="">请选择结束学期</option>
          <option v-for="semester in availableSemesters" :key="'end-' + semester" :value="semester">
            {{ semester }}
          </option>
        </select>

        <BaseButton 
          type="primary" 
          @click="handleComparison" 
          :disabled="isLoading || !isSelectionValid"
        >
          {{ isLoading ? '正在分析...' : '对比分析' }}
        </BaseButton>
      </div>
    </div>

    <div v-if="errorMsg" class="status-message error-state">
      <p>😕 {{ errorMsg }}</p>
    </div>

    <div class="chart-wrapper">
      <Radar 
        v-if="!isLoading && chartData.datasets[0].data.length > 0" 
        :data="chartData" 
        :options="chartOptions" 
      />
      <div v-else-if="!isLoading && !errorMsg" class="status-message placeholder-state">
        <p>🚀 请选择一个时间范围，然后点击“对比分析”按钮来查看您与同届的综合表现。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend
} from 'chart.js';
import { Radar } from 'vue-chartjs';
// 引入新组件
import BaseButton from '@/components/BaseButton.vue';

ChartJS.register(
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend
);

// --- 响应式状态定义 ---
const availableSemesters = ref([]);
const selectedStartSemester = ref('');
const selectedEndSemester = ref('');
const isLoading = ref(false);
const errorMsg = ref('');

// --- Chart.js 数据和配置 (图例已修改) ---
const chartData = ref({
  labels: [],
  datasets: [
    {
      label: '我的成绩',
      data: [], // <-- 关键修改：添加空的 data 数组
      backgroundColor: 'rgba(54, 162, 235, 0.2)',
      borderColor: 'rgba(54, 162, 235, 1)'
    },
    {
      label: '同届平均成绩',
      data: [], // <-- 关键修改：添加空的 data 数组
      backgroundColor: 'rgba(255, 159, 64, 0.2)',
      borderColor: 'rgba(255, 159, 64, 1)'
    }
  ]
});

const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    r: {
      angleLines: { display: true },
      suggestedMin: 0,
      suggestedMax: 100,
      pointLabels: { font: { size: 12 } }
    }
  },
  plugins: {
    legend: { position: 'top' },
    tooltip: {
      callbacks: {
        label: (context) => `${context.dataset.label}: ${context.raw.toFixed(2)}`
      }
    }
  }
});

// --- 计算属性 ---
const isSelectionValid = computed(() => {
  return selectedStartSemester.value && selectedEndSemester.value;
});

// --- API 调用和逻辑 ---

// 获取可用学期列表 (逻辑不变)
const fetchAvailableSemesters = async () => {
  try {
    const student_id = localStorage.getItem('user_id');
    if (!student_id) throw new Error('无法获取用户ID，请重新登录。');
    const response = await axios.get('/student/studentanalyze/getavailable', { params: { studentId: student_id } });
    if (response.data.code === '200') {
      availableSemesters.value = response.data.data || [];
      if (availableSemesters.value.length >= 2) {
        selectedStartSemester.value = availableSemesters.value[0];
        selectedEndSemester.value = availableSemesters.value[availableSemesters.value.length - 1];
      }
    } else {
      throw new Error(response.data.message || '获取可用学期列表失败');
    }
  } catch (error) {
    errorMsg.value = error.message;
  }
};

// 【重要】执行对比分析的逻辑已修正
const handleComparison = async () => {
  if (!isSelectionValid.value) {
    errorMsg.value = "请选择起始和结束学期。";
    return;
  }

  // 【修改】增加学期顺序校验
  if (selectedStartSemester.value > selectedEndSemester.value) {
    errorMsg.value = "起始学期不能晚于结束学期，请重新选择。";
    return;
  }
  
  isLoading.value = true;
  errorMsg.value = '';
  try {
    const student_id = localStorage.getItem('user_id');

    // 【修改】不再自动排序，直接使用用户选择的值
    const start = selectedStartSemester.value;
    const end = selectedEndSemester.value;

    // 只调用一次 API，获取指定时间段内，学生与同届的对比数据
    const response = await axios.get('/student/studentanalyze/getcomparsion', {
      params: { studentId: student_id, startsemester: start, endsemester: end }
    });

    if (response.data.code === '200') {
      updateChartData(response.data.data);
    } else {
      throw new Error(response.data.message || '获取对比数据失败');
    }

  } catch (error) {
    errorMsg.value = error.message || '网络请求失败，请稍后再试。';
    // 清空旧数据
    chartData.value.labels = [];
    chartData.value.datasets[0].data = [];
    chartData.value.datasets[1].data = [];
  } finally {
    isLoading.value = false;
  }
};

// 【重要】更新图表数据的逻辑已修正
const updateChartData = (responseData) => {
  const { studentScoreList = [], averageScoreList = [] } = responseData;

  const allCategories = [...new Set([...studentScoreList.map(d => d.courseCategory), ...averageScoreList.map(d => d.courseCategory)])];
  
  const createScoreMap = (data) => new Map(data.map(item => [item.courseCategory, item.avgScore]));
  const studentScoreMap = createScoreMap(studentScoreList);
  const averageScoreMap = createScoreMap(averageScoreList);

  chartData.value.labels = allCategories;
  chartData.value.datasets[0].data = allCategories.map(cat => studentScoreMap.get(cat) || 0);
  chartData.value.datasets[1].data = allCategories.map(cat => averageScoreMap.get(cat) || 0);
};

// --- 生命周期钩子 ---
onMounted(fetchAvailableSemesters);
</script>

<style scoped>
.radar-chart-container {
  background-color: #fff;
  padding: 1.5rem 2rem;
  border-radius: 8px;
  box-shadow: var(--box-shadow-base);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.chart-header h2 {
  margin: 0;
  color: var(--text-color-primary);
}

.controls-wrapper {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.semester-select {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 0.9rem;
}

.chart-wrapper {
  position: relative;
  height: 400px;
}

.status-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
  padding: 2rem;
  border-radius: 8px;
}

.placeholder-state {
  background-color: #f8f9fa;
  color: #6c757d;
}

.error-state {
  color: var(--danger-color);
  background-color: #fbecec;
  border: 1px solid #f5c6cb;
  border-radius: 5px;
  padding: 1rem;
  margin-top: 1rem;
  height: auto;
}
</style>