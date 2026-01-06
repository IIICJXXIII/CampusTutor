import request from '../../../../utils/request';
import api from '../../../../config/apiConfig';

Page({
  data: {
    certImgs: [], // 预览用的数组
    videoUrl: '',
    isSubmitting: false
  },

  // 选择证书图片
  chooseCertImage() {
    wx.chooseImage({
      count: 9 - this.data.certImgs.length,
      success: async (res) => {
        const tempPaths = res.tempFilePaths;
        // 循环上传（实际开发建议并发上传）
        for (let path of tempPaths) {
          try {
             const url = await request.upload(api.file.upload, path, { folder: 'cert' });
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

  // 选择视频 (暂不支持上传大文件，仅演示)
  chooseVideo() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      success: async (res) => {
        const path = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '视频处理中...' });
        // 视频上传逻辑同图片
        // const url = await request.upload(api.file.upload, path, { folder: 'video' });
        // this.setData({ videoUrl: url });
        
        // 演示：直接使用本地路径
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
        // 补充 Step 1 中没填但接口需要的字段 (mock)
        idCardFrontUrl: step1Data.studentCardUrl, // 暂用学生证代替
        idCardBackUrl: step1Data.studentCardUrl,
        certificateUrls: this.data.certImgs // 数组
        // videoUrl: this.data.videoUrl (接口如果没定义视频字段，暂时忽略)
      };

      console.log('提交认证数据:', postData);

      // 3. 调用提交接口
      await request.post(api.tutor.certification, postData);

      wx.showToast({ title: '提交成功' });
      
      // 4. 清除缓存并跳转
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