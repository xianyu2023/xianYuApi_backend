use api;
-- 开放接口信息
create table if not exists api.`open_api`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `name` varchar(256) not null comment '接口名称',
    `description` varchar(256) null comment '接口描述',
    `url` varchar(512) not null comment '接口地址',
    `method` varchar(256) null comment '请求类型',
    `requestParams` text null comment '请求参数',
    `requestHeader` text null comment '请求头',
    `responseHeader` text null comment '响应头',
    `status` int default 0 not null comment '接口状态（0-关闭，1-开启）',
    `userId` bigint not null comment '创建人id',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
    ) comment '开放接口信息';

insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('GLF', 'h1hX3', 'www.jaime-schinner.co', 'tT', 'pC5OK', 'RGo', 0, 61081);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('a2a', '0Xk', 'www.shane-orn.io', 'hKp2C', 'zeHHZ', 'Vn6F8', 0, 864);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('FKhHz', 'p5dd6', 'www.don-keebler.org', 'esWY7', 'i2', 'ZG3T', 0, 8);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('o9RPf', 'Jbb', 'www.chung-grady.io', 'SY', 'bOGE', 'kC', 0, 491100);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('wj', 'Wf', 'www.ken-mayer.com', 'NnE', '33', 'vq', 0, 95187695);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('45J7', 'wtCO', 'www.devon-lubowitz.name', 'aCV', 'ImV', 'RdmZ', 0, 322857);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('5Odw', 'Hlf', 'www.ollie-ruecker.com', 'Y9uI8', 'Tmpo', 'Fr', 0, 55916186);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('7P6', '1hur', 'www.carri-lakin.org', '0besi', 'lQg', '8Kx3', 0, 7803240267);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('wE', 'UwoX4', 'www.daniel-walsh.biz', 'YUR', 'q73N', 'OT7', 0, 81);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('WEh1', '1H', 'www.otto-homenick.io', '848Ie', 'xW', 'Rp', 0, 7119);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('G8O', 'XO', 'www.otha-schulist.name', 'Ytg4', 'Oia', '7SZM', 0, 812);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('Rn', 'gPIhz', 'www.ruthie-farrell.net', 'UMHhI', 'tm8P2', 'x3', 0, 1157677884);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('JuCq', 'bI', 'www.clyde-cronin.co', 'k87j', 'pb0R', 'AAf', 0, 82);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('9ERZ', 'eSMB', 'www.dwain-prohaska.info', 'Gcw', 'nu', '143b', 0, 2);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('7j', 'UO', 'www.clemente-corwin.info', 'JKYmn', 'UK', 'Jlfk', 0, 938);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('yP', 'fJ1', 'www.toccara-bergnaum.name', 'cqax', 'WhJ7U', 'XkZN', 0, 329714);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('8gI', '20AI', 'www.lucius-kling.info', 'Ac', 'ZSI', 'fNe', 0, 149545612);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('oNB', 'ot0r', 'www.calvin-lubowitz.org', 'nXZG', 'Sd', 'jqyu', 0, 84116556);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('CWux', 'P5VB', 'www.arnold-harvey.co', 'DQMly', 'bMbgg', 'Pp', 0, 91703);
insert into api.`open_api` (`name`, `description`, `url`, `method`, `requestHeader`, `responseHeader`, `status`, `userId`) values ('exx', 'YqCvu', 'www.mose-mosciski.biz', 'n1V', 'PQwxp', '4t', 0, 8);

-- 用户调用接口关系表：
create table user_open_api
(
    id             bigint auto_increment comment '主键'
        primary key,
    userId         bigint                             not null comment '调用者id',
    openApiId         bigint                             not null comment '接口id',
    totalNum         int      default 0                 not null comment '总调用次数',
    leftNum         int      default 0                 not null comment '剩余调用次数',
    status         int      default 0                 not null comment '调用权限（0-正常，1-禁止）',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted      tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
)
    comment '用户调用接口关系表'
