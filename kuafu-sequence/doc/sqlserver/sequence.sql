
create table t_sequence (
	segment            int                   not null  default 0,   -- 分片，从0开始计数
	name               varchar(150)          not null,              -- 序列名
	current_value      bigint                not null,             -- 当前值（对应 MySQL bigint unsigned）
	create_time        datetime2             not null,             -- 创建时间
	update_time        datetime2             not null,             -- 更新时间

	primary key (segment, name)
);

exec sp_addextendedproperty 'ms_table_comment', '序列号表',       'SCHEMA', 'dbo', 'TABLE', 't_sequence';
exec sp_addextendedproperty 'ms_description', '分片，从0开始计数', 'SCHEMA', 'dbo', 'TABLE', 't_sequence', 'COLUMN', 'segment';
exec sp_addextendedproperty 'ms_description', '序列名',            'SCHEMA', 'dbo', 'TABLE', 't_sequence', 'COLUMN', 'name';
exec sp_addextendedproperty 'ms_description', '当前值',            'SCHEMA', 'dbo', 'TABLE', 't_sequence', 'COLUMN', 'current_value';
exec sp_addextendedproperty 'ms_description', '创建时间',          'SCHEMA', 'dbo', 'TABLE', 't_sequence', 'COLUMN', 'create_time';
exec sp_addextendedproperty 'ms_description', '更新时间',          'SCHEMA', 'dbo', 'TABLE', 't_sequence', 'COLUMN', 'update_time';
