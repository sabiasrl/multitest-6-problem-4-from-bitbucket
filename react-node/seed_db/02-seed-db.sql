-- Seed Data for School Management System
-- This file populates the database with initial data

-- Insert Roles
INSERT INTO roles (id, name) VALUES
    (1, 'Admin'),
    (2, 'Teacher'),
    (3, 'Student'),
    (4, 'Staff')
ON CONFLICT DO NOTHING;

-- Reset sequence for roles
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));

-- Insert Departments
INSERT INTO departments (id, name, description) VALUES
    (1, 'Mathematics', 'Mathematics Department'),
    (2, 'Science', 'Science Department'),
    (3, 'English', 'English Language Department'),
    (4, 'History', 'History Department'),
    (5, 'Physical Education', 'PE Department'),
    (6, 'Administration', 'Administrative Department')
ON CONFLICT DO NOTHING;

-- Reset sequence for departments
SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));

-- Insert Classes
INSERT INTO classes (id, name) VALUES
    (1, 'Grade 1'),
    (2, 'Grade 2'),
    (3, 'Grade 3'),
    (4, 'Grade 4'),
    (5, 'Grade 5'),
    (6, 'Grade 6'),
    (7, 'Grade 7'),
    (8, 'Grade 8'),
    (9, 'Grade 9'),
    (10, 'Grade 10'),
    (11, 'Grade 11'),
    (12, 'Grade 12')
ON CONFLICT DO NOTHING;

-- Reset sequence for classes
SELECT setval('classes_id_seq', (SELECT MAX(id) FROM classes));

-- Insert Sections for each class
INSERT INTO sections (id, name, class_id) VALUES
    (1, 'A', 10), (2, 'B', 10), (3, 'C', 10),
    (4, 'A', 11), (5, 'B', 11), (6, 'C', 11),
    (7, 'A', 12), (8, 'B', 12), (9, 'C', 12)
ON CONFLICT DO NOTHING;

-- Reset sequence for sections
SELECT setval('sections_id_seq', (SELECT MAX(id) FROM sections));

-- Insert Admin User
-- Password: 3OU4zn3q6Zh9 (will be hashed by backend on first login or setup)
-- For now, using a placeholder - the actual hash will be set when password is configured
INSERT INTO users (id, name, email, password, role_id, is_active, is_email_verified, reporter_id) VALUES
    (1, 'Admin User', 'admin@school-admin.com', '$argon2id$v=19$m=65536,t=3,p=4$placeholder$will_be_updated', 1, true, true, NULL)
ON CONFLICT (email) DO NOTHING;

-- Insert Teachers
INSERT INTO users (id, name, email, role_id, is_active, is_email_verified, reporter_id) VALUES
    (2, 'John Smith', 'john.smith@school.com', 2, true, true, 1),
    (3, 'Sarah Johnson', 'sarah.johnson@school.com', 2, true, true, 1),
    (4, 'Michael Brown', 'michael.brown@school.com', 2, true, true, 1),
    (5, 'Emily Davis', 'emily.davis@school.com', 2, true, true, 1),
    (6, 'David Wilson', 'david.wilson@school.com', 2, true, true, 1)
ON CONFLICT (email) DO NOTHING;

-- Insert Students
INSERT INTO users (id, name, email, role_id, is_active, is_email_verified, reporter_id) VALUES
    (7, 'Alice Williams', 'alice.williams@student.school.com', 3, true, true, 2),
    (8, 'Bob Martinez', 'bob.martinez@student.school.com', 3, true, true, 2),
    (9, 'Charlie Garcia', 'charlie.garcia@student.school.com', 3, true, true, 2),
    (10, 'Diana Rodriguez', 'diana.rodriguez@student.school.com', 3, true, true, 3),
    (11, 'Ethan Lee', 'ethan.lee@student.school.com', 3, true, true, 3),
    (12, 'Fiona Taylor', 'fiona.taylor@student.school.com', 3, true, true, 3),
    (13, 'George Anderson', 'george.anderson@student.school.com', 3, true, true, 4),
    (14, 'Hannah Thomas', 'hannah.thomas@student.school.com', 3, true, true, 4),
    (15, 'Ian Jackson', 'ian.jackson@student.school.com', 3, true, true, 4),
    (16, 'Julia White', 'julia.white@student.school.com', 3, true, true, 5),
    (17, 'Kevin Harris', 'kevin.harris@student.school.com', 3, true, true, 5),
    (18, 'Luna Martin', 'luna.martin@student.school.com', 3, true, true, 5)
