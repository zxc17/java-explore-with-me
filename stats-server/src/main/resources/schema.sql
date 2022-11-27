CREATE TABLE IF NOT EXISTS statistics
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app       VARCHAR(100)                            NOT NULL,
    uri       VARCHAR(1000)                           NOT NULL,
    ip        VARCHAR(40)                             NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT pk_statistics PRIMARY KEY (id)
);