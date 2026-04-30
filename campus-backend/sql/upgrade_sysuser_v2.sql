-- 用户表扩展：添加地址/微信/地区字段
ALTER TABLE `sys_user`
  ADD COLUMN `wechat` varchar(64) DEFAULT NULL COMMENT '微信号' AFTER `gender`,
  ADD COLUMN `region` varchar(128) DEFAULT NULL COMMENT '所在地区(省,市,区)' AFTER `wechat`,
  ADD COLUMN `address` varchar(256) DEFAULT NULL COMMENT '详细地址' AFTER `region`,
  ADD COLUMN `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度' AFTER `address`,
  ADD COLUMN `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度' AFTER `longitude`;