ON CONFLICT (email) DO NOTHING;

-- Insert Staff
INSERT INTO users (id, name, email, role_id, is_active, is_email_verified, reporter_id) VALUES
    (19, 'Robert Thompson', 'robert.thompson@school.com', 4, true, true, 1),
    (20, 'Lisa Moore', 'lisa.moore@school.com', 4, true, true, 1)
ON CONFLICT (email) DO NOTHING;

-- Reset sequence for users
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Insert User Profiles for Students
INSERT INTO user_profiles (user_id, phone, gender, dob, class_name, section_name, roll, father_name, father_phone, mother_name, mother_phone, guardian_name, guardian_phone, relation_of_guardian, current_address, permanent_address, admission_dt) VALUES
    (7, '555-0101', 'female', '2008-05-15', 'Grade 10', 'A', '101', 'William Williams', '555-0102', 'Mary Williams', '555-0103', 'William Williams', '555-0102', 'Father', '123 Main St, City', '123 Main St, City', '2020-09-01'),
    (8, '555-0201', 'male', '2008-07-22', 'Grade 10', 'A', '102', 'Carlos Martinez', '555-0202', 'Maria Martinez', '555-0203', 'Carlos Martinez', '555-0202', 'Father', '456 Oak Ave, City', '456 Oak Ave, City', '2020-09-01'),
    (9, '555-0301', 'male', '2008-03-10', 'Grade 10', 'B', '103', 'Jose Garcia', '555-0302', 'Ana Garcia', '555-0303', 'Jose Garcia', '555-0302', 'Father', '789 Pine Rd, City', '789 Pine Rd, City', '2020-09-01'),
    (10, '555-0401', 'female', '2007-11-18', 'Grade 11', 'A', '201', 'Miguel Rodriguez', '555-0402', 'Carmen Rodriguez', '555-0403', 'Miguel Rodriguez', '555-0402', 'Father', '321 Elm St, City', '321 Elm St, City', '2019-09-01'),
    (11, '555-0501', 'male', '2007-09-25', 'Grade 11', 'A', '202', 'James Lee', '555-0502', 'Susan Lee', '555-0503', 'James Lee', '555-0502', 'Father', '654 Maple Dr, City', '654 Maple Dr, City', '2019-09-01'),
    (12, '555-0601', 'female', '2007-12-05', 'Grade 11', 'B', '203', 'Robert Taylor', '555-0602', 'Jennifer Taylor', '555-0603', 'Robert Taylor', '555-0602', 'Father', '987 Cedar Ln, City', '987 Cedar Ln, City', '2019-09-01'),
    (13, '555-0701', 'male', '2006-04-14', 'Grade 12', 'A', '301', 'Mark Anderson', '555-0702', 'Patricia Anderson', '555-0703', 'Mark Anderson', '555-0702', 'Father', '147 Birch Way, City', '147 Birch Way, City', '2018-09-01'),
    (14, '555-0801', 'female', '2006-06-30', 'Grade 12', 'A', '302', 'Daniel Thomas', '555-0802', 'Linda Thomas', '555-0803', 'Daniel Thomas', '555-0802', 'Father', '258 Spruce Ct, City', '258 Spruce Ct, City', '2018-09-01'),
    (15, '555-0901', 'male', '2006-08-12', 'Grade 12', 'B', '303', 'Christopher Jackson', '555-0902', 'Nancy Jackson', '555-0903', 'Christopher Jackson', '555-0902', 'Father', '369 Willow St, City', '369 Willow St, City', '2018-09-01'),
    (16, '555-1001', 'female', '2008-01-20', 'Grade 10', 'C', '104', 'Andrew White', '555-1002', 'Karen White', '555-1003', 'Andrew White', '555-1002', 'Father', '741 Ash Blvd, City', '741 Ash Blvd, City', '2020-09-01'),
    (17, '555-1101', 'male', '2008-02-28', 'Grade 10', 'C', '105', 'Brian Harris', '555-1102', 'Betty Harris', '555-1103', 'Brian Harris', '555-1102', 'Father', '852 Poplar Ave, City', '852 Poplar Ave, City', '2020-09-01'),
    (18, '555-1201', 'female', '2007-10-08', 'Grade 11', 'C', '204', 'Edward Martin', '555-1202', 'Helen Martin', '555-1203', 'Edward Martin', '555-1202', 'Father', '963 Hickory Dr, City', '963 Hickory Dr, City', '2019-09-01')
