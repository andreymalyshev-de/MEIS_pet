create table if not exists metric_snapshots (
                    	id SERIAL primary key,
                    	time_stamp TIMESTAMPTZ,
                    	symbol varchar(30),
                    	avgPrice double precision,
                    	volume double precision,
                    	volatility double precision,
                    	tradeCount bigint
                    );

create table if not exists anomaly_events (
                    	id SERIAL primary key,
                    	time_stamp TIMESTAMPTZ,
                    	eventType varchar(20),
                    	symbol varchar(30),
                    	change double precision
                    )