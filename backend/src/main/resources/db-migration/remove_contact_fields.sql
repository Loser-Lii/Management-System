-- 从联络记录表中删除下次跟进时间和意向等级字段

-- 删除 next_contact_time 字段
ALTER TABLE `contact_record` DROP COLUMN IF EXISTS `next_contact_time`;

-- 删除 intention_level 字段
ALTER TABLE `contact_record` DROP COLUMN IF EXISTS `intention_level`;
