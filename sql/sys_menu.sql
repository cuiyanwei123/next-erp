CREATE TABLE `netx_erp`.`sys_menu` (
  `id` INT NOT NULL,
  `code` VARCHAR(45) NOT NULL COMMENT '代码',
  `name` VARCHAR(45) NOT NULL COMMENT '名称',
  `enable` CHAR(1) NOT NULL DEFAULT 'Y',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_date` timestamp NULL DEFAULT NULL,
  `create_by` int(11) DEFAULT NULL COMMENT '操作人id',
  `last_update_date` timestamp NULL DEFAULT NULL,
  `last_update_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`));
