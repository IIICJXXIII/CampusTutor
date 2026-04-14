<template>
  <div class="sign-page">
    <el-page-header @back="$router.back()">
      <template #content>电子合同签署</template>
    </el-page-header>

    <div class="sign-container" v-loading="loading">
      <!-- 合同内容 -->
      <div class="contract-card">
        <h2>家教服务协议</h2>
        <div class="contract-body">
          <section>
            <h4>一、服务内容</h4>
            <p>甲方（家长）聘请乙方（教员 <strong>{{ order.tutorName || '—' }}</strong>）为其子女提供 <strong>{{ order.subject || '—' }}</strong> 科目的家教辅导服务。</p>
          </section>
          <section>
            <h4>二、服务安排</h4>
            <p>总课时：<strong>{{ order.totalHours || '—' }}</strong> 课时，每课时 2 小时。</p>
            <p>总费用：<strong>¥{{ order.totalAmount || '—' }}</strong>，按课时结算。</p>
          </section>
          <section>
            <h4>三、双方权责</h4>
            <ul>
              <li>甲方应按时安排学生上课，如需调课应提前 24 小时通知。</li>
              <li>乙方应按时到达授课地点，保质保量完成教学任务。</li>
              <li>课时由教员打卡签到，家长确认后计为有效课时。</li>
            </ul>
          </section>
          <section>
            <h4>四、费用与结算</h4>
            <ul>
              <li>课时费通过平台钱包支付，订单创建时冻结全部金额。</li>
              <li>每节课家长确认后，对应课时费释放至教员账户。</li>
              <li>平台收取 {{ serviceFeePercent }}% 服务费。</li>
            </ul>
          </section>
          <section>
            <h4>五、违约条款</h4>
            <ul>
              <li>教员无故缺课，扣除该课时费双倍作为违约金。</li>
              <li>家长未提前 24 小时取消课程，视为已消耗课时。</li>
              <li>如发生纠纷，双方应协商解决，协商不成可通过平台介入处理。</li>
            </ul>
          </section>
        </div>
      </div>

      <!-- 确认签署 -->
      <div class="sign-action">
        <el-checkbox v-model="agreed" :label="true">
          我已阅读并同意以上协议条款
        </el-checkbox>
        <el-button
          type="primary"
          size="large"
          :disabled="!agreed"
          :loading="signing"
          @click="handleSign"
        >
          确认签署并进入支付
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@shared/api/order'

const route = useRoute()
const router = useRouter()

const order = ref({})
const loading = ref(true)
const agreed = ref(false)
const signing = ref(false)
const serviceFeePercent = 10

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.data?.code === 200) {
      order.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
})

const handleSign = async () => {
  signing.value = true
  try {
    // 签署后跳转到支付页
    router.push(`/parent/orders/${route.params.id}/pay`)
  } finally {
    signing.value = false
  }
}
</script>

<style lang="scss" scoped>
.sign-page {
  max-width: 700px;
  margin: 0 auto;

  .sign-container {
    margin-top: 24px;
  }

  .contract-card {
    background: #fff;
    border-radius: 12px;
    padding: 32px;
    margin-bottom: 20px;

    h2 {
      text-align: center;
      margin-bottom: 24px;
      font-size: 20px;
    }

    .contract-body {
      section {
        margin-bottom: 20px;

        h4 {
          font-size: 15px;
          margin-bottom: 8px;
        }

        p, li {
          font-size: 14px;
          line-height: 1.8;
          color: #303133;
        }

        ul {
          padding-left: 20px;
        }
      }
    }
  }

  .sign-action {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    align-items: center;

    .el-button {
      width: 100%;
      max-width: 400px;
      height: 48px;
      font-size: 16px;
    }
  }
}
</style>
