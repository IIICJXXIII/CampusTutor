/**
 * WebSocket 连接管理工具类
 * 用于管理与后端 WebSocket 服务的连接
 */

class WebSocketClient {
    constructor() {
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
        this.messageHandlers = [];
        this.connectionHandlers = [];
        this.isConnecting = false;
    }

    /**
     * 建立 WebSocket 连接
     */
    connect() {
        if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
            console.log('WebSocket 已连接或正在连接中');
            return;
        }

        const token = localStorage.getItem('token');
        if (!token) {
            console.warn('未找到 token，无法建立 WebSocket 连接');
            return;
        }

        this.isConnecting = true;

        // 根据当前页面协议决定使用 ws 还是 wss
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws/chat?token=${token}`;

        console.log('正在连接 WebSocket:', wsUrl);

        try {
            this.ws = new WebSocket(wsUrl);
            this.setupEventHandlers();
        } catch (error) {
            console.error('WebSocket 连接失败:', error);
            this.isConnecting = false;
            this.scheduleReconnect();
        }
    }

    /**
     * 设置 WebSocket 事件处理器
     */
    setupEventHandlers() {
        this.ws.onopen = () => {
            console.log('WebSocket 连接成功');
            this.isConnecting = false;
            this.reconnectAttempts = 0;

            // 通知所有连接状态监听器
            this.connectionHandlers.forEach(handler => handler('connected'));

            // 启动心跳
            this.startHeartbeat();
        };

        this.ws.onmessage = (event) => {
            try {
                const message = JSON.parse(event.data);
                console.log('收到 WebSocket 消息:', message);

                // 通知所有消息处理器
                this.messageHandlers.forEach(handler => handler(message));
            } catch (error) {
                console.error('解析 WebSocket 消息失败:', error);
            }
        };

        this.ws.onclose = (event) => {
            console.log('WebSocket 连接关闭:', event.code, event.reason);
            this.isConnecting = false;
            this.stopHeartbeat();

            // 通知所有连接状态监听器
            this.connectionHandlers.forEach(handler => handler('disconnected'));

            // 非正常关闭时尝试重连
            if (event.code !== 1000) {
                this.scheduleReconnect();
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket 错误:', error);
            this.isConnecting = false;
        };
    }

    /**
     * 安排重新连接
     */
    scheduleReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('WebSocket 重连次数已达上限');
            return;
        }

        this.reconnectAttempts++;
        const delay = this.reconnectDelay * this.reconnectAttempts;

        console.log(`将在 ${delay}ms 后进行第 ${this.reconnectAttempts} 次重连`);

        setTimeout(() => {
            this.connect();
        }, delay);
    }

    /**
     * 发送消息
     * @param {Object} message 消息对象
     */
    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
            return true;
        } else {
            console.warn('WebSocket 未连接，无法发送消息');
            return false;
        }
    }

    /**
     * 发送聊天消息
     * @param {number} receiverId 接收者ID
     * @param {string} content 消息内容
     * @param {number} msgType 消息类型 (1-文本, 2-图片)
     */
    sendChatMessage(receiverId, content, msgType = 1) {
        return this.send({
            type: 'send',
            receiverId,
            content,
            msgType
        });
    }

    /**
     * 发送已读通知
     * @param {number} senderId 发送者ID（对方）
     */
    sendReadNotification(senderId) {
        return this.send({
            type: 'read',
            receiverId: senderId
        });
    }

    /**
     * 注册消息处理器
     * @param {Function} handler 处理函数
     * @returns {Function} 取消注册的函数
     */
    onMessage(handler) {
        this.messageHandlers.push(handler);
        return () => {
            const index = this.messageHandlers.indexOf(handler);
            if (index > -1) {
                this.messageHandlers.splice(index, 1);
            }
        };
    }

    /**
     * 注册连接状态处理器
     * @param {Function} handler 处理函数
     * @returns {Function} 取消注册的函数
     */
    onConnectionChange(handler) {
        this.connectionHandlers.push(handler);
        return () => {
            const index = this.connectionHandlers.indexOf(handler);
            if (index > -1) {
                this.connectionHandlers.splice(index, 1);
            }
        };
    }

    /**
     * 启动心跳
     */
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            this.send({ type: 'ping' });
        }, 30000); // 每30秒发送一次心跳
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close(1000, '用户主动断开');
            this.ws = null;
        }
        this.messageHandlers = [];
        this.connectionHandlers = [];
    }

    /**
     * 获取连接状态
     */
    isConnected() {
        return this.ws && this.ws.readyState === WebSocket.OPEN;
    }
}

// 导出单例
export default new WebSocketClient();
