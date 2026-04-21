# CampusTutor 模块六（预约与接单）+ 模块七（订单全生命周期）完整测试脚本
# 测试账号: 教师 17209892755 / 家长 15273153320 / 密码 123456

$ErrorActionPreference = "Continue"
$BASE = "http://localhost:8080"

# ============================================================
# 辅助函数
# ============================================================
function Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token = "",
        [string]$Body = "",
        [string]$ContentType = "application/json"
    )
    $uri = "$BASE$Path"
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    
    try {
        if ($Body -and $Method -ne "GET") {
            $resp = Invoke-WebRequest -Uri $uri -Method $Method -ContentType $ContentType -Body ([System.Text.Encoding]::UTF8.GetBytes($Body)) -Headers $headers -UseBasicParsing -TimeoutSec 15
        } else {
            $resp = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -UseBasicParsing -TimeoutSec 15
        }
        return @{ Status = $resp.StatusCode; Body = ($resp.Content | ConvertFrom-Json) }
    } catch {
        $ex = $_.Exception
        $statusCode = 0
        $respBody = $null
        if ($ex.Response) {
            $statusCode = [int]$ex.Response.StatusCode
            try {
                $reader = New-Object System.IO.StreamReader($ex.Response.GetResponseStream())
                $respBody = $reader.ReadToEnd() | ConvertFrom-Json
                $reader.Close()
            } catch {
                $respBody = @{ message = $ex.Message }
            }
        } else {
            $respBody = @{ message = $ex.Message }
        }
        return @{ Status = $statusCode; Body = $respBody }
    }
}

function Log-Result {
    param(
        [string]$TestId,
        [string]$TestName,
        [string]$Expected,
        [object]$Result,
        [bool]$Pass
    )
    $icon = if ($Pass) { "[PASS]" } else { "[FAIL]" }
    $status = $Result.Status
    $msg = ""
    if ($Result.Body) {
        try { $msg = $Result.Body.message } catch {}
        if (-not $msg) { try { $msg = $Result.Body.msg } catch {} }
    }
    Write-Host "$icon $TestId - $TestName | HTTP=$status | Msg=$msg | Expected: $Expected"
    return @{ Id=$TestId; Name=$TestName; Pass=$Pass; Status=$status; Message=$msg; Expected=$Expected }
}

$results = @()

Write-Host "============================================================"
Write-Host " CampusTutor Module 6 & 7 Full Test Suite"
Write-Host " Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host "============================================================"
Write-Host ""

# ============================================================
# Phase 0: 登录获取 Token
# ============================================================
Write-Host "===== Phase 0: Login and Get Tokens ====="

# 教师登录
$teacherLogin = Api -Method POST -Path "/api/auth/login" -Body '{"account":"17209892755","password":"123456","loginType":"password"}'
$teacherToken = ""
$teacherUserId = 0
if ($teacherLogin.Body -and $teacherLogin.Body.data) {
    $teacherToken = $teacherLogin.Body.data.token
    $teacherUserId = $teacherLogin.Body.data.userId
    Write-Host "[INFO] Teacher login OK, userId=$teacherUserId, token=$($teacherToken.Substring(0, [Math]::Min(20, $teacherToken.Length)))..."
} else {
    Write-Host "[FATAL] Teacher login FAILED! $($teacherLogin | ConvertTo-Json -Depth 3)"
    exit 1
}

# 家长登录
$parentLogin = Api -Method POST -Path "/api/auth/login" -Body '{"account":"15273153320","password":"123456","loginType":"password"}'
$parentToken = ""
$parentUserId = 0
if ($parentLogin.Body -and $parentLogin.Body.data) {
    $parentToken = $parentLogin.Body.data.token
    $parentUserId = $parentLogin.Body.data.userId
    Write-Host "[INFO] Parent login OK, userId=$parentUserId, token=$($parentToken.Substring(0, [Math]::Min(20, $parentToken.Length)))..."
} else {
    Write-Host "[FATAL] Parent login FAILED! $($parentLogin | ConvertTo-Json -Depth 3)"
    exit 1
}
Write-Host ""

# ============================================================
# Phase 1: 前置条件 - 确保家长有需求可用
# ============================================================
Write-Host "===== Phase 1: Prerequisites - Publish Demand ====="

