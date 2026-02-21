CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100),
                       email VARCHAR(150) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       last_login DATETIME
);

CREATE TABLE user_roles (
                            user_id BIGINT,
                            role_id BIGINT,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 🔥 ONLY ROLE_ prefix roles
INSERT INTO roles(name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');