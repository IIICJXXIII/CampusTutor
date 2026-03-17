// aiAssistant.js
// AI智能助手组件
const api = require('../../config/apiConfig.js');
const request = require('../../utils/request.js');

Component({
  options: {
    multipleSlots: true
  },
  data: {
    isChatOpen: false,
    messages: [],
    inputValue: '',
    isLoading: false,
    unreadCount: 0,
    showQuickQuestions: true,
    scrollToView: '',
    quickQuestions: [
      '如何发布家教需求？',
      '如何成为认证教师？',
      '平台如何保障教学质量？',
      '如何查看附近的家教需求？',
      '如何处理退款问题？'
    ]
  },
  methods: {
    // 切换聊天窗口
    toggleChatWindow() {
      this.setData({
        isChatOpen: !this.data.isChatOpen,
        unreadCount: 0
      });
    },

    // 关闭聊天窗口
    closeChatWindow() {
      this.setData({
        isChatOpen: false
      });
    },

    // 处理输入
    handleInput(e) {
      this.setData({
        inputValue: e.detail.value
      });
    },

    // 发送消息
    sendMessage() {
      const content = this.data.inputValue.trim();
      if (!content) return;

      // 添加用户消息
      const userMessage = {
        id: Date.now() + Math.random(),
        sender: 'user',
        content: content,
        timestamp: new Date().toLocaleTimeString()
      };

      this.setData({
        messages: [...this.data.messages, userMessage],
        inputValue: '',
        isLoading: true,
        showQuickQuestions: false,
        scrollToView: `message-${userMessage.id}`
      });

      // 调用AI接口
      this.callAiApi(content);
    },

    // 发送快捷问题
    sendQuickQuestion(e) {
      const question = e.currentTarget.dataset.question;
      
      // 添加用户消息
      const userMessage = {
        id: Date.now() + Math.random(),
        sender: 'user',
        content: question,
        timestamp: new Date().toLocaleTimeString()
      };

      this.setData({
        messages: [...this.data.messages, userMessage],
        isLoading: true,
        showQuickQuestions: false,
        scrollToView: `message-${userMessage.id}`
      });

      // 调用AI接口
      this.callAiApi(question);
    },

    // 调用AI接口
    callAiApi(content) {
      // 获取最近10条历史记录构建上下文
      const historyContext = this.data.messages.slice(-10).map(msg => ({
        role: msg.sender === 'user' ? 'user' : 'assistant',
        content: msg.content
      }));

      // 真实API调用
      wx.request({
        url: api.llm.chat,
        method: 'POST',
        data: {
          messages: historyContext,
          scene: 'general'
        },
        header: {
          'Content-Type': 'application/json',
          'token': wx.getStorageSync('token') || ''
        },
        success: (res) => {
          try {
            if (res.statusCode === 200 && res.data.code === 200) {
              const aiResponseData = res.data.data || {};
              // 添加数据类型验证
              let aiResponse = '';
              
              if (typeof aiResponseData === 'string') {
                aiResponse = aiResponseData;
              } else if (typeof aiResponseData === 'object' && aiResponseData !== null) {
                if (aiResponseData.content !== undefined) {
                  if (typeof aiResponseData.content === 'string') {
                    aiResponse = aiResponseData.content;
                  } else {
                    // 处理非字符串类型的content
                    aiResponse = JSON.stringify(aiResponseData.content);
                  }
                } else {
                  // 处理没有content字段的情况
                  aiResponse = JSON.stringify(aiResponseData);
                }
              } else {
                // 处理其他数据类型
                aiResponse = String(aiResponseData);
              }
              
              // 添加AI消息
              const aiMessage = {
                id: Date.now() + Math.random(),
                sender: 'ai',
                content: aiResponse,
                timestamp: new Date().toLocaleTimeString()
              };

              this.setData({
                messages: [...this.data.messages, aiMessage],
                isLoading: false,
                scrollToView: `message-${aiMessage.id}`
              });
            } else {
              this.handleApiError();
            }
          } catch (error) {
            console.error('处理AI响应失败:', error);
            this.handleApiError();
          }
        },
        fail: (err) => {
          console.error('AI接口调用失败:', err);
          // 调用失败时使用模拟回复
          this.mockAiResponse(content);
        }
      });
    },

    // 处理API错误
    handleApiError() {
      const errorMessage = {
        id: Date.now() + Math.random(),
        sender: 'ai',
        content: '抱歉，AI服务暂时不可用，请稍后再试。',
        timestamp: new Date().toLocaleTimeString()
      };

      this.setData({
        messages: [...this.data.messages, errorMessage],
        isLoading: false,
        scrollToView: `message-${errorMessage.id}`
      });
    },

    // 模拟AI回复（用于测试）
    mockAiResponse(content) {
      setTimeout(() => {
        let response = '';
        
        // 简单的关键词匹配
        if (content.includes('发布需求') || content.includes('需求')) {
          response = '发布家教需求的步骤：1. 进入首页点击"发布需求"按钮 2. 填写学生信息 3. 选择科目和年级 4. 设置期望价格和地点 5. 提交审核。审核通过后，您的需求会在平台上展示。';
        } else if (content.includes('认证') || content.includes('教师')) {
          response = '成为认证教师的步骤：1. 注册账号并选择教师角色 2. 进入"我的"页面点击"教师认证" 3. 上传身份证和学生证照片 4. 填写个人信息和教学专长 5. 提交审核。审核通过后，您将获得认证教师标识。';
        } else if (content.includes('质量') || content.includes('保障')) {
          response = '平台保障教学质量的措施：1. 严格的教师认证流程 2. 教学过程全程记录 3. 家长确认课时制度 4. 评价反馈机制 5. 纠纷处理服务。如有任何问题，可随时联系客服。';
        } else if (content.includes('附近') || content.includes('需求')) {
          response = '查看附近家教需求的方法：1. 进入首页点击"附近需求" 2. 允许位置权限 3. 地图上会显示附近的家教需求 4. 点击需求查看详情 5. 符合条件可直接接单。';
        } else if (content.includes('退款') || content.includes('退费')) {
          response = '处理退款问题的流程：1. 进入"我的订单"页面 2. 找到对应订单点击"申请退款" 3. 填写退款原因并提交 4. 等待平台审核 5. 审核通过后，款项将原路退回。具体退款规则可查看平台服务协议。';
        } else {
          response = `感谢您的咨询。关于"${content}"，我建议您：1. 查看平台帮助中心 2. 联系客服获取详细信息 3. 参考平台相关教程。如果您有其他问题，随时告诉我。`;
        }

        const aiMessage = {
          id: Date.now() + Math.random(),
          sender: 'ai',
          content: response,
          timestamp: new Date().toLocaleTimeString()
        };

        this.setData({
          messages: [...this.data.messages, aiMessage],
          isLoading: false,
          scrollToView: `message-${aiMessage.id}`
        });
      }, 1000);
    }
  }
});