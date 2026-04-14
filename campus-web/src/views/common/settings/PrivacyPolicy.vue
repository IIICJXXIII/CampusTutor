<template>
  <div class="policy-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">隐私政策</h1>
    </div>
    
    <div class="content-card">
      <h2>校园智教平台隐私政策</h2>
      <p class="update-time">最后更新日期：2024年12月01日</p>
      
      <div class="section">
        <h3>一、信息收集</h3>
        <p>我们在您使用服务时收集以下信息：</p>
        <ul>
          <li><strong>账号信息</strong>：手机号码、昵称、头像等注册信息。</li>
          <li><strong>位置信息</strong>：用于"附近找老师"功能，仅在您授权后使用。</li>
          <li><strong>教学信息</strong>：教师认证资料、教学科目、排课时间等。</li>
          <li><strong>交易信息</strong>：订单记录、支付流水（不存储银行卡完整信息）。</li>
        </ul>
      </div>
      
      <div class="section">
        <h3>二、信息使用</h3>
        <p>我们使用收集的信息用于以下目的：</p>
        <ul>
          <li>提供、维护和改进我们的服务。</li>
          <li>进行教员与需求的智能匹配。</li>
          <li>处理交易并发送相关通知。</li>
          <li>预防欺诈和保障平台安全。</li>
        </ul>
      </div>

      <div class="section">
        <h3>三、信息安全</h3>
        <p>我们采用行业标准的安全措施保护您的个人信息，包括数据加密传输、权限访问控制、安全审计等技术和管理手段。</p>
      </div>

      <div class="section">
        <h3>四、信息共享</h3>
        <p>我们不会向第三方出售您的个人信息。仅在以下情况下可能共享：</p>
        <ul>
          <li>获得您的明确同意。</li>
          <li>履行法律义务或配合司法/行政机关的要求。</li>
          <li>为完成交易所必需（如向对方展示必要的联系信息）。</li>
        </ul>
      </div>

      <div class="section">
        <h3>五、您的权利</h3>
        <p>您有权查阅、更正、删除您的个人信息，也可随时撤回授权或注销账户。如需行使上述权利，请通过"设置 > 注销账号"或联系客服。</p>
      </div>

      <div class="section">
        <h3>六、联系方式</h3>
        <p>如您对本隐私政策有任何疑问，请通过以下方式联系我们：</p>
        <p>邮箱：privacy@campustutor.com</p>
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
