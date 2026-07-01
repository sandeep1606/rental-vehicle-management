-- Seed data: 4 branches, sample vehicles, customers, and bookings/payments/maintenance.

INSERT INTO branches (name, address, phone, manager_name, opening_hours, active) VALUES
('Downtown Central',  '123 Main St, City Center',      '+1-555-0101', 'Alice Johnson', 'Mon-Sun 07:00-21:00', TRUE),
('Airport Terminal',  '500 Airport Blvd, Terminal 2',   '+1-555-0102', 'Brian Lee',     '24/7',                TRUE),
('Uptown',            '45 Uptown Ave, Northside',       '+1-555-0103', 'Carla Gomez',   'Mon-Sat 08:00-20:00', TRUE),
('Suburban Mall',     '900 Mall Ring Rd, Suburbia',     '+1-555-0104', 'David Kim',     'Mon-Sun 09:00-19:00', TRUE);

-- Downtown Central vehicles
INSERT INTO vehicles (plate_number, vin, type, brand, model, year, mileage, fuel_type, transmission, daily_rate, status, branch_id) VALUES
('DTN-1001', '1HGCM82633A004001', 'SEDAN',       'Toyota', 'Camry',    2023, 12000, 'PETROL',   'AUTOMATIC', 55.00, 'AVAILABLE',   (SELECT id FROM branches WHERE name = 'Downtown Central')),
('DTN-1002', '1HGCM82633A004002', 'SUV',         'Honda',  'CR-V',     2022, 18000, 'PETROL',   'AUTOMATIC', 75.00, 'AVAILABLE',   (SELECT id FROM branches WHERE name = 'Downtown Central')),
('DTN-1003', '1HGCM82633A004003', 'HATCHBACK',   'Ford',   'Focus',    2021, 30500, 'PETROL',   'MANUAL',    45.00, 'AVAILABLE',   (SELECT id FROM branches WHERE name = 'Downtown Central')),
('DTN-1004', '1HGCM82633A004004', 'SEDAN',       'Tesla',  'Model 3',  2023,  8000, 'ELECTRIC', 'AUTOMATIC', 95.00, 'AVAILABLE',   (SELECT id FROM branches WHERE name = 'Downtown Central')),
('DTN-1005', '1HGCM82633A004005', 'VAN',         'Ford',   'Transit',  2020, 52000, 'DIESEL',   'MANUAL',    85.00, 'MAINTENANCE', (SELECT id FROM branches WHERE name = 'Downtown Central'));

