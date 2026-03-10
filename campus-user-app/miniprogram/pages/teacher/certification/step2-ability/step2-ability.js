const request = require('../../../../utils/request.js');
const api = require('../../../../config/apiConfig.js');

Page({
  data: {
    certImgs: [],
    videoUrl: '',
    isSubmitting: false,
    // 年龄段列表
    gradeList: [
      { id: 'age_4_6', name: '4-6岁' },
      { id: 'age_7_9', name: '7-9岁' },
      { id: 'age_10_12', name: '10-12岁' },
      { id: 'age_13_15', name: '13-15岁' },
      { id: 'age_16_18', name: '16-18岁' },
      { id: 'age_18_plus', name: '18岁以上' }
    ],
    // 素质教育科目列表
    subjectList: [
      { id: 'piano', name: '钢琴/乐器陪练' },
      { id: 'art', name: '美术/书法' },
      { id: 'vocal', name: '声乐/视唱练耳' },
      { id: 'pe_exam', name: '中考体育专项' },
      { id: 'racket', name: '羽毛球/网球陪练' },
      { id: 'ball', name: '篮球/足球指导' },
      { id: 'coding', name: '少儿编程(Scratch/Python)' },
      { id: 'robot', name: '机器人/3D打印' },
      { id: 'science', name: '科学实验/航模' }
    ],
    selectedGrades: [], // 已选年级
    selectedSubjects: [], // 已选科目
    expectPrice: '' // 期望时薪
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

  // 切换年级选中状态
  toggleGrade(e) {
    const id = e.currentTarget.dataset.id;
    const selected = [...this.data.selectedGrades];
    const index = selected.indexOf(id);
    if (index > -1) {
      selected.splice(index, 1);
    } else {
      selected.push(id);
    }
    this.setData({ selectedGrades: selected });
  },

  // 切换科目选中状态
  toggleSubject(e) {
    const id = e.currentTarget.dataset.id;
    const selected = [...this.data.selectedSubjects];
    const index = selected.indexOf(id);
    if (index > -1) {
      selected.splice(index, 1);
    } else {
      selected.push(id);
    }
    this.setData({ selectedSubjects: selected });
  },

  // 输入期望时薪
  onPriceInput(e) {
    this.setData({ expectPrice: e.detail.value });
  },

  async submitAll() {
    const { selectedGrades, selectedSubjects, expectPrice } = this.data;

    // 验证必填项
    if (selectedGrades.length === 0) {
      return wx.showToast({ title: '请选择可教授年级', icon: 'none' });
    }
    if (selectedSubjects.length === 0) {
      return wx.showToast({ title: '请选择可教授科目', icon: 'none' });
    }
    if (!expectPrice || parseFloat(expectPrice) <= 0) {
      return wx.showToast({ title: '请填写期望时薪', icon: 'none' });
    }

    this.setData({ isSubmitting: true });

    try {
      // 1. 获取 Step 1 的数据
      const step1Data = wx.getStorageSync('cert_step1') || {};

      // 2. 获取选中的年级和科目名称
      const gradeNames = this.data.gradeList
        .filter(g => selectedGrades.includes(g.id))
        .map(g => g.name);
      const subjectNames = this.data.subjectList
        .filter(s => selectedSubjects.includes(s.id))
        .map(s => s.name);

      // 3. 组装最终数据
      const postData = {
        ...step1Data,
        idCardFrontUrl: step1Data.studentCardUrl,
        idCardBackUrl: step1Data.studentCardUrl,
        certificateUrls: this.data.certImgs,
        teachGrades: gradeNames,
        teachSubjects: subjectNames,
        expectPrice: parseFloat(expectPrice)
      };

      console.log('提交认证数据:', postData);

      // 4. 调用提交接口
      await request.post(api.tutor.certification, postData);

      wx.showToast({ title: '提交成功' });

      // 5. 标记认证已提交
      wx.setStorageSync('certificationSubmitted', true);

      // 6. 清除缓存并跳转
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