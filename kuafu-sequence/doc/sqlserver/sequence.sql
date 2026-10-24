-- ============================================================================
-- SQL Server 建表脚本
-- 对应运行期 SQL: com.yuweix.kuafu.sequence.dao.AbstractSequenceDao
--
-- 【关键字冲突说明】
-- SEQUENCE 是 SQL Server 的保留关键字，而 AbstractSequenceDao 运行期是以
-- 字符串拼接的方式直接使用表名（insert into <tableName> ...），默认表名
-- "sequence" 未加界定符会报语法错误。
-- 解决办法：配置 tableName 时使用方括号界定符，令其拼成 [sequence]，例如：
--     <property name="tableName" value="[sequence]"/>
-- 这样运行期会拼成 insert into [sequence](...) / update [sequence] set ...
-- / select ... from [sequence]，从而避开关键字冲突。
-- ============================================================================

if object_id('[dbo].[sequence]', 'U') is not null
	drop table [dbo].[sequence];
go

create table [dbo].[sequence] (
	segment            int                   not null  default 0,   -- 分片，从0开始计数
	name               varchar(150)          not null,              -- 序列名
	current_value      bigint                not null,             -- 当前值（对应 MySQL bigint unsigned）
	create_time        datetime2             not null,             -- 创建时间
	update_time        datetime2             not null,             -- 更新时间

	primary key (segment, name)
);
go