-- Airport Terminal vehicles
INSERT INTO vehicles (plate_number, vin, type, brand, model, year, mileage, fuel_type, transmission, daily_rate, status, branch_id) VALUES
('APT-2001', '1HGCM82633A004006', 'LUXURY',      'BMW',       '5 Series',  2023,  9000, 'PETROL', 'AUTOMATIC', 120.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Airport Terminal')),
('APT-2002', '1HGCM82633A004007', 'SUV',         'Toyota',    'RAV4',      2022, 21000, 'HYBRID', 'AUTOMATIC',  80.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Airport Terminal')),
('APT-2003', '1HGCM82633A004008', 'SEDAN',       'Nissan',    'Altima',    2021, 34000, 'PETROL', 'AUTOMATIC',  50.00, 'RENTED',    (SELECT id FROM branches WHERE name = 'Airport Terminal')),
('APT-2004', '1HGCM82633A004009', 'SUV',         'Jeep',      'Wrangler',  2020, 41000, 'PETROL', 'MANUAL',     90.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Airport Terminal')),
('APT-2005', '1HGCM82633A004010', 'TRUCK',       'Chevrolet', 'Silverado', 2022, 15000, 'DIESEL', 'AUTOMATIC', 100.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Airport Terminal'));

-- Uptown vehicles
INSERT INTO vehicles (plate_number, vin, type, brand, model, year, mileage, fuel_type, transmission, daily_rate, status, branch_id) VALUES
('UPT-3001', '1HGCM82633A004011', 'SEDAN',       'Audi',          'A4',       2023, 11000, 'PETROL', 'AUTOMATIC',  88.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Uptown')),
('UPT-3002', '1HGCM82633A004012', 'LUXURY',      'Mercedes-Benz', 'C-Class',  2023,  7000, 'PETROL', 'AUTOMATIC', 130.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Uptown')),
('UPT-3003', '1HGCM82633A004013', 'HATCHBACK',   'Kia',           'Soul',     2021, 28000, 'PETROL', 'AUTOMATIC',  42.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Uptown')),
('UPT-3004', '1HGCM82633A004014', 'SUV',         'Hyundai',       'Tucson',   2022, 19500, 'PETROL', 'AUTOMATIC',  78.00, 'RESERVED',  (SELECT id FROM branches WHERE name = 'Uptown')),
('UPT-3005', '1HGCM82633A004015', 'CONVERTIBLE', 'Mazda',         'MX-5',     2022, 13500, 'PETROL', 'MANUAL',     99.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Uptown'));

-- Suburban Mall vehicles
INSERT INTO vehicles (plate_number, vin, type, brand, model, year, mileage, fuel_type, transmission, daily_rate, status, branch_id) VALUES
('SUB-4001', '1HGCM82633A004016', 'SEDAN',     'Honda',      'Civic',  2022, 16000, 'PETROL', 'AUTOMATIC', 48.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Suburban Mall')),
('SUB-4002', '1HGCM82633A004017', 'VAN',       'Toyota',     'Sienna', 2021, 24000, 'PETROL', 'AUTOMATIC', 82.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Suburban Mall')),
('SUB-4003', '1HGCM82633A004018', 'SUV',       'Ford',       'Escape', 2023,  6000, 'PETROL', 'AUTOMATIC', 72.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Suburban Mall')),
('SUB-4004', '1HGCM82633A004019', 'HATCHBACK', 'Chevrolet',  'Spark',  2020, 61000, 'PETROL', 'MANUAL',    35.00, 'RETIRED',   (SELECT id FROM branches WHERE name = 'Suburban Mall')),
('SUB-4005', '1HGCM82633A004020', 'SEDAN',     'Volkswagen', 'Jetta',  2021, 27000, 'DIESEL', 'AUTOMATIC', 52.00, 'AVAILABLE', (SELECT id FROM branches WHERE name = 'Suburban Mall'));

-- Customers
INSERT INTO customers (full_name, email, phone, driver_license_number, address, date_of_birth, blacklisted) VALUES
('John Smith',       'john.smith@example.com',       '+1-555-1001', 'DL-100001', '12 Elm St, City Center',  '1985-04-12', FALSE),
('Maria Garcia',     'maria.garcia@example.com',      '+1-555-1002', 'DL-100002', '34 Oak Ave, Northside',   '1990-08-23', FALSE),
('Wei Chen',         'wei.chen@example.com',          '+1-555-1003', 'DL-100003', '56 Pine Rd, Suburbia',    '1978-12-01', FALSE),
('Fatima Al-Sayed',  'fatima.alsayed@example.com',    '+1-555-1004', 'DL-100004', '78 Birch Blvd, Downtown', '1995-02-17', FALSE),
('Robert Brown',     'robert.brown@example.com',      '+1-555-1005', 'DL-100005', '90 Cedar Ln, Uptown',     '1982-06-30', TRUE),
('Aiko Tanaka',      'aiko.tanaka@example.com',       '+1-555-1006', 'DL-100006', '11 Maple Dr, City Center','1993-11-05', FALSE),
('Liam O''Connor',   'liam.oconnor@example.com',      '+1-555-1007', 'DL-100007', '22 Spruce Ct, Northside', '1988-09-14', FALSE),
('Priya Patel',      'priya.patel@example.com',       '+1-555-1008', 'DL-100008', '33 Willow Way, Suburbia', '1991-03-22', FALSE);

-- Upcoming reservation on the Uptown Hyundai Tucson (which is why its status is RESERVED)
INSERT INTO reservations (customer_id, vehicle_id, branch_id, start_date, end_date, status, estimated_total)
SELECT c.id, v.id, v.branch_id, CURRENT_DATE + INTERVAL '7 day', CURRENT_DATE + INTERVAL '10 day', 'CONFIRMED', 234.00
FROM customers c, vehicles v
WHERE c.email = 'priya.patel@example.com' AND v.plate_number = 'UPT-3004';

-- Active rental on the Airport Nissan Altima (which is why its status is RENTED)
INSERT INTO rentals (customer_id, vehicle_id, branch_id, start_date, planned_end_date, daily_rate, status)
SELECT c.id, v.id, v.branch_id, CURRENT_DATE - INTERVAL '2 day', CURRENT_DATE + INTERVAL '3 day', v.daily_rate, 'ACTIVE'
FROM customers c, vehicles v
WHERE c.email = 'liam.oconnor@example.com' AND v.plate_number = 'APT-2003';

-- Completed rental (returned 1 day late) on the Downtown Toyota Camry, with a paid invoice
INSERT INTO rentals (customer_id, vehicle_id, branch_id, start_date, planned_end_date, actual_return_date, daily_rate, total_amount, late_fee, status)
SELECT c.id, v.id, v.branch_id, CURRENT_DATE - INTERVAL '20 day', CURRENT_DATE - INTERVAL '15 day', CURRENT_DATE - INTERVAL '14 day', v.daily_rate, 357.50, 82.50, 'COMPLETED'
FROM customers c, vehicles v
WHERE c.email = 'john.smith@example.com' AND v.plate_number = 'DTN-1001';

INSERT INTO payments (rental_id, amount, status, method, transaction_ref, paid_at)
SELECT r.id, 357.50, 'PAID', 'CREDIT_CARD', 'TXN-SEED-0001', (CURRENT_DATE - INTERVAL '14 day')::timestamptz
FROM rentals r
JOIN customers c ON c.id = r.customer_id
WHERE c.email = 'john.smith@example.com' AND r.status = 'COMPLETED';

-- Maintenance in progress on the Downtown Ford Transit (which is why its status is MAINTENANCE)
INSERT INTO maintenance_records (vehicle_id, branch_id, description, cost, status, scheduled_date)
SELECT v.id, v.branch_id, 'Scheduled oil change and brake inspection', 150.00, 'IN_PROGRESS', CURRENT_DATE - INTERVAL '1 day'
FROM vehicles v
WHERE v.plate_number = 'DTN-1005';
