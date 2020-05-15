CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entry CLOB,
    category VARCHAR(255),
    creationdatetime TIMESTAMP
);