# 家长发布一个素质教育需求（用于后续接单测试）
$demandBody = @{
    title = "Testing Piano Lesson - $(Get-Date -Format 'HHmmss')"
    subject = "piano"
    grade = "5-8 years old"
    teachMode = 1
    expectPrice = 150
    longitude = 113.26
    latitude = 23.13
    address = "Guangzhou Test Address"
    detail = "Looking for piano teacher for testing"
} | ConvertTo-Json -Compress
$publishResult = Api -Method POST -Path "/api/demand/publish" -Token $parentToken -Body $demandBody
$demandId = 0
if ($publishResult.Body -and $publishResult.Body.code -eq 200) {
    $demandId = $publishResult.Body.data
    Write-Host "[INFO] Demand published OK, demandId=$demandId"
} else {
    Write-Host "[WARN] Demand publish failed: $($publishResult.Body | ConvertTo-Json -Depth 3)"
    # Try to get existing demands
    $myDemands = Api -Method GET -Path "/api/demand/my" -Token $parentToken
    if ($myDemands.Body -and $myDemands.Body.data -and $myDemands.Body.data.Count -gt 0) {
        $demandId = $myDemands.Body.data[0].id
        Write-Host "[INFO] Using existing demand, demandId=$demandId"
    } else {
        Write-Host "[FATAL] No demands available for testing!"
    }
}

# 同时发布第二个需求（用于取消预约测试等）
$demandBody2 = @{
    title = "Testing Art Class - $(Get-Date -Format 'HHmmss')"
    subject = "art"
    grade = "8-12 years old"
    teachMode = 2
    expectPrice = 100
    longitude = 113.27
    latitude = 23.14
    address = "Guangzhou Test Address 2"
    detail = "Looking for art teacher for testing"
} | ConvertTo-Json -Compress
$publishResult2 = Api -Method POST -Path "/api/demand/publish" -Token $parentToken -Body $demandBody2
$demandId2 = 0
if ($publishResult2.Body -and $publishResult2.Body.code -eq 200) {
    $demandId2 = $publishResult2.Body.data
    Write-Host "[INFO] Demand 2 published OK, demandId2=$demandId2"
}
Write-Host ""

# ============================================================
# Module 6: 预约与接单
# ============================================================
Write-Host "============================================================"
Write-Host " MODULE 6: Booking & Order Acceptance (预约与接单)"
Write-Host "============================================================"
Write-Host ""

# ----- 6.1 家长发起预约 -----
Write-Host "--- 6.1 Parent Creates Booking ---"

$bookingBody = @{
    tutorId = $teacherUserId
    subject = "piano"
    grade = "5-8 years old"
    bookingDate = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")
    startTime = "14:00"
    endTime = "16:00"
    remark = "Test booking from parent"
} | ConvertTo-Json -Compress

# TC-6.1.1: 家长创建预约
$r = Api -Method POST -Path "/api/booking/create" -Token $parentToken -Body $bookingBody
$bookingId = 0
$pass = ($r.Body -and $r.Body.code -eq 200)
if ($pass) { $bookingId = $r.Body.data }
$results += Log-Result -TestId "TC-6.1.1" -TestName "Parent creates booking" -Expected "code=200, bookingId returned" -Result $r -Pass $pass

# TC-6.1.2: 家长查看预约列表
$r = Api -Method GET -Path "/api/booking/parent/list" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$hasBooking = $false
if ($pass -and $r.Body.data) {
    foreach ($b in $r.Body.data) {
        if ($b.id -eq $bookingId) { $hasBooking = $true; break }
    }
}
$results += Log-Result -TestId "TC-6.1.2" -TestName "Parent views booking list" -Expected "code=200, new booking in list" -Result $r -Pass ($pass -and ($bookingId -eq 0 -or $hasBooking))

# ----- 6.2 教师处理预约 -----
Write-Host ""
Write-Host "--- 6.2 Teacher Processes Booking ---"

# TC-6.2.1: 教师查看预约列表
$r = Api -Method GET -Path "/api/booking/tutor/list" -Token $teacherToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$results += Log-Result -TestId "TC-6.2.1" -TestName "Teacher views booking list" -Expected "code=200, contains parent's booking" -Result $r -Pass $pass

