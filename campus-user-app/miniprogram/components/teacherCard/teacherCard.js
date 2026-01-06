Component({
  properties: {
    teacher: {
      type: Object,
      value: {}
    }
  },
  methods: {
    onTap() {
      // 注意：后端返回的主键可能是 id 或 userId，根据 API 文档详情页通常用 tutorId
      // 这里传递整个对象方便页面判断
      this.triggerEvent('click', { id: this.properties.teacher.id });
    }
  }
});