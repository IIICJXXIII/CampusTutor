-- Migration: Add application_id to course_order to link tutor applications with orders
-- This enables teachers to see their demand applications in "My Orders" with cancel capability

ALTER TABLE course_order
ADD COLUMN application_id BIGINT DEFAULT NULL COMMENT '关联的申请ID',
ADD INDEX idx_application_id (application_id);
