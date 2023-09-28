-- 创建库
create database if not exists api;

-- 切换库
use api;

-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    accessKey    varchar(512) default 'xianyu'          not null comment 'accessKey',
    secretKey    varchar(512) default '123456'          not null comment 'secretKey',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    status       tinyint      default 0                 not null comment '0-正常
1-封号
2-永久封号',
    constraint uni_userAccount
        unique (userAccount)
)
    comment '用户' charset = utf8;
-- 开放接口信息
-- auto-generated definition
create table open_api
(
    id             bigint auto_increment comment '主键'
        primary key,
    name           varchar(256)        not null comment '接口名称',
    description    varchar(256)        null comment '接口描述',
    url            varchar(512)        not null comment '接口地址',
    method         varchar(256)        null comment '请求类型',
    requestParams  text                null comment '请求参数',
    requestHeader  text                null comment '请求头',
    responseHeader text                null comment '响应头',
    status         int      default 0                 not null comment '接口状态（0-关闭，1-开启）',
    userId         bigint                             not null comment '创建人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted      tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)',
    path           varchar(256)                       null comment 'API的请求路径'
)
    comment '开放接口信息' charset = utf8;

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
