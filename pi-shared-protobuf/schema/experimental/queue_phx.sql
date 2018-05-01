-- noinspection SqlDialectInspectionForFile
-- for local docker test can use: CREATE SCHEMA IF NOT EXISTS "test"; USE SCHEMA "test"

-- QUEUE_PHX used for phoenix based index of queue to enable simple method for count GROUP BY etc
    CREATE TABLE IF NOT EXISTS QUEUE_PHX (
        queue_name_pk VARCHAR NOT NULL,
        queue_key_pk VARCHAR NOT NULL,
        idx_queue_key VARCHAR,
        idx_queue_name VARCHAR,
        phoenix_ts UNSIGNED_TIME,
        attempt_count UNSIGNED_LONG DEFAULT 0,
        CONSTRAINT pk PRIMARY KEY (queue_name_pk, queue_key_pk)
    ) VERSIONS=1;
      -- idx_ fields are convenience fields used for mapping into solr
      -- DROP TABLE QUEUE_PHX;

    CREATE INDEX IF NOT EXISTS QUEUE_PHX_IDX_TS ON QUEUE_PHX (queue_name_pk ASC, phoenix_ts ASC) INCLUDE (queue_key_pk);
      --  DROP INDEX IF EXISTS QUEUE_PHX_IDX_TS ON "test".QUEUE_PHX;
      --  SELECT /*+ INDEX(QUEUE_PHX_IDX_TS QUEUE_PHX) */ * FROM QUEUE_PHX WHERE queue_name_pk = 'hello' ORDER BY PHOENIX_TS;

