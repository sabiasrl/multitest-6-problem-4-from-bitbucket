-- Database Schema for School Management System
-- This file creates all necessary tables

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role_id INTEGER REFERENCES roles(id),
    is_active BOOLEAN DEFAULT false,
    is_email_verified BOOLEAN DEFAULT false,
    reporter_id INTEGER REFERENCES users(id),
    last_login TIMESTAMP,
    status_last_reviewed_dt TIMESTAMP,
    status_last_reviewer_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User profiles table
CREATE TABLE IF NOT EXISTS user_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(20),
    gender VARCHAR(20),
    dob DATE,
    class_name VARCHAR(100),
    section_name VARCHAR(100),
    roll VARCHAR(50),
    father_name VARCHAR(255),
    father_phone VARCHAR(20),
    mother_name VARCHAR(255),
    mother_phone VARCHAR(20),
    guardian_name VARCHAR(255),
    guardian_phone VARCHAR(20),
    relation_of_guardian VARCHAR(100),
    current_address TEXT,
    permanent_address TEXT,
    admission_dt DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User refresh tokens table
CREATE TABLE IF NOT EXISTS user_refresh_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Access controls table
CREATE TABLE IF NOT EXISTS access_controls (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(100),
    parent_path VARCHAR(255),
    hierarchy_id INTEGER,
    type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    access_control_id INTEGER NOT NULL REFERENCES access_controls(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, access_control_id)
);

-- Classes table
CREATE TABLE IF NOT EXISTS classes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sections table
CREATE TABLE IF NOT EXISTS sections (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    class_id INTEGER REFERENCES classes(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, class_id)
);

-- Departments table
CREATE TABLE IF NOT EXISTS departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notices table
CREATE TABLE IF NOT EXISTS notices (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author_id INTEGER NOT NULL REFERENCES users(id),
    reviewer_id INTEGER REFERENCES users(id),
    status_id INTEGER DEFAULT 0,
    recipient_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
);

