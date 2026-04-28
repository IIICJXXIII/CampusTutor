ALTER TABLE `course_order`
  ADD COLUMN `longitude` decimal(10, 7) DEFAULT NULL COMMENT '经度',
  ADD COLUMN `latitude` decimal(10, 7) DEFAULT NULL COMMENT '纬度',
  ADD COLUMN `address` varchar(255) DEFAULT NULL COMMENT '地址';

UPDATE `course_order` o
  INNER JOIN `demand_post` d ON o.demand_id = d.id
  SET o.longitude = d.longitude, o.latitude = d.latitude, o.address = d.address
  WHERE o.demand_id IS NOT NULL AND d.longitude IS NOT NULL;
