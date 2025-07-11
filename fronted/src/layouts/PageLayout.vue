<template>
  <div class="page-layout">
    <header class="page-header">
      <div class="header-content">
        <button v-if="showBackButton" @click="goBack" class="back-button">
          &lt; 返回
        </button>
        <h1 class="page-title">{{ title }}</h1>
      </div>
    </header>
    <main class="page-content">
      <slot></slot>
    </main>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'PageLayout',
  props: {
    title: {
      type: String,
      required: true,
    },
    // 控制返回按钮是否显示，默认为 true
    showBackButton: {
      type: Boolean,
      default: true,
    },
  },
  setup() {
    const router = useRouter();
    const goBack = () => {
      router.back();
    };
    return { goBack };
  },
};
</script>

<style scoped>
.page-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-large);
}

.page-header {
  margin-bottom: var(--spacing-medium);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-title {
  margin: 0;
  font-size: var(--font-size-h1);
  color: var(--text-color-primary);
}

.back-button {
  background: none;
  border: 1px solid var(--border-color);
  color: var(--text-color-secondary);
  padding: 0.4rem 0.8rem;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-content {
  width: 100%;
}
</style>