-- 给所有用户钱包设置大额测试余额
-- 运行此脚本可以方便进行支付测试

-- 更新所有现有钱包的余额为 100000.00 元
UPDATE sys_wallet SET balance = 100000.00;

-- 如果需要，也可以清零冻结金额
-- UPDATE sys_wallet SET frozen_amount = 0.00;

SELECT user_id, balance, frozen_amount FROM sys_wallet LIMIT 20;
