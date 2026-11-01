create table tb_user (
    id Bigserial primary key,
    name varchar(150) not null,
    phone varchar(12) not null unique,
    password varchar(200) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);

create index idx_tb_user_phone on tb_user(phone);
create index idx_tb_user_name on tb_user(name);