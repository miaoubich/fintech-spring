create table outbox_dead_letter (
        id bigint not null,
        aggregate_id varchar(64) not null,
        created_at timestamp(6) with time zone not null,
        event_type varchar(64) not null,
        last_error TEXT,
        payload TEXT not null,
        published_to_kafka boolean not null,
        retry_count integer not null,
        primary key (id)
    )
Hibernate: 
    create table outbox_events (
        id bigint not null,
        aggregate_id varchar(100) not null,
        aggregate_type varchar(100) not null,
        created_at timestamp(6) with time zone not null,
        event_type varchar(100) not null,
        last_error varchar(255),
        payload jsonb not null,
        published_at timestamp(6) with time zone,
        retry_count integer not null,
        status varchar(20) not null check (status in ('PENDING','PUBLISHED','DEAD_LETTER')),
        version bigint not null,
        primary key (id)
    )
Hibernate: 
    create table trades (
        id bigint not null,
        asset varchar(255) not null,
        created_at timestamp(6) with time zone not null,
        price numeric(38,18) not null,
        quantity numeric(38,18) not null,
        side varchar(255) not null,
        status varchar(255) not null,
        symbol varchar(255) not null,
        trade_id varchar(255) not null,
        updated_at timestamp(6) with time zone not null,
        user_id varchar(255) not null,
        primary key (id)
    )
Hibernate: 
    create index idx_outbox_dlq_aggregate_id 
       on outbox_dead_letter (aggregate_id)
Hibernate: 
    create index idx_outbox_dlq_created_at 
       on outbox_dead_letter (created_at desc)
Hibernate: 
    create index idx_outbox_events_processed 
       on outbox_events (processed, created_at)