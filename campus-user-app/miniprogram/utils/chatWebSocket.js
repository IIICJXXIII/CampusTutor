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
                    this.reconnectCount++;
                    console.log(`尝试重连 (${this.reconnectCount}/${this.maxReconnect})...`);
                    setTimeout(() => this.connect(), 3000);
                }
            });

            this.socketTask.onError((err) => {
                console.error('WebSocket 错误:', err);
                this.isConnected = false;
                this.isConnecting = false;
                this.connectPromise = null;
                reject(err);
            });
        });

        return this.connectPromise;
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
            console.warn('WebSocket 未连接，无法发送。isConnected:', this.isConnected);
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
            fail: (err) => console.error('WebSocket 消息发送失败:', err)
        });

        return true;
    }

    markAsRead(senderId) {
        if (!this.isConnected || !this.socketTask) return;

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

    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatTimer = setInterval(() => {
            if (this.isConnected && this.socketTask) {
                this.socketTask.send({
                    data: JSON.stringify({ type: 'ping' }),
                    fail: () => { } // 静默失败
                });
            }
        }, 30000);
    }

    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    disconnect() {
        this.stopHeartbeat();
        if (this.socketTask) {
            this.socketTask.close();
            this.socketTask = null;
        }
        this.isConnected = false;
        this.isConnecting = false;
        this.connectPromise = null;
    }

    getStatus() {
        return this.isConnected;
    }
}

// 单例模式
const chatWebSocket = new ChatWebSocket();

module.exports = chatWebSocket;
