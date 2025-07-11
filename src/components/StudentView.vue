<template>
  <PageLayout title="学生控制面板" :show-back-button="false">
    <div class="welcome-card">
      <div>
        <h2>欢迎回来, {{ userName }}</h2>
        <p>学号: {{ userId }}</p>
      </div>
      <BaseButton type="danger" @click="handleLogout">退出登录</BaseButton>
    </div>

    <div class="action-buttons-grid">
      <BaseButton type="primary" @click="handleEditInfo" class="card-button">
        <span class="btn-icon">⚙️</span>
        <span class="btn-text">修改信息</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleGrowthRecord" class="card-button">
        <span class="btn-icon">📈</span>
        <span class="btn-text">成长记录</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleAcademicAnalysis" class="card-button">
        <span class="btn-icon">📊</span>
        <span class="btn-text">学情分析</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleJobAnalysis" class="card-button">
        <span class="btn-icon">💼</span>
        <span class="btn-text">岗位分析</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleCompetitionRecommend" class="card-button">
        <span class="btn-icon">🏆</span>
        <span class="btn-text">竞赛推荐</span>
      </BaseButton>
      <BaseButton type="primary" @click="handleCourseSelection" class="card-button">
        <span class="btn-icon">⚡️</span>
        <span class="btn-text">抢课入口</span>
      </BaseButton>
    </div>
  </PageLayout>

  <div class="ai-corner-trigger" @click="toggleChat">
    <img src="@/assets/ai.png" alt="AI 助手" class="ai-trigger-image" />
    <p class="ai-trigger-text">AI助手</p>
  </div>

  <div v-if="isChatOpen" class="chat-modal-overlay">
    <div class="chat-window">
      <div class="chat-header">
        <h3>AI 助手</h3>
        <BaseButton type="danger" @click="toggleChat" class="close-chat-btn">&times;</BaseButton>
      </div>
      
      <div class="chat-messages" ref="chatMessagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['message-wrapper', 'wrapper-' + msg.sender]">
          <img v-if="msg.sender === 'ai'" src="@/assets/ai.png" alt="AI Avatar" class="chat-avatar" />
          
          <div :class="['message-bubble', 'bubble-' + msg.sender]">
            <div v-if="msg.sender === 'ai'" class="prose" style="white-space: pre-wrap;">
              <div v-if="msg.isComplete" v-html="renderMarkdown(msg.text)"></div>
              <div v-else>{{ msg.text + ' ▌' }}</div>
            </div>
            <div v-else>{{ msg.text }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <textarea
          v-model="newMessage"
          @keydown.enter.prevent="handleSendMessage"
          placeholder="你好，有什么可以帮你的吗？"
          rows="2" 
          ref="inputArea"
        ></textarea>
        <BaseButton type="success" @click="handleSendMessage" :disabled="isSending" class="send-button">发送</BaseButton>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import MarkdownIt from 'markdown-it';
import PageLayout from '@/layouts/PageLayout.vue';
import BaseButton from '@/components/BaseButton.vue';

export default {
  name: 'StudentDashboard',
  components: {
    PageLayout,
    BaseButton,
  },
  setup() {
    const router = useRouter();
    const userName = ref('');
    const userId = ref('');
    const isChatOpen = ref(false);
    const newMessage = ref('');
    const messages = ref([]);
    const isSending = ref(false);
    const chatMessagesContainer = ref(null);
    const inputArea = ref(null);
    let abortController = null;

    const md = new MarkdownIt({
      html: false,
      linkify: true,
      typographer: true,
    });
  
    onMounted(() => {
      userName.value = localStorage.getItem('user_name') || '同学';
      userId.value = localStorage.getItem('user_id') || '';
      // 初始消息已完成，直接标记
      messages.value.push({ 
        sender: 'ai', 
        text: '你好！有什么可以帮助你的吗？\n\n我可以帮你**选课、退课、查询课程**，或者为你**推荐竞赛和岗位**。',
        isComplete: true
      });
    });

    const handleLogout = () => {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_role');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_id');
      delete axios.defaults.headers.common['Authorization'];
      router.push('/login');
    };

    const handleEditInfo = () => router.push('/edit-profile');
    const handleGrowthRecord = () => router.push('/growth-record');
    const handleAcademicAnalysis = () => router.push('/academic-analysis');
    const handleJobAnalysis = () => router.push('/job-analysis');
    const handleCompetitionRecommend = () => router.push('/competition-recommend');
    const handleCourseSelection = () => router.push('/course-selection');

    const toggleChat = () => {
      isChatOpen.value = !isChatOpen.value;
      if (!isChatOpen.value && abortController) {
        abortController.abort();
        abortController = null;
        isSending.value = false;
      }
    };

    const autoScrollChat = () => {
      nextTick(() => {
        const container = chatMessagesContainer.value;
        if (container) container.scrollTop = container.scrollHeight;
      });
    };

    watch(messages, autoScrollChat, { deep: true });

    const renderMarkdown = (text) => md.render(text);

    const handleSendMessage = async () => {
      const userMessage = newMessage.value.trim();
      if (!userMessage || isSending.value) return;

      if (abortController) abortController.abort();
      abortController = new AbortController();
      const { signal } = abortController;

      messages.value.push({ sender: 'user', text: userMessage });
      newMessage.value = '';
      // AI消息占位符，初始状态为未完成
      messages.value.push({ sender: 'ai', text: '', isComplete: false });
      const lastMessageIndex = messages.value.length - 1;
      isSending.value = true;

      try {
        const token = localStorage.getItem('auth_token');
        
        const baseUrl = process.env.NODE_ENV === 'development' && process.env.VUE_APP_AI_STREAM_DIRECT_URL 
          ? process.env.VUE_APP_AI_STREAM_DIRECT_URL 
          : '/api';

        const url = `${baseUrl}/ai/chat?message=${encodeURIComponent(userMessage)}`;
        
        const response = await fetch(url, {
          method: 'GET',
          headers: { 'Authorization': `Bearer ${token}` },
          signal: signal
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

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
                isSending.value = false;
                // AI回复结束，将消息标记为已完成
                if (messages.value[lastMessageIndex]) {
                  messages.value[lastMessageIndex].isComplete = true;
                }
                break;
              }
              if (data) {
                messages.value[lastMessageIndex].text += data;
              }
            }
          }
          if (!isSending.value) break;
        }
      } catch (error) {
        if (error.name === 'AbortError') {
          console.log('Fetch aborted');
          messages.value[lastMessageIndex].text = messages.value[lastMessageIndex].text || '请求已取消。';
        } else {
          console.error('AI chat fetch error:', error);
          messages.value[lastMessageIndex].text = '抱歉，我的大脑出了一点小问题，请稍后再试。';
        }
        // 确保任何异常结束时，都将消息标记为已完成，以进行最终渲染
        if (messages.value[lastMessageIndex]) {
            messages.value[lastMessageIndex].isComplete = true;
        }
      } finally {
        isSending.value = false;
        abortController = null;
        nextTick(() => inputArea.value?.focus());
      }
    };

    return {
      userName, userId, handleLogout, handleEditInfo, handleGrowthRecord,
      handleAcademicAnalysis, handleJobAnalysis, handleCompetitionRecommend,
      handleCourseSelection, isChatOpen, newMessage, messages, isSending,
      chatMessagesContainer, inputArea, toggleChat, handleSendMessage,
      renderMarkdown
    };
  }
}
</script>

