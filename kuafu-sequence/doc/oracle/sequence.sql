-- ============================================================================
-- Oracle 建表脚本
-- 对应运行期 SQL: com.yuweix.kuafu.sequence.dao.AbstractSequenceDao
--
-- 【兼容性说明】
-- 1. SEQUENCE 在 Oracle 中是非保留关键字，可直接作为表名，无需加引号；
--    Oracle 会把未加引号的标识符统一折叠为大写（SEQUENCE），与运行期拼接的
--    "select ... from sequence" 折叠结果一致，故默认 tableName 可直接使用。
-- 2. 本脚本仅建表，运行期插入语句尾部的分号（getSeqInsertSql 中的
--    values(?,?,?,?,?);）会导致 Oracle 报 ORA-00911，需另行去掉该分号，
--    详见 AbstractSequenceDao 的适配说明。
-- 3. MySQL 的 bigint unsigned 对应 Oracle 的 NUMBER(19)；datetime 对应 TIMESTAMP
--    （运行期通过 setTimestamp 绑定）。
-- ============================================================================

begin
	execute immediate 'drop table sequence purge';
exception
	when others then
		null;  -- 表不存在时忽略
end;
/

create table sequence (
	segment            number(10)        default 0  not null,   -- 分片，从0开始计数
	name               varchar2(150)                not null,   -- 序列名
	current_value      number(19)                   not null,   -- 当前值（对应 MySQL bigint unsigned）
	create_time        timestamp                    not null,   -- 创建时间
	update_time        timestamp                    not null,   -- 更新时间

	constraint pk_sequence primary key (segment, name)
);

comment on table  sequence               is '序列号表';
comment on column sequence.segment       is '分片，从0开始计数';
comment on column sequence.name          is '序列名';
comment on column sequence.current_value is '当前值';
comment on column sequence.create_time   is '创建时间';
comment on column sequence.update_time   is '更新时间';