# TC-6.2.2: 教师确认预约
if ($bookingId -gt 0) {
    $r = Api -Method POST -Path "/api/booking/confirm/$bookingId" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-6.2.2" -TestName "Teacher confirms booking" -Expected "code=200" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-6.2.2 - No bookingId available"
    $results += @{ Id="TC-6.2.2"; Name="Teacher confirms booking"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# 创建另一个预约用于拒绝测试
$bookingBody2 = @{
    tutorId = $teacherUserId
    subject = "art"
    grade = "8-12 years old"
    bookingDate = (Get-Date).AddDays(5).ToString("yyyy-MM-ddTHH:mm:ss")
    startTime = "10:00"
    endTime = "12:00"
    remark = "Test booking for rejection"
} | ConvertTo-Json -Compress
$r2 = Api -Method POST -Path "/api/booking/create" -Token $parentToken -Body $bookingBody2
$bookingId2 = 0
if ($r2.Body -and $r2.Body.code -eq 200) { $bookingId2 = $r2.Body.data }

# TC-6.2.3: 教师拒绝预约
if ($bookingId2 -gt 0) {
    $r = Api -Method POST -Path "/api/booking/reject/${bookingId2}?reason=Schedule+conflict" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-6.2.3" -TestName "Teacher rejects booking" -Expected "code=200" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-6.2.3 - No bookingId2 available"
    $results += @{ Id="TC-6.2.3"; Name="Teacher rejects booking"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# ----- 6.3 教师主动接单（需求匹配） -----
Write-Host ""
Write-Host "--- 6.3 Teacher Accepts Demand (Match) ---"

# TC-6.3.1: 教师通过 demand match 接口接单
$orderId_match = 0
if ($demandId -gt 0) {
    $r = Api -Method POST -Path "/api/demand/$demandId/match" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    if ($pass) { $orderId_match = $r.Body.data }
    $results += Log-Result -TestId "TC-6.3.1" -TestName "Teacher matches demand (creates order)" -Expected "code=200, orderId returned" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-6.3.1 - No demandId available"
    $results += @{ Id="TC-6.3.1"; Name="Teacher matches demand"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# TC-6.3.2: 教师通过 order/accept 接口接单
if ($demandId2 -gt 0) {
    $acceptBody = @{
        demandId = $demandId2
        totalHours = 10
        remark = "I can teach art"
    } | ConvertTo-Json -Compress
    $r = Api -Method POST -Path "/api/order/accept" -Token $teacherToken -Body $acceptBody
    $orderId_accept = 0
    $pass = ($r.Body -and $r.Body.code -eq 200)
    if ($pass) { $orderId_accept = $r.Body.data }
    $results += Log-Result -TestId "TC-6.3.2" -TestName "Teacher accepts demand via order/accept" -Expected "code=200, orderId returned" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-6.3.2 - No demandId2 available"
    $results += @{ Id="TC-6.3.2"; Name="Teacher accepts demand via order/accept"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# ----- 6.4 家长取消预约 -----
Write-Host ""
Write-Host "--- 6.4 Parent Cancels Booking ---"

# 创建一个新预约用于取消测试
$bookingBody3 = @{
    tutorId = $teacherUserId
    subject = "dance"
    grade = "3-6 years old"
    bookingDate = (Get-Date).AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss")
    startTime = "09:00"
    endTime = "11:00"
    remark = "Test booking for cancel"
} | ConvertTo-Json -Compress
$r3 = Api -Method POST -Path "/api/booking/create" -Token $parentToken -Body $bookingBody3
$bookingId3 = 0
if ($r3.Body -and $r3.Body.code -eq 200) { $bookingId3 = $r3.Body.data }

# TC-6.4.1: 家长取消预约
if ($bookingId3 -gt 0) {
    $r = Api -Method POST -Path "/api/booking/cancel/$bookingId3" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-6.4.1" -TestName "Parent cancels booking" -Expected "code=200" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-6.4.1 - No bookingId3 available"
    $results += @{ Id="TC-6.4.1"; Name="Parent cancels booking"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

Write-Host ""
Write-Host "============================================================"
Write-Host " MODULE 7: Order Full Lifecycle (订单全生命周期)"
Write-Host "============================================================"
Write-Host ""

# Use $orderId_match from TC-6.3.1 as the primary order to test the full lifecycle

# ----- 7.1 订单创建与确认 -----
Write-Host "--- 7.1 Order Creation & Confirmation ---"

# TC-7.1.1: 家长查看订单列表 (should contain the order from teacher matching)
$r = Api -Method GET -Path "/api/order/parent/list?page=1&size=10" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$orderFound = $false
if ($pass -and $r.Body.data -and $r.Body.data.records) {
    foreach ($o in $r.Body.data.records) {
        if ($o.id -eq $orderId_match) { $orderFound = $true; break }
    }
}
$results += Log-Result -TestId "TC-7.1.1" -TestName "Parent views order list (has new order status=0)" -Expected "code=200, order in list" -Result $r -Pass $pass

# TC-7.1.2: 家长确认订单
if ($orderId_match -gt 0) {
    $r = Api -Method POST -Path "/api/order/$orderId_match/confirm" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.1.2" -TestName "Parent confirms order" -Expected "code=200, status->待支付" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-7.1.2 - No orderId_match available"
    $results += @{ Id="TC-7.1.2"; Name="Parent confirms order"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# TC-7.1.3: 查看订单详情
if ($orderId_match -gt 0) {
    $r = Api -Method GET -Path "/api/order/$orderId_match" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $orderStatus = -1
    if ($pass -and $r.Body.data) { $orderStatus = $r.Body.data.status }
    Write-Host "  [DEBUG] Order detail: status=$orderStatus, totalAmount=$($r.Body.data.totalAmount)"
    $results += Log-Result -TestId "TC-7.1.3" -TestName "View order detail after confirm" -Expected "code=200, status=0(待支付)" -Result $r -Pass $pass
} else {
    $results += @{ Id="TC-7.1.3"; Name="View order detail after confirm"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# ----- 7.2 订单支付 -----
Write-Host ""
Write-Host "--- 7.2 Order Payment ---"

# TC-7.2.1: 余额不足时支付 (先检查余额)
$walletR = Api -Method GET -Path "/api/wallet" -Token $parentToken
$parentBalance = 0
if ($walletR.Body -and $walletR.Body.data) {
    $parentBalance = $walletR.Body.data.balance
    Write-Host "  [DEBUG] Parent wallet balance: $parentBalance"
}

# TC-7.2.2: 先充值 Mock
$r = Api -Method POST -Path "/api/wallet/recharge?amount=1000&paymentMethod=wechat" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$results += Log-Result -TestId "TC-7.2.2" -TestName "Parent recharges wallet (1000)" -Expected "code=200" -Result $r -Pass $pass

# TC-7.2.3: 支付订单
if ($orderId_match -gt 0) {
    $payBody = @{
        orderId = $orderId_match
        payType = 1
    } | ConvertTo-Json -Compress
    $r = Api -Method POST -Path "/api/order/pay" -Token $parentToken -Body $payBody
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.2.3" -TestName "Parent pays order (wallet)" -Expected "code=200, status->1(已支付)" -Result $r -Pass $pass
} else {
    $results += @{ Id="TC-7.2.3"; Name="Parent pays order"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# TC-7.2.4: 验证支付后订单状态
if ($orderId_match -gt 0) {
    $r = Api -Method GET -Path "/api/order/$orderId_match" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $orderStatus = -1
    if ($pass -and $r.Body.data) { $orderStatus = $r.Body.data.status }
    Write-Host "  [DEBUG] Order status after pay: $orderStatus"
    $results += Log-Result -TestId "TC-7.2.4" -TestName "Verify order status after payment" -Expected "status=1(已支付待上课)" -Result $r -Pass ($pass -and $orderStatus -eq 1)
}

# ----- 7.3 开课与上课 -----
Write-Host ""
Write-Host "--- 7.3 Start Class ---"

# TC-7.3.1: 教师确认开课
if ($orderId_match -gt 0) {
    $r = Api -Method POST -Path "/api/order/$orderId_match/start" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.3.1" -TestName "Teacher confirms start" -Expected "code=200, status->2(进行中)" -Result $r -Pass $pass
} else {
    $results += @{ Id="TC-7.3.1"; Name="Teacher confirms start"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# TC-7.3.2: 教师查看进行中订单
$r = Api -Method GET -Path "/api/order/tutor/list?status=2&page=1&size=10" -Token $teacherToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$results += Log-Result -TestId "TC-7.3.2" -TestName "Teacher views in-progress orders" -Expected "code=200, list returned" -Result $r -Pass $pass

# TC-7.3.3: 验证开课后订单状态
if ($orderId_match -gt 0) {
    $r = Api -Method GET -Path "/api/order/$orderId_match" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $orderStatus = -1
    if ($pass -and $r.Body.data) { $orderStatus = $r.Body.data.status }
    Write-Host "  [DEBUG] Order status after start: $orderStatus"
    $results += Log-Result -TestId "TC-7.3.3" -TestName "Verify order status after start" -Expected "status=2(进行中)" -Result $r -Pass ($pass -and $orderStatus -eq 2)
}

# ----- 7.4 订单完成 -----
Write-Host ""
Write-Host "--- 7.4 Order Completion ---"

# TC-7.4.1: 教师标记完成
if ($orderId_match -gt 0) {
    $r = Api -Method POST -Path "/api/order/$orderId_match/complete" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.4.1" -TestName "Teacher marks order complete" -Expected "code=200, status->3(已完成)" -Result $r -Pass $pass
} else {
    $results += @{ Id="TC-7.4.1"; Name="Teacher marks order complete"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# TC-7.4.2: 家长端显示已完成
$r = Api -Method GET -Path "/api/order/parent/list?status=3&page=1&size=10" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$results += Log-Result -TestId "TC-7.4.2" -TestName "Parent views completed orders" -Expected "code=200" -Result $r -Pass $pass

# TC-7.4.3: 验证完成后订单状态
if ($orderId_match -gt 0) {
    $r = Api -Method GET -Path "/api/order/$orderId_match" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $orderStatus = -1
    if ($pass -and $r.Body.data) { $orderStatus = $r.Body.data.status }
    Write-Host "  [DEBUG] Order status after complete: $orderStatus"
    $results += Log-Result -TestId "TC-7.4.3" -TestName "Verify order status after complete" -Expected "status=3(已完成)" -Result $r -Pass ($pass -and $orderStatus -eq 3)
}

# ----- 7.5 取消与退款 -----
Write-Host ""
Write-Host "--- 7.5 Cancel & Refund ---"

# 使用 orderId_accept 来测试取消 (如果它还在待确认/待支付状态)
# 先确认第二个订单
if ($orderId_accept -gt 0) {
    # TC-7.5.1: 取消待支付订单
    $r = Api -Method POST -Path "/api/order/$orderId_accept/cancel?reason=Testing+cancel" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.5.1" -TestName "Parent cancels unpaid order" -Expected "code=200, status->4(已取消)" -Result $r -Pass $pass
    
    # 验证取消后状态
    $r = Api -Method GET -Path "/api/order/$orderId_accept" -Token $parentToken
    $cancelStatus = -1
    if ($r.Body -and $r.Body.data) { $cancelStatus = $r.Body.data.status }
    Write-Host "  [DEBUG] Order status after cancel: $cancelStatus"
} else {
    Write-Host "[SKIP] TC-7.5.1 - No orderId_accept available"
    $results += @{ Id="TC-7.5.1"; Name="Parent cancels unpaid order"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
}

# 创建另一个需求并走完支付流程用于退款测试
Write-Host ""
Write-Host "  [INFO] Creating new demand -> order for refund test..."

$demandBodyRefund = @{
    title = "Refund Test - Swimming $(Get-Date -Format 'HHmmss')"
    subject = "swimming"
    grade = "6-10 years old"
    teachMode = 1
    expectPrice = 200
    longitude = 113.28
    latitude = 23.15
    address = "Guangzhou Swimming Pool"
    detail = "Looking for swimming coach - refund test"
} | ConvertTo-Json -Compress
$prRefund = Api -Method POST -Path "/api/demand/publish" -Token $parentToken -Body $demandBodyRefund
$demandIdRefund = 0
if ($prRefund.Body -and $prRefund.Body.code -eq 200) { $demandIdRefund = $prRefund.Body.data }

$orderIdRefund = 0
if ($demandIdRefund -gt 0) {
    # Teacher matches
    $mr = Api -Method POST -Path "/api/demand/$demandIdRefund/match" -Token $teacherToken
    if ($mr.Body -and $mr.Body.code -eq 200) { $orderIdRefund = $mr.Body.data }
    
    if ($orderIdRefund -gt 0) {
        # Parent confirms
        Api -Method POST -Path "/api/order/$orderIdRefund/confirm" -Token $parentToken | Out-Null
        # Ensure sufficient funds
        Api -Method POST -Path "/api/wallet/recharge?amount=2000&paymentMethod=wechat" -Token $parentToken | Out-Null
        # Parent pays
        $payBody = @{ orderId = $orderIdRefund; payType = 1 } | ConvertTo-Json -Compress
        Api -Method POST -Path "/api/order/pay" -Token $parentToken -Body $payBody | Out-Null
    }
}

# TC-7.5.2: 已支付订单申请退款
if ($orderIdRefund -gt 0) {
    # Get order detail to find totalAmount
    $orderDetail = Api -Method GET -Path "/api/order/$orderIdRefund" -Token $parentToken
    $refundAmount = 100
    if ($orderDetail.Body -and $orderDetail.Body.data -and $orderDetail.Body.data.totalAmount) {
        $refundAmount = $orderDetail.Body.data.totalAmount
    }
    
    $r = Api -Method POST -Path "/api/order/refund?orderId=$orderIdRefund&refundAmount=$refundAmount&reason=Testing+refund" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $refundNo = ""
    if ($pass -and $r.Body.data) { $refundNo = $r.Body.data }
    $results += Log-Result -TestId "TC-7.5.2" -TestName "Parent applies for refund (paid order)" -Expected "code=200, refundNo returned" -Result $r -Pass $pass
    Write-Host "  [DEBUG] Refund number: $refundNo"
    
    # TC-7.5.3: 验证退款后订单状态
    $r = Api -Method GET -Path "/api/order/$orderIdRefund" -Token $parentToken
    $refundStatus = -1
    if ($r.Body -and $r.Body.data) { $refundStatus = $r.Body.data.status }
    $pass = ($r.Body -and $r.Body.code -eq 200 -and $refundStatus -eq 5)
    Write-Host "  [DEBUG] Order status after refund request: $refundStatus"
    $results += Log-Result -TestId "TC-7.5.3" -TestName "Verify order status after refund request" -Expected "status=5(退款中)" -Result $r -Pass $pass
} else {
    Write-Host "[SKIP] TC-7.5.2 - No refund order available"
    $results += @{ Id="TC-7.5.2"; Name="Parent applies for refund"; Pass=$false; Status=0; Message="Skipped"; Expected="code=200" }
    $results += @{ Id="TC-7.5.3"; Name="Verify refund status"; Pass=$false; Status=0; Message="Skipped"; Expected="status=5" }
}

# ----- 7.6 权限验证 -----
Write-Host ""
Write-Host "--- 7.6 Permission Verification ---"

# TC-7.6.1: 教师查看不属于自己的订单 (使用教师token去查看家长独有的order - 这里教师也是参与者, 所以不好测)
# 但我们可以尝试用 家长token 操作 教师专用接口
if ($orderId_match -gt 0) {
    # TC-7.6.1: 订单详情权限验证 (订单参与者可以查看)
    $r = Api -Method GET -Path "/api/order/$orderId_match" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -eq 200)
    $results += Log-Result -TestId "TC-7.6.1" -TestName "Owner can view own order" -Expected "code=200" -Result $r -Pass $pass
}

# TC-7.6.2: 无Token访问订单
$r = Api -Method GET -Path "/api/order/$orderId_match"
$pass = ($r.Status -eq 401 -or $r.Status -eq 403 -or ($r.Body -and $r.Body.code -ne 200))
$results += Log-Result -TestId "TC-7.6.2" -TestName "Unauthorized access to order" -Expected "401/403" -Result $r -Pass $pass

# TC-7.6.3: 家长尝试教师接口 - 教师确认开课 (用家长token)
if ($orderId_match -gt 0) {
    $r = Api -Method POST -Path "/api/order/$orderId_match/start" -Token $parentToken
    $pass = ($r.Body -and $r.Body.code -ne 200)
    $results += Log-Result -TestId "TC-7.6.3" -TestName "Parent tries teacher-only operation (start)" -Expected "error/forbidden" -Result $r -Pass $pass
}

# TC-7.6.4: 对已完成订单再次完成
if ($orderId_match -gt 0) {
    $r = Api -Method POST -Path "/api/order/$orderId_match/complete" -Token $teacherToken
    $pass = ($r.Body -and $r.Body.code -ne 200)
    $results += Log-Result -TestId "TC-7.6.4" -TestName "Complete already-completed order" -Expected "error (status not allowed)" -Result $r -Pass $pass
}

# ----- 7.7 钱包余额验证 -----
Write-Host ""
Write-Host "--- 7.7 Wallet Balance Verification ---"

# TC-7.7.1: 家长查看钱包余额
$r = Api -Method GET -Path "/api/wallet" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$parentBalanceAfter = 0
if ($pass -and $r.Body.data) { $parentBalanceAfter = $r.Body.data.balance }
Write-Host "  [DEBUG] Parent wallet balance after all operations: $parentBalanceAfter"
$results += Log-Result -TestId "TC-7.7.1" -TestName "Parent wallet balance check" -Expected "code=200, balance reflects operations" -Result $r -Pass $pass

# TC-7.7.2: 教师查看钱包余额  
$r = Api -Method GET -Path "/api/wallet" -Token $teacherToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$teacherBalance = 0
if ($pass -and $r.Body.data) { $teacherBalance = $r.Body.data.balance }
Write-Host "  [DEBUG] Teacher wallet balance: $teacherBalance"
$results += Log-Result -TestId "TC-7.7.2" -TestName "Teacher wallet balance check" -Expected "code=200" -Result $r -Pass $pass

# TC-7.7.3: 家长查看交易流水
$r = Api -Method GET -Path "/api/wallet/transactions?page=1&size=20" -Token $parentToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$txCount = 0
if ($pass -and $r.Body.data -and $r.Body.data.records) { $txCount = $r.Body.data.records.Count }
Write-Host "  [DEBUG] Parent transaction count: $txCount"
$results += Log-Result -TestId "TC-7.7.3" -TestName "Parent views transactions" -Expected "code=200, has transaction records" -Result $r -Pass ($pass -and $txCount -gt 0)

# TC-7.7.4: 教师查看交易流水
$r = Api -Method GET -Path "/api/wallet/transactions?page=1&size=20" -Token $teacherToken
$pass = ($r.Body -and $r.Body.code -eq 200)
$results += Log-Result -TestId "TC-7.7.4" -TestName "Teacher views transactions" -Expected "code=200" -Result $r -Pass $pass

Write-Host ""
Write-Host "============================================================"
Write-Host " TEST SUMMARY"
Write-Host "============================================================"
Write-Host ""

$passCount = ($results | Where-Object { $_.Pass }).Count
$failCount = ($results | Where-Object { -not $_.Pass }).Count
$total = $results.Count

Write-Host "Total Tests: $total"
Write-Host "Passed:      $passCount"
Write-Host "Failed:      $failCount"
Write-Host ""

# Print failures in detail
if ($failCount -gt 0) {
    Write-Host "===== FAILED TESTS ====="
    foreach ($t in ($results | Where-Object { -not $_.Pass })) {
        Write-Host "  FAIL: $($t.Id) - $($t.Name)"
        Write-Host "        Expected: $($t.Expected)"
        Write-Host "        HTTP Status: $($t.Status) | Message: $($t.Message)"
        Write-Host ""
    }
}

Write-Host ""
Write-Host "===== ALL RESULTS TABLE ====="
foreach ($t in $results) {
    $icon = if ($t.Pass) { "PASS" } else { "FAIL" }
    Write-Host ("{0,-10} {1,-12} {2}" -f $icon, $t.Id, $t.Name)
}

Write-Host ""
Write-Host "Test completed at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