-- Leave policies table
CREATE TABLE IF NOT EXISTS leave_policies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    max_days INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User leaves table
CREATE TABLE IF NOT EXISTS user_leaves (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    leave_policy_id INTEGER REFERENCES leave_policies(id),
    from_dt DATE NOT NULL,
    to_dt DATE NOT NULL,
    note TEXT,
    status_id INTEGER DEFAULT 0,
    approver_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Class teachers table
CREATE TABLE IF NOT EXISTS class_teachers (
    id SERIAL PRIMARY KEY,
    class_id INTEGER NOT NULL REFERENCES classes(id),
    section_id INTEGER REFERENCES sections(id),
    teacher_id INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(class_id, section_id, teacher_id)
);

-- Function for student add/update
CREATE OR REPLACE FUNCTION student_add_update(payload JSONB)
RETURNS JSONB AS $$
DECLARE
    v_user_id INTEGER;
    v_role_id INTEGER := 3; -- Student role
    v_result JSONB;
    v_is_update BOOLEAN := false;
    v_email_exists BOOLEAN := false;
BEGIN
    -- Check if this is an update (userId provided)
    IF payload->>'userId' IS NOT NULL THEN
        v_user_id := (payload->>'userId')::INTEGER;
        
        -- Verify user exists and is a student
        SELECT EXISTS(SELECT 1 FROM users WHERE id = v_user_id AND role_id = v_role_id) INTO v_is_update;
        IF NOT v_is_update THEN
            RETURN jsonb_build_object(
                'status', false,
                'message', 'Student not found',
                'userId', NULL
            );
        END IF;
    END IF;
    
    -- Check email uniqueness (for create or if email is being changed)
    IF NOT v_is_update OR payload->>'email' IS NOT NULL THEN
        SELECT EXISTS(SELECT 1 FROM users WHERE email = payload->>'email' AND (NOT v_is_update OR id != v_user_id)) INTO v_email_exists;
        IF v_email_exists THEN
            RETURN jsonb_build_object(
                'status', false,
                'message', 'Email already exists',
                'userId', NULL
            );
        END IF;
    END IF;
    
    -- Create or Update User
    IF v_is_update THEN
        -- Update existing user
        UPDATE users SET
            name = COALESCE(payload->>'name', name),
            email = COALESCE(payload->>'email', email),
            is_active = COALESCE((payload->>'systemAccess')::BOOLEAN, is_active),
            updated_dt = CURRENT_TIMESTAMP
        WHERE id = v_user_id
        RETURNING id INTO v_user_id;
    ELSE
        -- Create new user
        INSERT INTO users (name, email, role_id, is_active, is_email_verified)
        VALUES (
            payload->>'name',
            payload->>'email',
            v_role_id,
            COALESCE((payload->>'systemAccess')::BOOLEAN, false),
            false
        )
        RETURNING id INTO v_user_id;
    END IF;
    
    -- Create or Update User Profile
    IF v_is_update THEN
        -- Update existing profile
        UPDATE user_profiles SET
            phone = COALESCE(payload->>'phone', phone),
            gender = COALESCE(payload->>'gender', gender),
            dob = COALESCE((payload->>'dob')::DATE, dob),
            class_name = COALESCE(payload->>'class', class_name),
            section_name = COALESCE(payload->>'section', section_name),
            roll = COALESCE(payload->>'roll', roll),
            father_name = COALESCE(payload->>'fatherName', father_name),
            father_phone = COALESCE(payload->>'fatherPhone', father_phone),
            mother_name = COALESCE(payload->>'motherName', mother_name),
            mother_phone = COALESCE(payload->>'motherPhone', mother_phone),
            guardian_name = COALESCE(payload->>'guardianName', guardian_name),
            guardian_phone = COALESCE(payload->>'guardianPhone', guardian_phone),
            relation_of_guardian = COALESCE(payload->>'relationOfGuardian', relation_of_guardian),
            current_address = COALESCE(payload->>'currentAddress', current_address),
            permanent_address = COALESCE(payload->>'permanentAddress', permanent_address),
            admission_dt = COALESCE((payload->>'admissionDate')::DATE, admission_dt),
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = v_user_id;
        
        -- If profile doesn't exist, create it
        IF NOT FOUND THEN
            INSERT INTO user_profiles (
                user_id, phone, gender, dob, class_name, section_name, roll,
                father_name, father_phone, mother_name, mother_phone,
                guardian_name, guardian_phone, relation_of_guardian,
                current_address, permanent_address, admission_dt
            )
            VALUES (
                v_user_id,
                payload->>'phone',
                payload->>'gender',
                (payload->>'dob')::DATE,
                payload->>'class',
                payload->>'section',
                payload->>'roll',
                payload->>'fatherName',
                payload->>'fatherPhone',
                payload->>'motherName',
                payload->>'motherPhone',
                payload->>'guardianName',
                payload->>'guardianPhone',
                payload->>'relationOfGuardian',
                payload->>'currentAddress',
                payload->>'permanentAddress',
                (payload->>'admissionDate')::DATE
            );
        END IF;
    ELSE
        -- Create new profile
        INSERT INTO user_profiles (
            user_id, phone, gender, dob, class_name, section_name, roll,
            father_name, father_phone, mother_name, mother_phone,
            guardian_name, guardian_phone, relation_of_guardian,
            current_address, permanent_address, admission_dt
        )
        VALUES (
            v_user_id,
            payload->>'phone',
            payload->>'gender',
            (payload->>'dob')::DATE,
            payload->>'class',
            payload->>'section',
            payload->>'roll',
            payload->>'fatherName',
            payload->>'fatherPhone',
            payload->>'motherName',
            payload->>'motherPhone',
            payload->>'guardianName',
            payload->>'guardianPhone',
            payload->>'relationOfGuardian',
            payload->>'currentAddress',
            payload->>'permanentAddress',
            (payload->>'admissionDate')::DATE
        );
    END IF;
    
    -- Return success
    IF v_is_update THEN
        v_result := jsonb_build_object(
            'status', true,
            'message', 'Student updated successfully',
            'userId', v_user_id
        );
    ELSE
        v_result := jsonb_build_object(
            'status', true,
            'message', 'Student added successfully',
            'userId', v_user_id
        );
    END IF;
    
    RETURN v_result;
EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
            'status', false,
            'message', 'Error: ' || SQLERRM,
            'userId', NULL
        );
END;
$$ LANGUAGE plpgsql;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_user_id ON user_refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_token ON user_refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_permissions_role_id ON permissions(role_id);
