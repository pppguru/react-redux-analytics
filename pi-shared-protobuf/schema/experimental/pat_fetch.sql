-- noinspection SqlDialectInspectionForFile
-- for local docker test can use: CREATE SCHEMA IF NOT EXISTS "test"; USE SCHEMA "test"

-- PAT_FETCH used for patent fetch requests
   CREATE TABLE IF NOT EXISTS PAT_FETCH (
       oc_pk CHAR(2) NOT NULL,
       uni_pat_pk VARCHAR NOT NULL,

       -- idx_ fields are convenience fields used for mapping into solr
       idx_oc CHAR(2),
       idx_uni_pat VARCHAR,

       -- when this patent fetch can be attempted again - note that DATE type is not SQL92 compliant
       phoenix_ts DATE,

       ex_io_cnt UNSIGNED_LONG ,
       ex_ufe_cnt UNSIGNED_LONG ,
       ex_nf_cnt UNSIGNED_LONG ,
       CONSTRAINT pk PRIMARY KEY (oc_pk, uni_pat_pk)
   ) VERSIONS=1;
     -- DROP TABLE PAT_FETCH;

   CREATE INDEX IF NOT EXISTS PAT_FETCH_IDX_TS ON PAT_FETCH (oc_pk ASC, phoenix_ts ASC) INCLUDE (uni_pat_pk);
     --  DROP INDEX IF EXISTS PAT_FETCH_IDX_TS ON "test".PAT_FETCH;
     --  SELECT /*+ INDEX(PAT_FETCH_IDX_TS PAT_FETCH) */ * FROM PAT_FETCH WHERE oc_pk = 'AU' ORDER BY PHOENIX_TS;
