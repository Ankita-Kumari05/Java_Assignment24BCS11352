CREATE DATABASE IF NOT EXISTS student_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_db;

CREATE TABLE IF NOT EXISTS students (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    email  VARCHAR(150) NOT NULL,
    course VARCHAR(100) NOT NULL,
    age    INT          NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_students_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Optional sample data. Hibernate also creates this table automatically
-- when spring.jpa.hibernate.ddl-auto=update, so this script is only a convenience.
INSERT INTO students (name, email, course, age)
SELECT 'Aarav Sharma', 'aarav.sharma@example.com', 'Computer Science', 20
WHERE NOT EXISTS (SELECT 1 FROM students WHERE email = 'aarav.sharma@example.com');

INSERT INTO students (name, email, course, age)
SELECT 'Priya Verma', 'priya.verma@example.com', 'Information Technology', 22
WHERE NOT EXISTS (SELECT 1 FROM students WHERE email = 'priya.verma@example.com');

INSERT INTO students (name, email, course, age)
SELECT 'Rohan Mehta', 'rohan.mehta@example.com', 'Computer Science', 21
WHERE NOT EXISTS (SELECT 1 FROM students WHERE email = 'rohan.mehta@example.com');
