alter table tb_user add column role varchar(10) not null default 'CLIENT',
    add constraint role_check check (role in ('CLIENT', 'ATTENDANT', 'ADMIN'));
