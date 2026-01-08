const request = require('../../../utils/request.js');
const api = require('../../../config/apiConfig.js');

Page({
  data: {
    id: null,
    item: {},
    loading: true
  },

  onLoad(options) {
    if (options && options.id) {
      this.setData({ id: options.id });
      this.loadDetail(options.id);
    }
  },

  async loadDetail(id) {
    this.setData({ loading: true });
    try {
      const res = await request.get(api.study.wrongbook.detail(id));
      const d = res || {};
      const item = {
        id: d.id || id,
        questionTitle: d.title || d.question || '题目',
        subject: d.subject || '',
        createdAt: d.createdAt || d.createTime || '',
        content: d.content || d.questionText || '',
        analysis: d.analysis || d.answer || ''
      };
      this.setData({ item });
    } catch (err) {
      console.warn('获取错题详情失败，使用示例数据：', err);
      this.setData({ item: { id, questionTitle: '示例题：对数换底公式是什么？', subject: '数学', createdAt: '2026-01-07', content: '求换底公式证明', analysis: '换底公式：log_a b = log_c b / log_c a' } });
    } finally {
      this.setData({ loading: false });
    }
  }
});
