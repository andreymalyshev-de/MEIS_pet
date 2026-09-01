create table if not exists metric_snapshots (
                    	id SERIAL primary key,
                    	time_stamp TIMESTAMPTZ,
                    	symbol varchar(30),
                    	avgPrice double precision,
                    	volume double precision,
                    	volatility double precision,
                    	tradeCount bigint
                    );

create index if not exists idx_metric_snapshots_time
    on metric_snapshots(time_stamp);

create table if not exists anomaly_events (
                    	id SERIAL primary key,
                    	time_stamp TIMESTAMPTZ,
                    	eventType varchar(20),
                    	symbol varchar(30),
                    	change double precision
                    );

create index if not exists idx_anomaly_events_time
    on anomaly_events(time_stamp);


