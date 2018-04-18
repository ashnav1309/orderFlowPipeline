USE inventory;
ALTER TABLE ordersnew MODIFY COLUMN customer_id BIGINT(20) NOT NULL;
ALTER TABLE ordersnew_temp MODIFY COLUMN customer_id BIGINT(20) NOT NULL;
ALTER TABLE refund_request MODIFY COLUMN customer_id BIGINT(20) NOT NULL;
ALTER TABLE refund_item MODIFY COLUMN supplier_id BIGINT(20) NOT NULL;
ALTER TABLE customer_account_info MODIFY COLUMN customer_id BIGINT(20) NOT NULL;
ALTER TABLE customer_feedback MODIFY COLUMN customer_id BIGINT(20) NOT NULL;