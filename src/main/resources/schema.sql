drop table if exists submission cascade;

create table submission(
    id varchar(36) primary key ,
    user_id varchar(255),
    problem_id bigint,
    result_text varchar(255),
    timestamp timestamp
)