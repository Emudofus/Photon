insert into users(name, password, nickname, secret_question, secret_answer, community_id, subscription_end)
values('test', 'test', 'test', 'test', 'test', 0, current_timestamp + interval '1 year');