<template>
  <PageLayout title="岗位分析">
    <div class="controls">
      <div class="button-group">
        <BaseButton type="primary" @click="refreshData" :disabled="loading">
          <span v-if="!loading">刷新数据</span>
          <span v-else>刷新中...</span>
        </BaseButton>
        </div>
    </div>

    <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>正在加载岗位推荐...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>

    <div v-if="!loading && !error" class="jobs-grid">
      <div v-for="job in jobs" :key="job.jobId" class="job-card">
        <div class="card-header">
          <h3>{{ job.title }}</h3>
          <span class="company">{{ job.company }}</span>
        </div>
        <div class="card-body">
          <div class="job-meta">
            <span class="location">📍 {{ job.location }}</span>
            <span class="salary">💰 {{ job.salaryRange }}</span>
          </div>
          <p class="description"><strong>岗位描述:</strong> {{ job.description }}</p>
          <p class="requirements"><strong>岗位要求:</strong> {{ job.requirements }}</p>
           <div class="reason">
            <strong>推荐理由 (匹配得分: {{ job.score.toFixed(2) }}):</strong>
            <pre>{{ job.reason }}</pre>
          </div>
          <div class="detailed-reason-link">
            <a href="#" @click.prevent="fetchDetailedReason(job)">✨ AI分析详细原因</a>
          </div>
        </div>
        <div class="card-footer">
            <span class="industry-tag">{{ job.industry }}</span>
            <span class="job-type-tag">{{ job.jobType }}</span>
        </div>
      </div>
    </div>
  </PageLayout>
  
  <AiAnalysisModal
    :is-open="isModalOpen"
    :is-generating="isGenerating"
    :title="analysisTitle"
    :content="modalContent"
    :rendered-content="renderedContent"
    @close="closeModal"
  />
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

// 引入组合式函数和新组件
import { useAiAnalysis } from '@/composables/useAiAnalysis.js';
import AiAnalysisModal from '@/components/AiAnalysisModal.vue';

export default {
  name: 'JobAnalysis',
  components: { PageLayout, BaseButton, AiAnalysisModal },
  setup() {
    const jobs = ref([]);
    const loading = ref(true);
    const error = ref(null);

    // 调用组合式函数，获取所有AI分析相关的状态和方法
    const {
      isModalOpen,
      isGenerating,
      modalContent,
      analysisTitle,
      renderedContent,
      closeModal,
      startAiAnalysis
    } = useAiAnalysis();
    
    const fetchData = async () => {
      try {
        const studentId = localStorage.getItem('user_id');
        if (!studentId) {
          throw new Error('无法获取学生ID，请重新登录。');
        }
        const response = await axios.get('/job/commend', {
          params: { studentId: studentId }
        });

        if (response.data && response.data.code === '200') {
          jobs.value = response.data.data;
        } else {
          throw new Error(response.data.message || '获取岗位推荐失败。');
        }
      } catch (err) {
        console.error('获取岗位推荐时出错:', err);
        error.value = err.message || '加载数据时发生未知错误，请稍后再试或点击刷新。';
      }
    };
    
    const fetchJobRecommendations = async () => {
      loading.value = true;
      error.value = null;
      try {
        await fetchData();
      } finally {
        loading.value = false;
      }
    };
    
    const refreshData = async () => {
      loading.value = true;
      error.value = null;
      try {
        await axios.delete('/job/refresh');
        await fetchData();
      } catch (err) {
        console.error('刷新数据时出错:', err);
        error.value = '刷新数据失败，请检查网络连接或稍后再试。';
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchJobRecommendations);

    // 创建新的、简洁的 fetchDetailedReason 方法
    const fetchDetailedReason = (job) => {
      const studentId = localStorage.getItem('user_id');
      const prompt = `我的学号是${studentId}，我正在考虑是否申请'${job.title}'这个职位。请基于我的个人情况（如课程、成绩、技能等），详细分析我与该岗位的匹配度，并从优势、潜在差距和发展建议三个方面给出具体说明。`;
      startAiAnalysis(job.title, prompt);
    };

    return {
      jobs,
      loading,
      error,
      refreshData,
      // 暴露给模板使用
      isModalOpen,
      isGenerating,
      modalContent,
      analysisTitle,
      renderedContent,
      closeModal,
      fetchDetailedReason,
    };
  },
};
</script>

<style scoped>
/* 移除了所有 modal 相关的样式 */
.controls {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--bg-color-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--box-shadow-base);
}

.button-group {
  display: flex;
  gap: 1rem;
}

.jobs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--spacing-medium);
}

.job-card {
  background-color: var(--bg-color-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-medium);
  box-shadow: var(--box-shadow-base);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  border-top: 4px solid var(--primary-color);
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.card-header h3 {
  margin: 0;
  font-size: var(--font-size-h2);
  color: var(--text-color-primary);
}

.company {
  color: var(--text-color-secondary);
  font-size: var(--font-size-base);
}

.card-body .job-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  color: var(--text-color-secondary);
  font-size: var(--font-size-small);
}

.description, .requirements {
  color: var(--text-color-primary);
  font-size: var(--font-size-small);
  line-height: 1.6;
  margin-bottom: 0.75rem;
}

.reason {
  background-color: var(--bg-color-page);
  border-left: 3px solid var(--primary-color);
  padding: 0.75rem 1rem;
  margin-top: 1rem;
  font-size: var(--font-size-small);
  border-radius: 4px;
}

.reason strong {
  color: var(--text-color-primary);
}

.reason pre {
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0.5rem 0 0;
  color: var(--text-color-secondary);
}

.card-footer {
  margin-top: auto;
  padding-top: 1rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.industry-tag, .job-type-tag {
  background-color: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: var(--font-size-small);
  font-weight: 500;
}

.detailed-reason-link {
  margin-top: 1rem;
  text-align: right;
}

.detailed-reason-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: bold;
  font-size: var(--font-size-small);
  transition: color 0.3s ease;
}

.detailed-reason-link a:hover {
  color: var(--primary-color-hover);
  text-decoration: underline;
}

.loading-container, .error-message {
  text-align: center;
  margin-top: 3rem;
  color: var(--text-color-secondary);
}
.error-message {
  color: var(--danger-color);
}
</style>