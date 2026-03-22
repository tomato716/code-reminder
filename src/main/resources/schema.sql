drop table if exists submission cascade;

create table submission(
    id varchar(36) primary key ,
    user_name varchar(255) not null ,
    problem_id bigint not null ,
    result_text varchar(255),
    timestamp bigint,
    last_attempt_timestamp bigint
)