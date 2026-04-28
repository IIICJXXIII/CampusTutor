<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧配置菜单 -->
      <el-col :span="5">
        <div class="settings-menu card-shadow">
          <el-menu 
            v-model:default-active="activeMenu" 
            @select="handleMenuSelect"
          >
            <el-menu-item index="basic">
              <el-icon><Setting /></el-icon>
              <span>基础设置</span>
            </el-menu-item>
            <el-menu-item index="fee">
              <el-icon><Money /></el-icon>
              <span>费用设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="sms">
              <el-icon><ChatDotSquare /></el-icon>
              <span>短信模板</span>
            </el-menu-item>
            <el-menu-item index="audit">
              <el-icon><Checked /></el-icon>
              <span>审核设置</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>
      
      <!-- 右侧配置内容 -->
      <el-col :span="19">
        <!-- 基础设置 -->
        <div v-show="activeMenu === 'basic'" class="settings-content card-shadow">
          <h3>基础设置</h3>
          <el-form :model="basicForm" label-width="150px" style="max-width: 600px;">
            <el-form-item label="平台名称">
              <el-input v-model="basicForm.platformName" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="basicForm.contactPhone" />
            </el-form-item>
            <el-form-item label="客服微信">
              <el-input v-model="basicForm.customerWechat" />
            </el-form-item>
            <el-form-item label="平台公告">
              <el-input v-model="basicForm.announcement" type="textarea" rows="4" />
            </el-form-item>
            <el-form-item label="新用户欢迎语">
              <el-input v-model="basicForm.welcomeMessage" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item label="维护模式">
              <el-switch v-model="basicForm.maintenanceMode" />
              <span class="tips">开启后用户将无法访问</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 费用设置 -->
        <div v-show="activeMenu === 'fee'" class="settings-content card-shadow">
          <h3>费用设置</h3>
          <el-form :model="feeForm" label-width="150px" style="max-width: 600px;">
            <el-form-item label="平台服务费率">
              <el-input-number v-model="feeForm.platformFeeRate" :min="0" :max="50" :precision="1" />
              <span class="unit">%</span>
              <span class="tips">从教师收入中扣除</span>
            </el-form-item>
            <el-form-item label="最低提现金额">
              <el-input-number v-model="feeForm.minWithdrawAmount" :min="1" :step="10" />
              <span class="unit">元</span>
            </el-form-item>
            <el-form-item label="提现手续费">
              <el-input-number v-model="feeForm.withdrawFee" :min="0" :max="10" :precision="1" />
              <span class="unit">%</span>
            </el-form-item>
            <el-form-item label="免提现手续费门槛">
              <el-input-number v-model="feeForm.freeWithdrawThreshold" :min="0" :step="100" />
              <span class="unit">元</span>
              <span class="tips">超过此金额免手续费</span>
            </el-form-item>
            <el-form-item label="订单取消费率">
              <el-input-number v-model="feeForm.cancelFeeRate" :min="0" :max="100" />
              <span class="unit">%</span>
              <span class="tips">已上课部分不退款的比例</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveFeeSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 通知设置 -->
        <div v-show="activeMenu === 'notification'" class="settings-content card-shadow">
          <h3>通知设置</h3>
          <el-form :model="notificationForm" label-width="200px" style="max-width: 600px;">
            <el-divider content-position="left">订单通知</el-divider>
            <el-form-item label="新订单通知">
              <el-switch v-model="notificationForm.newOrder" />
            </el-form-item>
            <el-form-item label="订单支付成功通知">
              <el-switch v-model="notificationForm.orderPaid" />
            </el-form-item>
            <el-form-item label="订单取消通知">
              <el-switch v-model="notificationForm.orderCancelled" />
            </el-form-item>
            
            <el-divider content-position="left">课时通知</el-divider>
            <el-form-item label="课时开始提醒">
              <el-switch v-model="notificationForm.lessonReminder" />
            </el-form-item>
            <el-form-item label="提前提醒时间">
              <el-select v-model="notificationForm.reminderTime" style="width: 200px;">
                <el-option label="30分钟" :value="30" />
                <el-option label="1小时" :value="60" />
                <el-option label="2小时" :value="120" />
                <el-option label="1天" :value="1440" />
              </el-select>
            </el-form-item>
            
            <el-divider content-position="left">审核通知</el-divider>
            <el-form-item label="教师认证结果通知">
              <el-switch v-model="notificationForm.certResult" />
            </el-form-item>
            <el-form-item label="提现结果通知">
              <el-switch v-model="notificationForm.withdrawResult" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 短信模板 -->
        <div v-show="activeMenu === 'sms'" class="settings-content card-shadow">
          <h3>短信模板</h3>
          <el-table :data="smsTemplates" border>
            <el-table-column prop="name" label="模板名称" width="150" />
            <el-table-column prop="code" label="模板编码" width="150" />
            <el-table-column prop="content" label="模板内容" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'info'" size="small">
                  {{ row.status ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" @click="editTemplate(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 审核设置 -->
        <div v-show="activeMenu === 'audit'" class="settings-content card-shadow">
          <h3>审核设置</h3>
          <el-form :model="auditForm" label-width="200px" style="max-width: 600px;">
            <el-divider content-position="left">教师认证</el-divider>
            <el-form-item label="自动审核">
              <el-switch v-model="auditForm.autoAudit" />
              <span class="tips">开启后符合条件的认证将自动通过</span>
            </el-form-item>
            <el-form-item label="必须上传身份证">
              <el-switch v-model="auditForm.requireIdCard" />
            </el-form-item>
            <el-form-item label="必须上传学生证">
              <el-switch v-model="auditForm.requireStudentCard" />
            </el-form-item>
            <el-form-item label="审核有效期">
              <el-input-number v-model="auditForm.auditValidDays" :min="30" :step="30" />
              <span class="unit">天</span>
            </el-form-item>
            
            <el-divider content-position="left">需求审核</el-divider>
            <el-form-item label="需求自动发布">
              <el-switch v-model="auditForm.autoPublishDemand" />
              <span class="tips">关闭后需求需审核才能发布</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveAuditSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Money, Bell, ChatDotSquare, Checked } from '@element-plus/icons-vue'

const activeMenu = ref('basic')

// 基础设置
const basicForm = reactive({
  platformName: '素质教育平台',
  contactPhone: '400-123-4567',
  customerWechat: 'campus_tutor',
  announcement: '欢迎使用素质教育平台，我们致力于为孩子提供优质的艺术、体育、科创STEAM教育服务。',
  welcomeMessage: '您好，欢迎加入素质教育平台！',
  maintenanceMode: false
})

// 费用设置
const feeForm = reactive({
  platformFeeRate: 10,
  minWithdrawAmount: 50,
  withdrawFee: 0.5,
  freeWithdrawThreshold: 500,
  cancelFeeRate: 20
})

// 通知设置
const notificationForm = reactive({
  newOrder: true,
  orderPaid: true,
  orderCancelled: true,
  lessonReminder: true,
  reminderTime: 60,
  certResult: true,
  withdrawResult: true
})

// 审核设置
const auditForm = reactive({
  autoAudit: false,
  requireIdCard: true,
  requireStudentCard: true,
  auditValidDays: 365,
  autoPublishDemand: true
})

// 短信模板
const smsTemplates = ref([
  { id: 1, name: '验证码', code: 'SMS_VERIFY', content: '您的验证码是${code}，5分钟内有效。', status: true },
  { id: 2, name: '订单创建', code: 'SMS_ORDER_CREATE', content: '您的订单${orderNo}已创建，请尽快支付。', status: true },
  { id: 3, name: '课时提醒', code: 'SMS_LESSON_REMIND', content: '您的课程将于${time}开始，请准时参加。', status: true },
  { id: 4, name: '认证通过', code: 'SMS_CERT_PASS', content: '恭喜您，您的教师认证已通过审核。', status: true },
  { id: 5, name: '认证拒绝', code: 'SMS_CERT_REJECT', content: '抱歉，您的教师认证未通过审核，原因：${reason}。', status: true }
])

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const saveBasicSettings = () => {
  ElMessage.success('基础设置保存成功')
}

const saveFeeSettings = () => {
  ElMessage.success('费用设置保存成功')
}

const saveNotificationSettings = () => {
  ElMessage.success('通知设置保存成功')
}

const saveAuditSettings = () => {
  ElMessage.success('审核设置保存成功')
}

const editTemplate = (row) => {
  ElMessage.info('模板编辑功能开发中')
}
</script>

<style lang="scss" scoped>
.settings-menu {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  
  :deep(.el-menu) {
    border-right: none;
    
    .el-menu-item {
      height: 50px;
      line-height: 50px;
      
      &.is-active {
        background: linear-gradient(90deg, #ecf5ff, #fff);
        border-right: 3px solid var(--el-color-primary);
      }
    }
  }
}

.settings-content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-height: 500px;
  
  h3 {
    margin: 0 0 24px 0;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    color: #303133;
    font-size: 18px;
  }
  
  .unit {
    margin-left: 8px;
    color: #909399;
  }
  
  .tips {
    margin-left: 12px;
    color: #909399;
    font-size: 12px;
  }
}

:deep(.el-divider__text) {
  color: #606266;
  font-weight: 600;
}
</style>
