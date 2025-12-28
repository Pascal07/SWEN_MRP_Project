-- MRP Database Schema Initialization Script
-- This script creates all necessary tables for the Media Rating Platform

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Media table
CREATE TABLE IF NOT EXISTS media (
    media_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre TEXT,
    media_type VARCHAR(50),
    release_year INTEGER,
    director VARCHAR(255),
    cast_members TEXT,
    creator_user_id INTEGER,
    age_restriction INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ratings table
CREATE TABLE IF NOT EXISTS ratings (
    rating_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    media_id INTEGER NOT NULL,
    rating_value INTEGER NOT NULL CHECK (rating_value >= 1 AND rating_value <= 5),
    comment TEXT,
    confirmed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE,
    UNIQUE (user_id, media_id)
);

-- Favorites table
CREATE TABLE IF NOT EXISTS favorites (
    favorite_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    media_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE,
    UNIQUE (user_id, media_id)
);

-- Rating likes table (users can like ratings)
CREATE TABLE IF NOT EXISTS rating_likes (
    rating_like_id SERIAL PRIMARY KEY,
    rating_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(rating_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (rating_id, user_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_ratings_media_id ON ratings(media_id);
CREATE INDEX IF NOT EXISTS idx_ratings_user_id ON ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_media_id ON favorites(media_id);
CREATE INDEX IF NOT EXISTS idx_rating_likes_rating_id ON rating_likes(rating_id);
CREATE INDEX IF NOT EXISTS idx_rating_likes_user_id ON rating_likes(user_id);

-- Insert some sample data (optional)
-- Sample users
INSERT INTO users (username, email, password_hash) VALUES
    ('admin', 'admin@mrp.com', '$2a$10$dummyHashForAdmin')
ON CONFLICT (username) DO NOTHING;

-- Sample media entries
INSERT INTO media (title, description, genre, media_type, release_year, director, age_restriction) VALUES
    ('The Shawshank Redemption', 'Two imprisoned men bond over a number of years.', 'Drama', 'movie', 1994, 'Frank Darabont', 12),
    ('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control.', 'Crime,Drama', 'movie', 1972, 'Francis Ford Coppola', 16),
    ('The Dark Knight', 'When the menace known as the Joker wreaks havoc on Gotham.', 'Action,Crime,Drama', 'movie', 2008, 'Christopher Nolan', 12),
    ('Breaking Bad', 'A high school chemistry teacher turned methamphetamine producer.', 'Crime,Drama,Thriller', 'series', 2008, 'Vince Gilligan', 16),
    ('The Witcher 3', 'An open-world RPG adventure in a fantasy universe.', 'RPG,Action,Adventure', 'game', 2015, 'CD Projekt Red', 18)
ON CONFLICT DO NOTHING;

COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE media IS 'Stores media entries (movies, series, games)';
COMMENT ON TABLE ratings IS 'Stores user ratings for media';
COMMENT ON TABLE favorites IS 'Stores user favorites';
COMMENT ON TABLE rating_likes IS 'Stores likes on ratings by users';

