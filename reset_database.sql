-- Complete database reset script
-- WARNING: This will delete ALL data!


-- Drop all tables in correct order (to respect foreign keys)
DROP TABLE IF EXISTS rating_likes CASCADE;
DROP TABLE IF EXISTS favorites CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS media CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- The application will recreate them with the correct schema on next start
-- Or you can run the init.sql script

COMMENT ON DATABASE postgres IS 'Database reset completed. Restart the application to recreate tables.';

