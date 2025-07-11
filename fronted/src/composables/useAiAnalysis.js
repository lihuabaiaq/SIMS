// 文件: src/composables/useAiAnalysis.js (修改后)

import { ref, computed } from 'vue';
import MarkdownIt from 'markdown-it';

export function useAiAnalysis() {
  const isModalOpen = ref(false);
  const isGenerating = ref(false);
  const modalContent = ref('');
  const analysisTitle = ref('');
  let abortController = null; // [修改] 使用 AbortController 来中断 fetch

  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
  });

  const renderedContent = computed(() => {
    if (isGenerating.value) {
      const escapedText = modalContent.value
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
      return `<pre style="white-space: pre-wrap; margin: 0; font-family: inherit;">${escapedText} ▌</pre>`;
    } else {
      return md.render(modalContent.value);
    }
  });

  const closeModal = () => {
    isModalOpen.value = false;
    // [修改] 中断 fetch 请求
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
  };

  // [修改] 将整个方法重构为 async 函数，并使用 fetch
  const startAiAnalysis = async (title, prompt) => {
    if (isGenerating.value) return;

    analysisTitle.value = title;
    modalContent.value = '';
    isGenerating.value = true;
    isModalOpen.value = true;
    
    abortController = new AbortController();
    const { signal } = abortController;

    try {
      const token = localStorage.getItem('auth_token');

      const baseUrl = process.env.NODE_ENV === 'development' && process.env.VUE_APP_AI_STREAM_DIRECT_URL
        ? process.env.VUE_APP_AI_STREAM_DIRECT_URL
        : '/api';

      // [修改] URL 中不再包含 token
      const url = `${baseUrl}/ai/chat?message=${encodeURIComponent(prompt)}`;

      // [修改] 使用 fetch 发起请求
      const response = await fetch(url, {
        method: 'GET',
        // [核心修改] 将 Token 放在 Authorization 请求头中
        headers: {
          'Authorization': `Bearer ${token}`
        },
        signal: signal // 允许中断请求
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let accumulatedData = '';

      // eslint-disable-next-line no-constant-condition
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        accumulatedData += decoder.decode(value, { stream: true });
        const lines = accumulatedData.split('\n');
        accumulatedData = lines.pop();

        for (const line of lines) {
          const match = line.match(/^data:\s?(.*)/);
          if (match) {
            const data = match[1].trim();
            if (data === '[DONE]') {
              isGenerating.value = false;
              break;
            }
            if (data) {
              modalContent.value += data;
            }
          }
        }
        if (!isGenerating.value) break;
      }
    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('Fetch aborted by user.');
      } else {
        console.error('AI chat fetch error:', error);
        modalContent.value = '抱歉，连接AI助手失败或分析时出现网络问题。';
      }
    } finally {
      isGenerating.value = false; // 确保无论成功或失败，生成状态都会结束
      abortController = null;
    }
  };

  return {
    isModalOpen,
    isGenerating,
    modalContent,
    analysisTitle,
    renderedContent,
    closeModal,
    startAiAnalysis,
  };
}