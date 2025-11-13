-- Sample inventory data
INSERT INTO inventory (product_id, available_stock) VALUES
    (1, 100),
    (2, 50),
    (3, 200),
    (4, 150),
    (5, 75)
ON CONFLICT (product_id) DO NOTHING;