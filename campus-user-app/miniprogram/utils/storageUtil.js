/**
 * 本地存储工具类 - storageUtil.js
 */

module.exports = {
    /**
     * 获取用户信息
     */
    getUserInfo() {
        try {
            return wx.getStorageSync('userInfo') || null;
        } catch (e) {
            console.error('获取用户信息失败:', e);
            return null;
        }
    },

    /**
     * 设置用户信息
     */
    setUserInfo(userInfo) {
        try {
            wx.setStorageSync('userInfo', userInfo);
        } catch (e) {
            console.error('设置用户信息失败:', e);
        }
    },

    /**
     * 获取 Token
     */
    getToken() {
        try {
            return wx.getStorageSync('token') || '';
        } catch (e) {
            console.error('获取Token失败:', e);
            return '';
        }
    },

    /**
     * 设置 Token
     */
    setToken(token) {
        try {
            wx.setStorageSync('token', token);
        } catch (e) {
            console.error('设置Token失败:', e);
        }
    },

    /**
     * 清除所有存储
     */
    clear() {
        try {
            wx.clearStorageSync();
        } catch (e) {
            console.error('清除存储失败:', e);
        }
    },

    /**
     * 获取指定 key 的值
     */
    get(key) {
        try {
            return wx.getStorageSync(key);
        } catch (e) {
            console.error('获取存储失败:', e);
            return null;
        }
    },

    /**
     * 设置指定 key 的值
     */
    set(key, value) {
        try {
            wx.setStorageSync(key, value);
        } catch (e) {
            console.error('设置存储失败:', e);
        }
    },

    /**
     * 删除指定 key
     */
    remove(key) {
        try {
            wx.removeStorageSync(key);
        } catch (e) {
            console.error('删除存储失败:', e);
        }
    }
};
