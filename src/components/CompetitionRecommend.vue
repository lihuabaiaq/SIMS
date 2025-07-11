<template>
  <PageLayout title="竞赛推荐" :show-back-button="true">
    <div class="content-card controls-card">
        <div class="header-actions">
            <BaseButton type="primary" @click="showRecentCompetitions">查看最近竞赛</BaseButton>
        </div>
        <div class="filter-grid">
            <div class="filter-group">
                <label for="min-date-filter">最早日期</label>
                <input type="date" id="min-date-filter" v-model="minDate" class="filter-input">
            </div>
            <div class="filter-group">
                <label for="max-date-filter">最晚日期</label>
                <input type="date" id="max-date-filter" v-model="maxDate" class="filter-input">
            </div>
            <div class="filter-group">
                <label for="level-filter">竞赛等级</label>
                <input type="text" id="level-filter" v-model="filterLevel" placeholder="例如：国家级" class="filter-input">
            </div>
            <div class="filter-group">
                <label for="category-filter">竞赛类别</label>
                <input type="text" id="category-filter" v-model="filterCategory" placeholder="例如：计算机" class="filter-input">
            </div>
        </div>
        <div class="filter-actions">
            <BaseButton type="primary" @click="applyFilters">筛选</BaseButton>
            <BaseButton type="secondary" @click="resetFilters">重置</BaseButton>
        </div>
    </div>

    <div class="content-card">
      <table class="competition-table">
        <thead>
          <tr>
            <th>竞赛名称</th>
            <th>报名截止日期</th>
            <th>竞赛描述</th>
            <th>竞赛类别</th>
            <th>竞赛等级</th>
            <th>推荐理由</th>
            <th>推荐状态</th>
            <th>相关性权重</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="loading-text">正在加载推荐数据...</td>
          </tr>
          <tr v-else-if="combinedData.length === 0">
             <td colspan="8" class="no-data-text">暂无符合条件的竞赛推荐</td>
          </tr>
          <tr v-for="item in combinedData" :key="item.competitionId">
            <td>{{ item.name }}</td>
            <td>{{ item.registrationDeadline }}</td>
            <td>{{ item.description }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.level }}</td>
            <td>
                <pre class="reason-text">{{ item.reason || 'N/A' }}</pre>
                <div class="detailed-reason-link">
                    <a href="#" @click.prevent="fetchDetailedReason(item)">✨ AI分析详细原因</a>
                </div>
            </td>
            <td>
              <span :class="getStatusClass(item.recommendStatus)">
                {{ item.recommendStatus || '待计算' }}
              </span>
            </td>
            <td>
                <div class="weight-bar-container">
                    <div class="weight-bar" :style="{ width: Math.min(item.comWeight || 0, 100) + '%' }"></div>
                    <span class="weight-text">{{ item.comWeight ? item.comWeight.toFixed(1) : 'N/A' }}</span>
                </div>
            </td>
          </tr>
        </tbody>
      </table>
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

// 引入组合式函数和新组件
import { useAiAnalysis } from '@/composables/useAiAnalysis.js';
import AiAnalysisModal from '@/components/AiAnalysisModal.vue';

export default {
  name: 'CompetitionRecommend',
  components: { PageLayout, BaseButton, AiAnalysisModal },
  setup() {
    const router = useRouter();
    const loading = ref(true);
    const error = ref(null);
    const competitions = ref([]);
    const recommendations = ref([]);
    const minDate = ref('');
    const maxDate = ref('');
    const filterLevel = ref('');
    const filterCategory = ref('');

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

    const fetchAllCompetitions = async () => {
      try {
        const response = await axios.get('/competition/all');
        if (response.data.code === 200 || response.data.code === '200') {
          competitions.value = response.data.data || [];
        } else {
          throw new Error(response.data.msg || '获取竞赛列表失败');
        }
      } catch (err) {
        console.error("获取竞赛列表失败:", err);
        error.value = '获取竞赛列表失败: ' + (err.response ? err.response.data.msg : err.message);
      }
    };

    const fetchRecommendations = async () => {
      loading.value = true;
      try {
        const studentId = localStorage.getItem('user_id') || '0';
        const payload = {
          studentId: parseInt(studentId),
          minDate: minDate.value || null,
          maxDate: maxDate.value || null,
          level: filterLevel.value || null,
          category: filterCategory.value || null,
        };
        const response = await axios.post('/competition/recommendations', payload);
        if (response.data.code === 200 || response.data.code === '200') {
          recommendations.value = response.data.data || [];
        } else {
          throw new Error(response.data.msg || '获取竞赛推荐失败');
        }
      } catch (err) {
        console.error("获取竞赛推荐失败:", err);
        error.value = '获取竞赛推荐失败: ' + (err.response ? err.response.data.msg : err.message);
        recommendations.value = [];
      } finally {
        loading.value = false;
      }
    };

    const combinedData = computed(() => {
      if (competitions.value.length === 0) return [];
      const recommendMap = new Map(recommendations.value.map(rec => [rec.competitionId, rec]));
      return competitions.value
        .map(comp => ({ ...comp, ...(recommendMap.get(comp.competitionId) || {}) }))
        .filter(item => {
          if (minDate.value || maxDate.value || filterLevel.value || filterCategory.value) {
            return recommendMap.has(item.competitionId);
          }
          return true;
        })
        .sort((a, b) => (b.comWeight || 0) - (a.comWeight || 0));
    });
    
    onMounted(async () => {
      await fetchAllCompetitions();
      await fetchRecommendations();
    });

    const applyFilters = () => {
      fetchRecommendations();
    };

    const resetFilters = () => {
        minDate.value = '';
        maxDate.value = '';
        filterLevel.value = '';
        filterCategory.value = '';
        fetchRecommendations();
    };

    const getStatusClass = (status) => {
      if (status === '推荐') return 'status-badge status-recommended';
      if (status === '可以考虑') return 'status-badge status-consider';
      if (status === '不推荐') return 'status-badge status-not-recommended';
      return 'status-badge';
    };
    
    const showRecentCompetitions = () => {
      router.push('/recent-competitions');
    };

    // 创建新的、简洁的 fetchDetailedReason 方法
    const fetchDetailedReason = (competition) => {
      const studentId = localStorage.getItem('user_id');
      const prompt = `我的学号是${studentId}，请基于我的个人情况，详细分析一下为什么我适合参加名为“${competition.name}”的这个竞赛，并从优势、潜在不足和准备建议三个方面给出具体的说明。`;
      startAiAnalysis(competition.name, prompt);
    };

    return {
      loading,
      combinedData,
      getStatusClass,
      error,
      filterLevel,
      filterCategory,
      applyFilters,
      resetFilters,
      minDate,
      maxDate,
      showRecentCompetitions,
      // 暴露给模板使用
      isModalOpen,
      isGenerating,
      modalContent,
      analysisTitle,
      renderedContent,
      closeModal,
      fetchDetailedReason,
    };
  }
};
</script>

