

------------------------ 数据库表更改记录 ------------------------

--2018-12-08
--advremaincachetmp: 增加字段 retopencout
--advtaskinfo: dayout -> dayusercount

--advremaintask: 主键由 rid 改成 autoid
--2018-12-11
--增加表:tb_devactstatus,并增加相应的dao主要用于后台统计在线和离线用户,取代之前的tb_devactlog,tb_devactlog和相关dao不再使用

--2018-12-20
--1.apkinfo: 增加unzip_regex,用来存放不压缩文件表达式
--2.增加表tb_requesttime，存放orderid、orderid生产时间(2019-1-10 已经删除)
--3.修改dbo.tb_advnewreglog和dbo.tb_advnewreglog_01-dbo.tb_advnewreglog_12 
--  把原有[phoneinfo]字段改为[appinfo]，同时更改dao类和service类文件

-- 2018-12-29
-- 在[tb_timeline]表中增加一个mark字段说明此曲线意义，方便运营人员在后台选择以前做过的曲线
	
-- 2019-1-9
-- 修改[tb_devpostmsg]的dev_tag字段为log_type(varchar(120)) 	
-- 修改[tb_devpostmsg]的message字段类型为text

-- 2019-1-18
-- 1.为微信公众号项目创建了 tb_weixinlink :存放微信link 
--   tb_weixinaccount：存放微信账号
--   tb_weixinlog ：针对该任务写的日志
--   tb_weixinlinkFail: 客户端阅读微信link失败时，失败链接存入此表

-- 2019-1-23
-- 1.迅雷充值任务需要QQ号，新建了tb_qqaccount_XunLeiChongZhi表
-- 2.由于之前做大圣轮回(alias:dslh)时也是用的相同QQ号，在tb_qqacount表新增comment字段来标记一下
-- 3.导入了1万多个QQ号进入tb_qqaccount表和tb_qqaccount_XunLeiChongZhi

-- 2019-1-25
-- 1.tb_qqacount表的alies(varchar(50)) 改为 lastLoginTime(dateTime)
-- 2.tb_vpninfo表增加comment字段，提供更多vpn信息
 