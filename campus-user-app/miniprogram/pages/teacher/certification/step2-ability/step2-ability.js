const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    certImgs: [],
    videoUrl: '',
    isSubmitting: false
  },

  // 选择证书图片
  chooseCertImage() {
    wx.chooseImage({
      count: 9 - this.data.certImgs.length,
      success: async (res) => {
        const tempPaths = res.tempFilePaths;
        for (let path of tempPaths) {
          try {
            const uploadRes = await request.upload(api.file.upload, path, { folder: 'cert' });
            const url = typeof uploadRes === 'string' ? uploadRes : uploadRes.url;
            this.setData({
              certImgs: [...this.data.certImgs, url]
            });
          } catch (e) {
            console.error('上传失败', e);
          }
        }
      }
    });
  },

  deleteImg(e) {
    const index = e.currentTarget.dataset.index;
    const list = this.data.certImgs;
    list.splice(index, 1);
    this.setData({ certImgs: list });
  },

  chooseVideo() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      success: async (res) => {
        const path = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '视频处理中...' });
        this.setData({ videoUrl: path });
        wx.hideLoading();
      }
    });
  },

  async submitAll() {
    this.setData({ isSubmitting: true });

    try {
      // 1. 获取 Step 1 的数据
      const step1Data = wx.getStorageSync('cert_step1') || {};

      // 2. 组装最终数据
      const postData = {
        ...step1Data,
        idCardFrontUrl: step1Data.studentCardUrl,
        idCardBackUrl: step1Data.studentCardUrl,
        certificateUrls: this.data.certImgs
      };

      console.log('提交认证数据:', postData);

      // 3. 调用提交接口
      await request.post(api.tutor.certification, postData);

      wx.showToast({ title: '提交成功' });

      // 4. 标记认证已提交
      wx.setStorageSync('certificationSubmitted', true);

      // 5. 清除缓存并跳转
      wx.removeStorageSync('cert_step1');
      wx.redirectTo({
        url: '../step3-result/step3-result'
      });

    } catch (err) {
      console.error(err);
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});