<style scoped>
/* 样式部分保持不变 */
.action-buttons-grid { grid-template-columns: repeat(3, 1fr); gap: var(--spacing-large); }
.card-button { background-color: var(--bg-color-card); border: 1px solid var(--border-color); border-radius: var(--border-radius-base); padding: 2rem; font-weight: 500; color: var(--text-color-primary); cursor: pointer; text-align: center; transition: all 0.3s ease; box-shadow: 0 2px 5px rgba(0,0,0,0.05); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 1rem; font-size: 1.1rem; }
.card-button:hover { transform: translateY(-5px); box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1); border-color: var(--primary-color); color: var(--primary-color); }
.card-button .btn-icon { font-size: 3rem; line-height: 1; }

.ai-corner-trigger { position: fixed; right: 40px; bottom: 40px; z-index: 1000; display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; transition: transform 0.3s ease; }
.ai-corner-trigger:hover { transform: scale(1.1); }
.ai-trigger-image { width: 70px; height: 70px; border-radius: 50%; box-shadow: 0 4px 15px rgba(0,0,0,0.2); }
.ai-trigger-text { margin: 0; padding: 2px 8px; font-size: 14px; font-weight: 500; color: white; background-color: rgba(0, 0, 0, 0.5); border-radius: var(--border-radius-base); }

.chat-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 999; }
.chat-window { width: 90%; max-width: 450px; height: 80vh; max-height: 700px; background: #f9f9f9; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); display: flex; flex-direction: column; overflow: hidden; }
.chat-header { padding: 15px 20px; background-color: #ffffff; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.chat-header h3 { margin: 0; color: var(--text-color-primary); font-size: 1.2rem; }
.close-chat-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #999; padding: 0; line-height: 1; }
.chat-messages { flex-grow: 1; overflow-y: auto; padding: 20px; }
.chat-input-area { padding: 10px 15px; background-color: #fff; border-top: 1px solid var(--border-color); display: flex; gap: 10px; align-items: center; }
.chat-input-area textarea { flex: 1; border: 1px solid #ccc; border-radius: 20px; padding: 10px 15px; font-size: 1rem; resize: none; max-height: 120px; overflow-y: auto; line-height: 1.4; font-family: inherit; background: #f4f4f4; }
.chat-input-area textarea:focus { outline: none; border-color: var(--primary-color); background: #fff; }
.send-button { flex-shrink: 0; background-color: var(--success-color); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; padding: 0; font-size: 0.9rem; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s ease; }
.send-button:hover:not(:disabled) { background-color: var(--success-color-hover); transform: scale(1.1); }
.send-button:disabled { background-color: #bdc3c7; cursor: not-allowed; transform: scale(1); }

.chat-avatar { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.message-wrapper { display: flex; margin-bottom: 15px; align-items: flex-start; gap: 12px; }
.wrapper-user { justify-content: flex-end; }
.wrapper-ai { justify-content: flex-start; }
.message-bubble { max-width: 85%; padding: 10px 15px; border-radius: 18px; line-height: 1.5; word-wrap: break-word; white-space: pre-wrap; }
.bubble-user { background-color: var(--primary-color); color: white; border-bottom-right-radius: 4px; }
.bubble-ai { background-color: #ffffff; color: #333; border: 1px solid #eee; border-top-left-radius: 4px; }
.prose { font-size: 1rem; }
.prose :first-child { margin-top: 0; }
.prose :last-child { margin-bottom: 0; }
.prose p, .prose ul, .prose ol, .prose pre { margin-bottom: 0.8em; }
</style>