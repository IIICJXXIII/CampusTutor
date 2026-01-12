const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');
const chatWebSocket = require('../../../utils/chatWebSocket.js');

Page({
    data: {
        targetUserId: null,
        targetNickname: '',
        targetAvatar: '',
        messages: [],
        inputValue: '',
        loading: true,
        sending: false,
        scrollToMessage: '',
        currentUserId: null,
        wsConnected: false,
        // 消息发送状态管理
        messageStatus: {} // key: messageId, value: 'sending' | 'success' | 'failed'
    },

    // WebSocket 消息回调
    messageHandler: null,
    connectHandler: null,

    onLoad(options) {
        const { userId, nickname, avatar } = options;
        const userInfo = wx.getStorageSync('userInfo') || {};

        this.setData({
            targetUserId: parseInt(userId),
            targetNickname: decodeURIComponent(nickname || '用户'),
            targetAvatar: decodeURIComponent(avatar || ''),
            currentUserId: userInfo.userId || userInfo.id
        });

        wx.setNavigationBarTitle({
            title: decodeURIComponent(nickname || '聊天')
        });

        // 注册回调
        this.registerCallbacks();

        // 加载历史消息
        this.loadMessages();

        // 连接 WebSocket
        this.connectWebSocket();
    },

    onShow() {
        // 同步连接状态
        this.setData({ wsConnected: chatWebSocket.getStatus() });

        // 如果未连接，尝试连接
        if (!chatWebSocket.getStatus()) {
            this.connectWebSocket();
        } else {
            // 已连接，标记已读
            this.markAsRead();
        }
    },

    onUnload() {
        // 移除回调（但不断开连接）
        if (this.messageHandler) {
            chatWebSocket.offMessage(this.messageHandler);
            this.messageHandler = null;
        }
        if (this.connectHandler) {
            chatWebSocket.offConnect(this.connectHandler);
            this.connectHandler = null;
        }
    },

    // 注册 WebSocket 回调
    registerCallbacks() {
        // 消息回调
        this.messageHandler = (type, data) => {
            this.handleWebSocketMessage(type, data);
        };
        chatWebSocket.onMessage(this.messageHandler);

        // 连接状态回调
        this.connectHandler = (connected) => {
            console.log('连接状态变化:', connected);
            this.setData({ wsConnected: connected });
        };
        chatWebSocket.onConnect(this.connectHandler);
    },

    // 连接 WebSocket
    async connectWebSocket() {
        try {
            await chatWebSocket.connect();
            this.setData({ wsConnected: true });
            this.markAsRead();
        } catch (err) {
            console.error('WebSocket 连接失败:', err);
            this.setData({ wsConnected: false });
        }
    },

    // 处理 WebSocket 消息
    handleWebSocketMessage(type, data) {
        const { targetUserId, currentUserId } = this.data;

        if (type === 'receive' || type === 'sent') {
            const senderId = data.senderId;
            const receiverId = data.receiverId;

            // 检查消息是否属于当前会话
            if ((senderId == targetUserId && receiverId == currentUserId) ||
                (senderId == currentUserId && receiverId == targetUserId)) {

                const newMessage = {
                    ...data,
                    isMine: senderId == currentUserId
                };

                // 检查是否已存在
                const exists = this.data.messages.some(m => m.id === newMessage.id);
                if (!exists) {
                    // 移除待发送的临时消息（如果有）
                    const messages = this.data.messages.filter(m => !m.pending || m.content !== newMessage.content);
                    this.setData({
                        messages: [...messages, newMessage]
                    });
                    this.scrollToBottom();
                }

                // 更新消息状态
                if (type === 'sent' && senderId == currentUserId) {
                    this.updateMessageStatus(data.id, 'success');
                }

                // 收到消息，标记已读
                if (type === 'receive') {
                    this.markAsRead();
                }
            }
        } else if (type === 'error') {
            wx.showToast({ title: '发送失败: ' + data, icon: 'none' });
            // 更新所有发送中消息的状态为失败
            Object.keys(this.data.messageStatus).forEach(msgId => {
                if (this.data.messageStatus[msgId] === 'sending') {
                    this.updateMessageStatus(msgId, 'failed');
                }
            });
        }
    },

    async loadMessages() {
        const { targetUserId } = this.data;
        if (!targetUserId) return;

        try {
            const messages = await request.get(api.chat.history(targetUserId));

            const sortedMessages = (messages || []).sort((a, b) =>
                new Date(a.createTime) - new Date(b.createTime)
            );

            this.setData({
                messages: sortedMessages,
                loading: false
            });

            this.scrollToBottom();
        } catch (err) {
            console.error('加载消息失败:', err);
            this.setData({ loading: false });
        }
    },

    markAsRead() {
        const { targetUserId, wsConnected } = this.data;
        if (!targetUserId) return;

        if (wsConnected && chatWebSocket.getStatus()) {
            chatWebSocket.markAsRead(targetUserId);
        } else {
            request.post(api.chat.markRead(targetUserId)).catch(() => { });
        }
    },

    scrollToBottom() {
        setTimeout(() => {
            const lastMsg = this.data.messages[this.data.messages.length - 1];
            if (lastMsg) {
                this.setData({ scrollToMessage: `msg-${lastMsg.id}` });
            }
        }, 100);
    },

    onInputChange(e) {
        this.setData({ inputValue: e.detail.value });
    },

    // 发送消息
    async sendMessage() {
        const { inputValue, targetUserId, sending, currentUserId } = this.data;
        const content = inputValue.trim();

        if (!content || sending) return;

        this.setData({ sending: true, inputValue: '' });

        // 检查实时连接状态
        const wsConnected = chatWebSocket.getStatus();

        if (wsConnected) {
            // 通过 WebSocket 发送
            const success = chatWebSocket.sendMessage(targetUserId, content, 1);

            if (success) {
                // 乐观更新
                const tempMessage = {
                    id: 'temp_' + Date.now(),
                    senderId: currentUserId,
                    receiverId: targetUserId,
                    content,
                    msgType: 1,
                    createTime: new Date().toISOString(),
                    isMine: true,
                    pending: true
                };
                
                // 更新消息状态
                this.updateMessageStatus(tempMessage.id, 'sending');
                
                this.setData({
                    messages: [...this.data.messages, tempMessage],
                    sending: false
                });
                this.scrollToBottom();
            } else {
                // WebSocket 发送失败，降级到 HTTP
                await this.sendViaHttp(content);
            }
        } else {
            // 使用 HTTP 发送
            await this.sendViaHttp(content);
        }
    },

    // HTTP 方式发送消息
    async sendViaHttp(content) {
        const { targetUserId, currentUserId } = this.data;

        try {
            // 创建临时消息
            const tempMessage = {
                id: 'temp_' + Date.now(),
                senderId: currentUserId,
                receiverId: targetUserId,
                content,
                msgType: 1,
                createTime: new Date().toISOString(),
                isMine: true,
                pending: true
            };
            
            // 更新消息状态
            this.updateMessageStatus(tempMessage.id, 'sending');
            
            // 添加到消息列表
            this.setData({
                messages: [...this.data.messages, tempMessage]
            });
            this.scrollToBottom();

            const messageId = await request.post(api.chat.send, {
                targetUserId,
                content,
                type: 'text'
            });

            // 移除临时消息，添加正式消息
            const messages = this.data.messages.filter(m => m.id !== tempMessage.id);
            const newMessage = {
                id: messageId,
                senderId: currentUserId,
                receiverId: targetUserId,
                content,
                msgType: 1,
                createTime: new Date().toISOString(),
                isMine: true
            };

            // 更新消息状态
            this.updateMessageStatus(tempMessage.id, 'success');

            this.setData({
                messages: [...messages, newMessage],
                sending: false
            });
            this.scrollToBottom();
        } catch (err) {
            console.error('HTTP 发送失败:', err);
            // 更新消息状态为失败
            Object.keys(this.data.messageStatus).forEach(msgId => {
                if (this.data.messageStatus[msgId] === 'sending') {
                    this.updateMessageStatus(msgId, 'failed');
                }
            });
            this.setData({
                sending: false,
                inputValue: content
            });
            wx.showToast({ title: '发送失败', icon: 'none' });
        }
    },

    // 刷新消息
    refreshMessages() {
        this.loadMessages();
    },

    // 更新消息状态
    updateMessageStatus(messageId, status) {
        const messageStatus = { ...this.data.messageStatus };
        messageStatus[messageId] = status;
        this.setData({ messageStatus });
    },

    // 重试发送失败的消息
    async retrySendMessage(messageId) {
        const message = this.data.messages.find(m => m.id === messageId);
        if (!message || message.isMine === false) return;

        // 更新消息状态为发送中
        this.updateMessageStatus(messageId, 'sending');

        const { targetUserId, currentUserId } = this.data;
        const content = message.content;

        // 检查实时连接状态
        const wsConnected = chatWebSocket.getStatus();

        if (wsConnected) {
            // 通过 WebSocket 发送
            const success = chatWebSocket.sendMessage(targetUserId, content, 1);

            if (!success) {
                // WebSocket 发送失败，降级到 HTTP
                await this.sendViaHttp(content);
            }
        } else {
            // 使用 HTTP 发送
            await this.sendViaHttp(content);
        }
    },

    // 显示消息通知
    showMessageNotification(message) {
        if (message && message.content) {
            // 只在应用在后台时显示通知
            wx.getApp().getCurrentPages().length === 1 && wx.showToast({
                title: `收到新消息: ${message.content.substring(0, 20)}${message.content.length > 20 ? '...' : ''}`,
                icon: 'none',
                duration: 3000
            });
        }
    }
});
