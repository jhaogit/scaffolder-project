-- 操作日志表
CREATE TABLE `op_log` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `request_ip` varchar(20) DEFAULT NULL COMMENT '请求源ip',
    `request_uri` varchar(255) DEFAULT NULL COMMENT '请求路径',
    `request_method` varchar(255) DEFAULT NULL COMMENT '请求方法',
    `request_time` datetime DEFAULT NULL COMMENT '请求时间',
    `menu` varchar(64) DEFAULT NULL COMMENT '菜单',
    `function` varchar(64) DEFAULT NULL COMMENT '操作功能',
    `request_param` varchar(1000) DEFAULT NULL COMMENT '入参',
    `response_param` varchar(1000) DEFAULT NULL COMMENT '出参(主要用于记录修改、新增类操作，不应该有太大参数)',
    `status` int(2) DEFAULT NULL COMMENT '0-正常 1-异常',
    `error_msg` varchar(2000) DEFAULT NULL COMMENT '异常信息',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='操作日志';

-- 操作详细记录，表历史数据变更记录
CREATE TABLE `op_log_info` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `op_log_id` int(11) DEFAULT NULL COMMENT 'op_log中的主键',
    `table_name` varchar(64) DEFAULT NULL COMMENT '表名',
    `table_key_info` varchar(255) DEFAULT NULL COMMENT '表主键信息 ["oneId:1","twoId:2"]',
    `update_info` varchar(2000) DEFAULT NULL COMMENT '变更信息，list:["[名称]：[呵呵7]->[呵呵1]","[描述]：[呵呵7]->[呵呵1]"]',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='操作日志详细信息';

-- 测试单主键表
CREATE TABLE `tb_user_info` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(64) DEFAULT NULL,
    `sex` int(2) DEFAULT NULL COMMENT '1-男 2-女',
    `age` int(11) DEFAULT NULL COMMENT '年龄',
    `addr` varchar(255) DEFAULT NULL COMMENT '地址',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8;

-- 测试多主键表
CREATE TABLE `tb_test_two` (
    `one_id` int(11) NOT NULL COMMENT '主键1',
    `two_id` int(11) NOT NULL,
    `name` varchar(32) DEFAULT NULL COMMENT '名称',
    `desc` varchar(32) DEFAULT NULL COMMENT '描述',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`one_id`,`two_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='测试联合主键表';