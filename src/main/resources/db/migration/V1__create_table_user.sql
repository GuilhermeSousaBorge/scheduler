create table tb_user (
    id Bigserial primary key,
    name varchar(150) not null,
    phone varchar(14) not null unique,
    password varchar(200) not null,
    created_at timestampz not null default current_timestamp,
    updated_at timestampz not null default current_timestamp
);

create index idx_tb_user_name on tb_user(name);