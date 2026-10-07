-- TODO 7.2: Tao schema ban dau
CREATE TABLE bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    showtime_id VARCHAR(24) NOT NULL,
    booking_time DATETIME NOT NULL,
    total_price DECIMAL(19,4) NOT NULL
);

CREATE TABLE tickets (
    ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_code VARCHAR(10) NOT NULL,
    ticket_price DECIMAL(19,4) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);
