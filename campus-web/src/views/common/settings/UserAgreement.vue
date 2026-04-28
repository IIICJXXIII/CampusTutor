<template>
  <div class="policy-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">用户协议</h1>
    </div>
    
    <div class="content-card">
      <h2>校园智教平台用户协议</h2>
      <p class="update-time">最后更新日期：2024年12月01日</p>
      
      <div class="section">
        <h3>一、服务条款</h3>
        <p>欢迎使用校园智教平台（以下简称"本平台"）。本平台致力于为家长和素质教育教师之间搭建连接桥梁。使用本平台前，请仔细阅读本协议。</p>
      </div>
      
      <div class="section">
        <h3>二、用户注册</h3>
        <ul>
          <li>您需使用真实有效的手机号码进行注册。</li>
          <li>教师用户需通过实名认证后方可接单。</li>
          <li>您应妥善保管账号密码，因保管不善导致的损失由您自行承担。</li>
        </ul>
      </div>

      <div class="section">
        <h3>三、平台规则</h3>
        <ul>
          <li>本平台仅提供<strong>素质教育</strong>类辅导服务的信息撮合，包括但不限于艺术、体育、科创等领域。</li>
          <li>严禁发布与学科类培训（如语文、数学、英语等义务教育阶段学科）相关的需求。</li>
          <li>用户不得利用平台从事违法违规活动。</li>
        </ul>
      </div>

      <div class="section">
        <h3>四、交易规则</h3>
        <ul>
          <li>家长通过平台发布需求后，教师可申请接单。</li>
          <li>订单确认后，家长需按约定支付课时费用。</li>
          <li>教师完成课时并由家长确认后，方可申请提现。</li>
          <li>如发生争议，平台将根据双方提供的证据进行调解。</li>
        </ul>
      </div>

      <div class="section">
        <h3>五、知识产权</h3>
        <p>本平台的所有内容（包括但不限于文字、图片、软件、界面设计等）均受知识产权法保护。未经书面授权，任何人不得复制、修改或传播。</p>
      </div>

      <div class="section">
        <h3>六、免责声明</h3>
        <p>本平台作为信息撮合方，不对教师的实际教学质量承担直接责任。我们鼓励双方通过沟通、试课等方式建立信任关系。</p>
      </div>

      <div class="section">
        <h3>七、协议变更</h3>
        <p>我们可能适时修订本协议，修订后的协议将在平台内公布。继续使用本平台即视为同意修订后的协议。</p>
      </div>

      <div class="section">
        <h3>八、联系方式</h3>
        <p>如有任何问题，请联系我们：</p>
        <p>邮箱：support@campustutor.com</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const goBack = () => router.back()
</script>

<style lang="scss" scoped>
.policy-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  h2 {
    font-size: 22px;
    font-weight: 700;
    text-align: center;
    margin: 0 0 8px;
  }
  
  .update-time {
    text-align: center;
    color: #999;
    font-size: 13px;
    margin-bottom: 32px;
  }
  
  .section {
    margin-bottom: 24px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 12px;
      color: #303133;
    }
    
    p {
      font-size: 14px;
      line-height: 1.8;
      color: #606266;
      margin: 0 0 8px;
    }
    
    ul {
      padding-left: 20px;
      margin: 0;
      
      li {
        font-size: 14px;
        line-height: 2;
        color: #606266;
      }
    }
  }
}
</style>
