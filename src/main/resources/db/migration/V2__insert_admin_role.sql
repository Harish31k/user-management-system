-- Insert default admin user
INSERT INTO users (username, email, password)
VALUES ('admin', 'admin@system.com',
        '$2a$12$Aj4TvBK6x.tZUiflkkxqou5/3dquG7yMYyg9GNrC4acVK78ZH3n6S');

-- Assign ROLE_ADMIN to admin
INSERT INTO user_roles (user_id, role_id)
VALUES (
           (SELECT id FROM users WHERE email='admin@system.com'),
           (SELECT id FROM roles WHERE name='ROLE_ADMIN')
       );

-- Audit table
CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            action VARCHAR(255),
                            user_id BIGINT,
                            timestamp DATETIME
);