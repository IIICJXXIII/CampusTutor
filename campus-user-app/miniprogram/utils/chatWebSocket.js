/**
 * WebSocket 聊天服务
 * 用于管理小程序与后端的 WebSocket 连接
 */

const getWsBaseUrl = () => {
    const api = require('../config/apiConfig.js');
    const httpUrl = api.BASE_URL || 'http://localhost:8080';
    return httpUrl.replace('http://', 'ws://').replace('https://', 'wss://');
};

class ChatWebSocket {
    constructor() {
        this.socketTask = null;
        this.isConnected = false;
        this.isConnecting = false; // 防止重复连接
        this.reconnectCount = 0;
        this.maxReconnect = 5;
        this.heartbeatTimer = null;
        this.messageCallbacks = [];
        this.connectCallbacks = [];
        this.connectPromise = null; // 保存连接 Promise
        this.reconnectTimer = null; // 重连定时器
        this.messageQueue = []; // 消息队列，用于连接建立后发送
    }

    /**
     * 连接 WebSocket（带防重复机制）
     */
    connect() {
        // 已连接，直接返回
        if (this.isConnected && this.socketTask) {
            console.log('WebSocket 已连接');
            return Promise.resolve();
        }

        // 正在连接中，返回同一个 Promise
        if (this.isConnecting && this.connectPromise) {
            console.log('WebSocket 正在连接中，等待...');
            return this.connectPromise;
        }

        const token = wx.getStorageSync('token');
        if (!token) {
            console.warn('未登录，无法连接 WebSocket');
            return Promise.reject(new Error('未登录'));
        }

        this.isConnecting = true;

        this.connectPromise = new Promise((resolve, reject) => {
            // 先关闭旧连接
            if (this.socketTask) {
                console.log('关闭旧的 WebSocket 连接');
                try {
                    this.socketTask.close();
                } catch (e) { }
                this.socketTask = null;
            }

            const wsUrl = `${getWsBaseUrl()}/ws/chat?token=${encodeURIComponent(token)}`;
            console.log('正在连接 WebSocket:', wsUrl);

            this.socketTask = wx.connectSocket({
                url: wsUrl,
                success: () => {
                    console.log('WebSocket 连接请求已发送');
                },
                fail: (err) => {
                    console.error('WebSocket 连接请求失败:', err);
                    this.isConnecting = false;
                    this.socketTask = null;
                    this.connectPromise = null;
                    // 触发重连
                    this.scheduleReconnect();
                    reject(err);
                }
            });

            this.socketTask.onOpen(() => {
                console.log('WebSocket 连接已建立');
                this.isConnected = true;
                this.isConnecting = false;
                this.reconnectCount = 0;
                this.startHeartbeat();
                this.connectCallbacks.forEach(cb => cb(true));
                // 发送队列中的消息
                this.flushMessageQueue();
                resolve();
            });

            this.socketTask.onMessage((res) => {
                try {
                    const data = JSON.parse(res.data);
                    // 只打印非 pong 消息
                    if (data.type !== 'pong') {
                        console.log('收到 WebSocket 消息:', data);
                    }
                    this.handleMessage(data);
                } catch (e) {
                    console.error('解析消息失败:', e);
                }
            });

            this.socketTask.onClose((res) => {
                console.log('WebSocket 连接已关闭:', res);
                this.isConnected = false;
                this.isConnecting = false;
                this.socketTask = null;
                this.connectPromise = null;
                this.stopHeartbeat();
                this.connectCallbacks.forEach(cb => cb(false));

                // 只在非正常关闭时尝试重连
                if (res.code !== 1000 && this.reconnectCount < this.maxReconnect) {
                    this.scheduleReconnect();
                }
            });

            this.socketTask.onError((err) => {
                console.error('WebSocket 错误:', err);
                this.isConnected = false;
                this.isConnecting = false;
                this.connectPromise = null;
                // 触发重连
                this.scheduleReconnect();
                reject(err);
            });
        });

        return this.connectPromise;
    }

