
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
