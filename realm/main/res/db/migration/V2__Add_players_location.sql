alter table players add column current_map integer not null default ${start_map_id};
alter table players add column current_cell integer not null default ${start_cell_id};