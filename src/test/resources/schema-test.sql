-- Schema for yomu-core-api tests (H2 in-memory)
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS gamification;
CREATE SCHEMA IF NOT EXISTS quiz;

CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student',
    google_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gamification.clans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    tier VARCHAR(20) NOT NULL DEFAULT 'bronze',
    total_score DECIMAL(10,2) DEFAULT 0,
    leader_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gamification.clan_members (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'member',
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(clan_id, user_id)
);

CREATE TABLE IF NOT EXISTS gamification.achievements (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    milestone INT NOT NULL,
    achievement_type VARCHAR(50) DEFAULT 'reading_count',
    icon_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gamification.user_achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    achievement_id UUID NOT NULL,
    unlocked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_visible BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(user_id, achievement_id)
);

CREATE TABLE IF NOT EXISTS quiz.completed_readings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reading_id UUID NOT NULL,
    score INT NOT NULL,
    accuracy DECIMAL(5,2) NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, reading_id)
);

CREATE TABLE IF NOT EXISTS quiz.quiz_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reading_id UUID NOT NULL,
    answers JSON NOT NULL,
    score INT NOT NULL,
    accuracy DECIMAL(5,2) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);