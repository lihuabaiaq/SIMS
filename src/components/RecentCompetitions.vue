<template>
  <PageLayout title="最近竞赛">
    <div class="content-card">
      <table class="competition-table">
        <thead>
          <tr>
            <th>竞赛名称</th>
            <th>报名截止日期</th>
            <th>竞赛描述</th>
            <th>竞赛类别</th>
            <th>竞赛等级</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="loading-text">正在加载数据...</td>
          </tr>
          <tr v-else-if="competitions.length === 0">
            <td colspan="5" class="no-data-text">暂无最近竞赛信息</td>
          </tr>
          <tr v-for="item in competitions" :key="item.competitionId">
            <td>{{ item.name }}</td>
            <td>{{ item.registrationDeadline }}</td>
            <td>{{ item.description }}</td>
            <td>{{ item.category }}</td>
            <td>
              <span :class="getLevelClass(item.level)">
                {{ item.level }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </PageLayout>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import PageLayout from '@/layouts/PageLayout.vue';
// 【已修复】移除了未使用的 BaseButton 组件的引入
// import BaseButton from '@/components/BaseButton.vue'; 

export default {
  name: 'RecentCompetitions',
  // 【已修复】移除了未使用的 BaseButton 组件的注册
  components: { PageLayout }, 
  setup() {
    const competitions = ref([]);
    const loading = ref(true);

    const fetchRecentCompetitions = async () => {
      loading.value = true;
      try {
        const response = await axios.get('/competition/recent');
        if (response.data.code === '200' || response.data.code === 200) {
          competitions.value = response.data.data || [];
        } else {
          throw new Error(response.data.msg || '获取数据失败');
        }
      } catch (err) {
        console.error('获取最近竞赛失败:', err);
        competitions.value = [];
      } finally {
        loading.value = false;
      }
    };

    const getLevelClass = (level) => {
      if (level === '国家级') return 'status-badge status-national';
      if (level === '省级') return 'status-badge status-provincial';
      return 'status-badge status-general';
    };

    onMounted(() => {
      fetchRecentCompetitions();
    });

    return {
      competitions,
      loading,
      getLevelClass,
    };
  }
};
</script>

<style scoped>
/* 样式部分保持不变 */
.content-card {
  background-color: var(--bg-color-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--box-shadow-base);
  padding: 1rem 0;
  overflow: hidden;
}

.competition-table {
  width: 100%;
  border-collapse: collapse;
}

.competition-table th,
.competition-table td {
  padding: 1.2rem 1.5rem;
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

.competition-table tbody tr:last-child td {
  border-bottom: none;
}

.competition-table tbody tr:hover {
  background-color: var(--bg-color-page);
}

.loading-text,
.no-data-text {
  text-align: center;
  padding: 4rem;
  color: var(--text-color-secondary);
  font-size: var(--font-size-base);
}

.status-badge {
  padding: 0.4rem 0.9rem;
  border-radius: 9999px;
  font-weight: 500;
  font-size: var(--font-size-small);
  white-space: nowrap;
  display: inline-block;
  color: var(--text-color-on-primary);
}

.status-national {
  background-color: var(--danger-color);
}

.status-provincial {
  background-color: #f59e0b;
}

.status-general {
  background-color: var(--primary-color-hover);
}
</style>