
import os
import re
import uuid
from decimal import Decimal
from datetime import date, datetime, time

import psycopg2
import snowflake.connector
from dotenv import load_dotenv

load_dotenv()

TABLES = [
    "services",
    "simulation_runs",
    "request_events",
    "release_decisions",
    "incidents",
    "slo_policies",
    "telemetry_runs",
]


def sf_type(pg_type):
    mapping = {
        "smallint": "NUMBER(38,0)",
        "integer": "NUMBER(38,0)",
        "bigint": "NUMBER(38,0)",
        "numeric": "NUMBER(38,10)",
        "real": "FLOAT",
        "double precision": "FLOAT",
        "boolean": "BOOLEAN",
        "date": "DATE",
        "timestamp without time zone": "TIMESTAMP_NTZ",
        "timestamp with time zone": "TIMESTAMP_TZ",
        "time without time zone": "TIME",
        "character varying": "VARCHAR",
        "character": "VARCHAR",
        "text": "VARCHAR",
        "uuid": "VARCHAR",
        "json": "VARCHAR",
        "jsonb": "VARCHAR",
        "bytea": "BINARY",
    }
    return mapping.get(pg_type, "VARCHAR")


def identifier(name):
    if not re.fullmatch(r"[a-z_][a-z0-9_]*", name):
        raise ValueError(f"Unexpected identifier: {name}")
    return '"' + name.upper() + '"'


def convert_value(value):
    if isinstance(value, (dict, list)):
        import json
        return json.dumps(value, default=str)
    if isinstance(value, Decimal):
        return value
    if isinstance(value, (datetime, date, time)):
        return value
    if isinstance(value, memoryview):
        return bytes(value)
    return value


def main():
    pg = psycopg2.connect(
        host=os.environ["RDS_HOST"],
        port=os.getenv("RDS_PORT", "5432"),
        dbname=os.environ["RDS_DATABASE"],
        user=os.environ["RDS_USER"],
        password=os.environ["RDS_PASSWORD"],
        connect_timeout=15,
    )

    sf_password = os.getenv("SNOWFLAKE_PASSWORD")
    if not sf_password:
        import getpass
        sf_password = getpass.getpass(
            "Enter Snowflake password: "
        )

    try:
        sf = snowflake.connector.connect(
            account=os.environ["SNOWFLAKE_ACCOUNT"],
            user=os.environ["SNOWFLAKE_USER"],
            password=sf_password,
            authenticator="snowflake",
            warehouse=os.environ["SNOWFLAKE_WAREHOUSE"],
            database=os.environ["SNOWFLAKE_DATABASE"],
            schema=os.environ["SNOWFLAKE_SCHEMA"],
            role=os.environ["SNOWFLAKE_ROLE"],
            login_timeout=30,
            network_timeout=60,
            client_session_keep_alive=False,
        )
    except Exception:
        pg.close()
        raise

    try:
        with pg.cursor() as pc, sf.cursor() as sc:
            for table in TABLES:
                print(f"\nProcessing {table}...")

                pc.execute("""
                    SELECT column_name, data_type
                    FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = %s
                    ORDER BY ordinal_position
                """, (table,))

                columns = pc.fetchall()

                if not columns:
                    raise RuntimeError(
                        f"No columns found for {table}"
                    )

                names = [identifier(c[0]) for c in columns]
                definitions = ", ".join(
                    f"{identifier(name)} {sf_type(dtype)}"
                    for name, dtype in columns
                )

                target = identifier(table)
                stage = identifier(
                    "etl_stage_" + uuid.uuid4().hex[:12]
                )

                sc.execute(
                    f"CREATE TABLE IF NOT EXISTS "
                    f"{target} ({definitions})"
                )

                sc.execute(
                    f"CREATE TEMPORARY TABLE {stage} "
                    f"({definitions})"
                )

                pc.execute(f'SELECT * FROM public."{table}"')
                source_rows = pc.fetchall()

                if source_rows:
                    placeholders = ", ".join(
                        ["%s"] * len(names)
                    )
                    insert_sql = (
                        f"INSERT INTO {stage} "
                        f"({', '.join(names)}) "
                        f"VALUES ({placeholders})"
                    )

                    batch_size = 500

                    for i in range(
                        0, len(source_rows), batch_size
                    ):
                        batch = [
                            tuple(convert_value(v) for v in row)
                            for row in source_rows[
                                i:i + batch_size
                            ]
                        ]
                        sc.executemany(insert_sql, batch)

                sc.execute(
                    f"SELECT COUNT(*) FROM {stage}"
                )
                staged_count = sc.fetchone()[0]

                if staged_count != len(source_rows):
                    raise RuntimeError(
                        f"Count mismatch for {table}"
                    )

                # Replace destination rows only after
                # staging has succeeded.
                sf.autocommit(False)
                try:
                    sc.execute(f"DELETE FROM {target}")

                    sc.execute(
                        f"INSERT INTO {target} "
                        f"({', '.join(names)}) "
                        f"SELECT {', '.join(names)} "
                        f"FROM {stage}"
                    )

                    sf.commit()
                except Exception:
                    sf.rollback()
                    raise
                finally:
                    sf.autocommit(True)

                print(
                    f"SUCCESS: {table} - "
                    f"{staged_count} rows transferred"
                )

        print("\nETL completed successfully!")

    finally:
        sf.close()
        pg.close()


if __name__ == "__main__":
    main()
