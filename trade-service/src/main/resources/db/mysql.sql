-- To drop and recreate the public schema in PostgreSQL, you can use the following SQL commands:
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;