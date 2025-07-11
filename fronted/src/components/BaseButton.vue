<template>
  <button :class="buttonClass">
    <slot></slot>
  </button>
</template>

<script>
import { computed } from 'vue';

export default {
  name: 'BaseButton',
  props: {
    // 定义按钮类型: primary (主操作), danger (危险操作), success (成功/提交)
    type: {
      type: String,
      default: 'primary', // 默认是主按钮样式
      validator: (value) => ['primary', 'danger', 'success', 'secondary'].includes(value),
    },
  },
  setup(props) {
    const buttonClass = computed(() => `base-button btn-${props.type}`);
    return { buttonClass };
  },
};
</script>

<style scoped>
.base-button {
  padding: 0.6rem 1.2rem;
  border-radius: 5px;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-color-on-primary);
  transition: background-color 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-primary {
  background-color: var(--primary-color);
}
.btn-primary:hover {
  background-color: var(--primary-color-hover);
}

.btn-danger {
  background-color: var(--danger-color);
}
.btn-danger:hover {
  background-color: var(--danger-color-hover);
}

.btn-success {
  background-color: var(--success-color);
  width: 100%; /* 特别为登录按钮设置宽度100% */
  padding: 0.75rem;
  font-size: 1rem;
  margin-top: 1rem;
}
.btn-success:hover {
  background-color: var(--success-color-hover);
}
</style>