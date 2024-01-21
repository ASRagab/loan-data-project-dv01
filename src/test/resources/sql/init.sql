CREATE TABLE IF NOT EXISTS loan_data
(
    id              BIGINT PRIMARY KEY,
    loan_amount     INTEGER,
    funded_amount   INTEGER,
    term            VARCHAR(255),
    interest_rate   VARCHAR(10),
    grade           VARCHAR(2),
    sub_grade       VARCHAR(3),
    employee_title  VARCHAR(255),
    home_ownership  VARCHAR(255),
    issued_date     VARCHAR(10),
    loan_status     VARCHAR(255),
    zip_code        VARCHAR(10),
    state_address   VARCHAR(2),
    fico_range_low  INTEGER,
    fico_range_high INTEGER
);

INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126382135, 40000, 40000, '60 months', '14.08%', 'C', 'C3', 'Owner/ Financial Professional', 'OWN', 'Dec-2023',
        'Current', '152xx', 'PA', 660, 664);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126355295, 30000, 40000, '60 months', '14.08%', 'C', 'C3', 'Sr. Lead Transport', 'MORTGAGE', 'Dec-2022',
        'Charged Off', '945xx', 'CA', 715, 719);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (125380890, 20000, 40000, ' 36 months', '9.93%', 'B', 'B2', 'owner ', 'RENT', 'Dec-2021', 'Current', '980xx',
        'WA', 685, 689);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126417358, 20000, 40000, ' 36 months', '7.35%', 'D', 'D4', 'Senior Java Developer', 'MORTGAGE', 'Dec-2020',
        'Current', '553xx', 'MO', 705, 709);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126380861, 10000, 40000, '36 months', '6.08%', 'A', 'A2', 'Engineer', 'MORTGAGE', 'Dec-2019', 'Fully Paid',
        '750xx', 'TX', 750, 754);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (125467821, 9000, 40000, ' 36 months', '6.08%', 'A', 'A2', 'Editor/Writer', 'MORTGAGE', 'Dec-2018', 'Current',
        '200xx', 'DC', 770, 774);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126285300, 8000, 40000, ' 36 months', '6.08%', 'A', 'A2', 'Loss Mitigation Manager', 'MORTGAGE', 'Dec-2017',
        'Fully Paid', '928xx', 'CA', 780, 784);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (115384090, 7000, 40000, ' 60 months', '12.62%', 'C', 'C1', 'Auto body tech', 'OWN', 'Dec-2016', 'Fully Paid',
        '347xx', 'FL', 690, 694);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (126212457, 6000, 40000, ' 36 months', '5.32%', 'A', 'A1', 'Ammunition Manager', 'MORTGAGE', 'Dec-2015',
        'Current', '920xx', 'CA', 725, 729);
INSERT INTO loan_data (id, loan_amount, funded_amount, term, interest_rate, grade, sub_grade, employee_title,
                       home_ownership, issued_date, loan_status, zip_code, state_address, fico_range_low,
                       fico_range_high)
VALUES (125519315, 5000, 40000, ' 36 months', '6.72%', 'B', 'B3', 'Truck driver ', 'MORTGAGE', 'Dec-2014', 'Current',
        '191xx', 'PA', 680, 684);

