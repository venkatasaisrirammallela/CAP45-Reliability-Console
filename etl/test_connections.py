
import os
import getpass
import snowflake.connector
from dotenv import load_dotenv

# Load environment variables from .env
load_dotenv()

try:
    # Ask for password securely
    password = getpass.getpass("Enter Snowflake password: ")

    print("\nConnecting to Snowflake...")

    # Establish Snowflake connection
    with snowflake.connector.connect(
        account=os.environ["SNOWFLAKE_ACCOUNT"],
        user=os.environ["SNOWFLAKE_USER"],
        password=password,
        authenticator="snowflake",
        warehouse=os.environ["SNOWFLAKE_WAREHOUSE"],
        database=os.environ["SNOWFLAKE_DATABASE"],
        schema=os.environ["SNOWFLAKE_SCHEMA"],
        role=os.environ["SNOWFLAKE_ROLE"],
        login_timeout=30,
        network_timeout=30,
        client_session_keep_alive=False
    ) as conn:

        with conn.cursor() as cursor:

            # Check Snowflake connection details
            cursor.execute("""
                SELECT
                    CURRENT_USER(),
                    CURRENT_DATABASE(),
                    CURRENT_SCHEMA(),
                    CURRENT_WAREHOUSE(),
                    CURRENT_ROLE()
            """)

            result = cursor.fetchone()

            print("\nSnowflake connected successfully!")
            print("----------------------------------")
            print("User:", result[0])
            print("Database:", result[1])
            print("Schema:", result[2])
            print("Warehouse:", result[3])
            print("Role:", result[4])

            # Check existing tables
            cursor.execute("SHOW TABLES")

            tables = cursor.fetchall()

            print("\nExisting Snowflake Tables:")
            if tables:
                for table in tables:
                    print("-", table[1])
            else:
                print("No tables found.")

except KeyError as error:
    print("\nMissing environment variable:", error)

except snowflake.connector.errors.Error as error:
    print("\nSnowflake connection failed.")
    print("Error:", error.msg)

except Exception as error:
    print("\nUnexpected error:", error)
