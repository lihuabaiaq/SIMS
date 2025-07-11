<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>分析与 “{{ title }}” 的匹配度</h3>
        <button @click="close" class="close-btn">&times;</button>
      </div>
      <div class="modal-body">
        <div v-if="isGenerating && !content" class="loading-indicator">
          <div class="spinner"></div>
          <span>AI 思考中，请稍候...</span>
        </div>
        <div class="prose" v-html="renderedContent"></div>
      </div>
      <div class="modal-footer">
        <BaseButton type="primary" @click="close">关闭</BaseButton>
      </div>
    </div>
  </div>
</template>

<script>
import BaseButton from '@/components/BaseButton.vue';

export default {
  name: 'AiAnalysisModal',
  components: { BaseButton },
  props: {
    isOpen: {
      type: Boolean,
      required: true,
    },
    isGenerating: {
      type: Boolean,
      required: true,
    },
    title: {
      type: String,
      required: true,
    },
    content: {
      type: String,
      required: true,
    },
    renderedContent: {
      type: String,
      required: true,
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const close = () => {
      emit('close');
    };

    return { close };
  },
};
</script>

<style scoped>
/* 这里直接从您原有的 CompetitionRecommend.vue 或 JobAnalysis.vue 复制所有 modal 相关的样式 */
/* Modal styles */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: var(--bg-color-card); width: 90%; max-width: 600px; max-height: 80vh; border-radius: var(--border-radius-base); box-shadow: 0 5px 15px rgba(0,0,0,0.3); display: flex; flex-direction: column; }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.2rem; color: var(--text-color-primary); }
.close-btn { background: none; border: none; font-size: 1.8rem; cursor: pointer; color: var(--text-color-secondary); }
.modal-body { padding: 1.5rem; overflow-y: auto; flex-grow: 1; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); text-align: right; }
.loading-indicator { display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 1rem; padding: 2rem; color: var(--text-color-secondary); }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid var(--primary-color); border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.prose { font-size: var(--font-size-base); line-height: 1.6; color: var(--text-color-primary); }
.prose :first-child { margin-top: 0; }
.prose :last-child { margin-bottom: 0; }
</style>