ON CONFLICT DO NOTHING;

-- Insert Access Controls (Basic menu items)
INSERT INTO access_controls (id, name, path, icon, parent_path, hierarchy_id, type) VALUES
    (1, 'Dashboard', '/app/dashboard', 'dashboard', NULL, 1, 'menu'),
    (2, 'Students', '/app/students', 'students', NULL, 2, 'menu'),
    (3, 'Staff', '/app/staffs', 'staff', NULL, 3, 'menu'),
    (4, 'Classes', '/app/classes', 'classes', NULL, 4, 'menu'),
    (5, 'Notices', '/app/notices', 'notices', NULL, 5, 'menu'),
    (6, 'Leave', '/app/leave', 'leave', NULL, 6, 'menu'),
    (7, 'Departments', '/app/departments', 'departments', NULL, 7, 'menu'),
    (8, 'Roles & Permissions', '/app/roles', 'rolesAndPermissions', NULL, 8, 'menu')
ON CONFLICT DO NOTHING;

-- Reset sequence for access_controls
SELECT setval('access_controls_id_seq', (SELECT MAX(id) FROM access_controls));

-- Insert Permissions (Admin has all access)
INSERT INTO permissions (role_id, access_control_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
    (2, 1), (2, 2), (2, 4), (2, 5),
    (3, 1), (3, 5),
    (4, 1), (4, 5), (4, 6)
ON CONFLICT DO NOTHING;

-- Insert Leave Policies
INSERT INTO leave_policies (id, name, max_days, description) VALUES
    (1, 'Annual Leave', 15, 'Annual vacation leave'),
    (2, 'Sick Leave', 10, 'Medical leave'),
    (3, 'Study Leave', 5, 'Leave for educational purposes'),
    (4, 'Family Leave', 3, 'Family emergency leave')
ON CONFLICT DO NOTHING;

-- Reset sequence for leave_policies
SELECT setval('leave_policies_id_seq', (SELECT MAX(id) FROM leave_policies));

-- Insert Sample Notices
INSERT INTO notices (id, title, description, author_id, status_id, recipient_type, created_at) VALUES
    (1, 'Welcome Back to School', 'Welcome all students and staff to the new academic year. We hope you had a great summer!', 1, 1, 'EV', CURRENT_TIMESTAMP),
    (2, 'Parent-Teacher Meeting', 'Parent-teacher meetings will be held next week. Please schedule your appointments.', 2, 1, 'EV', CURRENT_TIMESTAMP),
    (3, 'Science Fair Registration', 'Registration for the annual science fair is now open. Submit your projects by next Friday.', 3, 1, 'SP', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Reset sequence for notices
SELECT setval('notices_id_seq', (SELECT MAX(id) FROM notices));

-- Insert Class Teachers
INSERT INTO class_teachers (class_id, section_id, teacher_id) VALUES
    (10, 1, 2), (10, 2, 3), (10, 3, 4),
    (11, 4, 2), (11, 5, 3), (11, 6, 4),
    (12, 7, 5), (12, 8, 6), (12, 9, 2)
ON CONFLICT DO NOTHING;

-- Note: Admin password hash needs to be generated
-- The password is: 3OU4zn3q6Zh9
-- This will be set when the admin user sets up their password through the system
-- For immediate use, you can generate the hash using the backend container:
-- docker-compose exec backend node -e "const argon2 = require('argon2'); argon2.hash('3OU4zn3q6Zh9').then(hash => console.log(hash))"
-- Then update the users table with: UPDATE users SET password = '<generated_hash>' WHERE email = 'admin@school-admin.com';
