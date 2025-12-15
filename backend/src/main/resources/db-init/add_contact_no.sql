-- 添加联络编号字段到联络记录表
ALTER TABLE contact_record ADD COLUMN contact_no VARCHAR(20) UNIQUE NULL;

-- 为现有记录生成联络编号
SET @rownum := 0;
UPDATE contact_record 
SET contact_no = CONCAT('CR', LPAD((@rownum := @rownum + 1), 8, '0'))
WHERE contact_no IS NULL
ORDER BY id;

-- 修改为非空约束
ALTER TABLE contact_record MODIFY COLUMN contact_no VARCHAR(20) NOT NULL UNIQUE;

-- 添加索引以提高查询性能
CREATE INDEX idx_contact_no ON contact_record(contact_no);
