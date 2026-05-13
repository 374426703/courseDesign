CREATE TABLE IF NOT EXISTS t_invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(64),
    invoice_code VARCHAR(64),
    seller_name VARCHAR(128),
    seller_tax_no VARCHAR(64),
    buyer_name VARCHAR(128),
    buyer_tax_no VARCHAR(64),
    amount DECIMAL(12,2),
    tax_amount DECIMAL(12,2),
    total_amount DECIMAL(12,2),
    invoice_date DATETIME,
    invoice_type VARCHAR(32),
    remark VARCHAR(512),
    xml_content LONGTEXT,
    digital_fingerprint VARCHAR(256),
    digital_signature LONGTEXT,
    status INT DEFAULT 0,
    block_hash VARCHAR(256),
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS t_block (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_index INT,
    previous_hash VARCHAR(256),
    hash VARCHAR(256),
    data LONGTEXT,
    timestamp BIGINT,
    nonce INT,
    invoice_id BIGINT,
    create_time DATETIME
);
