// app.js
App({
  onLaunch: function () {
    this.globalData = {
      env: "",
      baseUrl: "http://localhost:8080"
    };
    // 注意：本项目使用自有后端，不使用微信云开发，已注释掉云初始化代码
    // if (!wx.cloud) {
    //   console.error("请使用 2.2.3 或以上的基础库以使用云能力");
    // } else {
    //   wx.cloud.init({
    //     env: this.globalData.env,
    //     traceUser: true,
    //   });
    // }
  },
  globalData: {
    env: "",
    baseUrl: "http://localhost:8080"
  }
});