<style scoped>
/* 移除了所有 modal 相关的样式，因为它们已经被移到 AiAnalysisModal.vue 组件中 */
.content-card {
    background-color: var(--bg-color-card);
    padding: 1.5rem 2rem;
    border-radius: var(--border-radius-base);
    margin-bottom: 2rem;
    box-shadow: var(--box-shadow-base);
}

.controls-card {
    border-top: 4px solid var(--primary-color);
}

.header-actions {
  margin-bottom: 1.5rem;
}

.filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 1.5rem;
}

.filter-group {
    display: flex;
    flex-direction: column;
}

.filter-group label {
    margin-bottom: 0.5rem;
    color: var(--text-color-secondary);
    font-size: var(--font-size-small);
    font-weight: 500;
}

.filter-input {
    padding: 0.75rem;
    border: 1px solid var(--border-color);
    border-radius: var(--border-radius-base);
    font-size: var(--font-size-base);
    color: var(--text-color-primary);
    transition: border-color 0.2s, box-shadow 0.2s;
}

.filter-input:focus {
    outline: none;
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

.filter-actions {
    margin-top: 1.5rem;
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
}

.competition-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

.competition-table th,
.competition-table td {
  padding: 1rem 1.5rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  vertical-align: middle;
}

.competition-table th {
  background-color: var(--bg-color-page);
  color: var(--text-color-secondary);
  font-size: var(--font-size-small);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: bold;
}

.competition-table td {
  color: var(--text-color-primary);
  font-size: var(--font-size-base);
}

.reason-text {
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
  font-size: 0.9rem;
  color: var(--text-color-secondary);
}

.competition-table tbody tr:last-child td {
    border-bottom: none;
}

.competition-table tbody tr:hover {
    background-color: var(--bg-color-page);
}

.loading-text, .no-data-text {
  text-align: center;
  padding: 3rem;
  color: var(--text-color-secondary);
  font-size: 1.1rem;
}

.status-badge {
  padding: 0.35rem 0.85rem;
  border-radius: 9999px;
  font-weight: 500;
  font-size: var(--font-size-small);
  white-space: nowrap;
  display: inline-block;
}

.status-recommended {
  color: var(--text-color-on-primary);
  background-color: var(--success-color);
}

.status-consider {
  color: #fff;
  background-color: #f59e0b;
}

.status-not-recommended {
  color: var(--text-color-on-primary);
  background-color: var(--danger-color);
}

.weight-bar-container {
    display: flex;
    align-items: center;
    height: 28px;
    background-color: #e5e7eb;
    border-radius: 14px;
    overflow: hidden;
    position: relative;
    min-width: 150px;
}

.weight-bar {
    height: 100%;
    background-color: var(--primary-color);
    border-radius: 14px 0 0 14px;
    transition: width 0.5s ease-in-out;
}

.weight-text {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    color: var(--text-color-primary);
    font-weight: bold;
    font-size: 0.9rem;
    text-shadow: 0 0 3px white;
}

.detailed-reason-link {
  margin-top: 0.75rem;
  text-align: left;
}

.detailed-reason-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: bold;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.detailed-reason-link a:hover {
  color: var(--primary-color-hover);
  text-decoration: underline;
}
</style>