/*drop table if exists reviewItem cascade;

create table reviewItem(
    id varchar(36) primary key ,
    user_name varchar(255) not null ,
    problem_id bigint not null ,
    result_text varchar(255),
    timestamp bigint,
    last_attempt_timestamp bigint,
    review_level int default 1,
    next_review_date date
)*/