    /**
     * 调度重连，使用指数退避策略
     */
    scheduleReconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
        }

        this.reconnectCount++;
        if (this.reconnectCount > this.maxReconnect) {
            console.error('WebSocket 重连失败，已达到最大重试次数');
            return;
        }

        // 指数退避策略：1s, 2s, 4s, 8s, 16s
        const delay = Math.min(1000 * Math.pow(2, this.reconnectCount - 1), 16000);
        console.log(`WebSocket 计划在 ${delay}ms 后重连 (${this.reconnectCount}/${this.maxReconnect})...`);

        this.reconnectTimer = setTimeout(() => {
            this.connect().catch(err => {
                console.error('WebSocket 重连失败:', err);
            });
        }, delay);
    }

    handleMessage(data) {
        const { type } = data;

        switch (type) {
            case 'connected':
                console.log('服务器确认连接:', data.message, 'userId:', data.userId);
                break;
            case 'message':
                this.messageCallbacks.forEach(cb => cb('receive', data.data));
                break;
            case 'sent':
                this.messageCallbacks.forEach(cb => cb('sent', data.data));
                break;
            case 'read':
                this.messageCallbacks.forEach(cb => cb('read', data.readBy));
                break;
            case 'pong':
                break;
            case 'error':
                console.error('服务器错误:', data.message);
                this.messageCallbacks.forEach(cb => cb('error', data.message));
                break;
            default:
                console.log('未知消息类型:', type, data);
        }
    }

    /**
     * 发送消息
     */
    sendMessage(receiverId, content, msgType = 1) {
        if (!this.isConnected || !this.socketTask) {
            console.warn('WebSocket 未连接，将消息加入队列');
            // 将消息加入队列，等待连接建立后发送
            this.messageQueue.push({ receiverId, content, msgType });
            // 尝试连接
            this.connect().catch(err => {
                console.error('WebSocket 连接失败，无法发送消息:', err);
            });
            return false;
        }

        const message = {
            type: 'send',
            receiverId: Number(receiverId),
            content: content,
            msgType: Number(msgType)
        };

        console.log('发送 WebSocket 消息:', message);

        this.socketTask.send({
            data: JSON.stringify(message),
            success: () => console.log('WebSocket 消息发送成功'),
            fail: (err) => {
                console.error('WebSocket 消息发送失败:', err);
                // 发送失败，将消息加入队列
                this.messageQueue.push({ receiverId, content, msgType });
            }
        });

        return true;
    }

    /**
     * 发送队列中的消息
     */
    flushMessageQueue() {
        if (this.messageQueue.length === 0) return;

        console.log(`WebSocket 连接已建立，发送队列中的 ${this.messageQueue.length} 条消息`);
        while (this.messageQueue.length > 0) {
            const msg = this.messageQueue.shift();
            this.sendMessage(msg.receiverId, msg.content, msg.msgType);
        }
    }

    /**
     * 标记消息已读
     */
    markAsRead(senderId) {
        if (!this.isConnected || !this.socketTask) {
            console.warn('WebSocket 未连接，无法标记已读');
            return;
        }

        this.socketTask.send({
            data: JSON.stringify({
                type: 'read',
                receiverId: Number(senderId)
            }),
            fail: (err) => console.error('标记已读失败:', err)
        });
    }

    onMessage(callback) {
        // 防止重复注册
        if (!this.messageCallbacks.includes(callback)) {
            this.messageCallbacks.push(callback);
        }
    }

    offMessage(callback) {
        this.messageCallbacks = this.messageCallbacks.filter(cb => cb !== callback);
    }

    onConnect(callback) {
        if (!this.connectCallbacks.includes(callback)) {
            this.connectCallbacks.push(callback);
        }
        // 如果已连接，立即调用
        if (this.isConnected) {
            callback(true);
        }
    }

    offConnect(callback) {
        this.connectCallbacks = this.connectCallbacks.filter(cb => cb !== callback);
    }

    /**
     * 开始心跳检测
     */
    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatTimer = setInterval(() => {
            if (this.isConnected && this.socketTask) {
                this.socketTask.send({
                    data: JSON.stringify({ type: 'ping' }),
                    fail: () => {
                        console.warn('心跳发送失败，可能连接已断开');
                        // 心跳失败，触发重连
                        this.scheduleReconnect();
                    }
                });
            }
        }, 30000);
    }

    /**
     * 停止心跳检测
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
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
        if (this.socketTask) {
            this.socketTask.close();
            this.socketTask = null;
        }
        this.isConnected = false;
        this.isConnecting = false;
        this.connectPromise = null;
        this.messageQueue = [];
    }

    /**
     * 获取连接状态
     */
    getStatus() {
        return this.isConnected;
    }
}

// 单例模式
const chatWebSocket = new ChatWebSocket();

module.exports = chatWebSocket;
