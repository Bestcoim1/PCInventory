-- PC Inventory - Database Design v1 / verification queries
-- READ-ONLY: Does not modify data. Run AFTER schema_v1.sql and optional seed_demo_v1.sql.
-- Requires MySQL Server >= 8.0.16.
USE pc_inventory;

SELECT VERSION() AS mysql_version, DATABASE() AS active_database;

-- Expect 27 tables.
SELECT COUNT(*) AS actual_table_count
FROM information_schema.tables
WHERE table_schema = 'pc_inventory' AND table_type = 'BASE TABLE';

-- Expect 33 distinct foreign-key constraints.
SELECT COUNT(*) AS actual_foreign_key_count
FROM (
    SELECT DISTINCT table_name, constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = 'pc_inventory'
      AND referenced_table_name IS NOT NULL
) AS foreign_keys;

-- CHECK constraints should be enforced.
SELECT constraint_name, table_name, enforced
FROM information_schema.table_constraints
WHERE constraint_schema = 'pc_inventory' AND constraint_type = 'CHECK'
ORDER BY table_name, constraint_name;

-- After seed_demo_v1.sql, expected counts: 9, 1, 12, 12, 1, 0.
-- Without seed, expected values are zero.
SELECT 'component_category' AS table_name, COUNT(*) AS row_count FROM component_category
UNION ALL SELECT 'brand', COUNT(*) FROM brand
UNION ALL SELECT 'component', COUNT(*) FROM component
UNION ALL SELECT 'stock_balance', COUNT(*) FROM stock_balance
UNION ALL SELECT 'supplier', COUNT(*) FROM supplier
UNION ALL SELECT 'stock_movement', COUNT(*) FROM stock_movement;

-- Every component should have exactly one StockBalance (0 missing rows).
SELECT COUNT(*) AS components_missing_stock_balance
FROM component AS c
LEFT JOIN stock_balance AS sb ON sb.component_id = c.id
WHERE sb.component_id IS NULL;

-- Check representative mapping of typed mainboard data and RAM bus set.
SELECT c.component_code, mb.socket, mb.supported_ram_type,
       GROUP_CONCAT(mrb.bus_mhz ORDER BY mrb.bus_mhz SEPARATOR ', ') AS supported_ram_buses
FROM component AS c
JOIN mainboard_spec AS mb ON mb.component_id = c.id
LEFT JOIN mainboard_ram_bus AS mrb ON mrb.mainboard_id = mb.component_id
GROUP BY c.component_code, mb.socket, mb.supported_ram_type
ORDER BY c.component_code;

-- Check snapshot and movement tables begin empty after seed.
SELECT (SELECT COUNT(*) FROM pc_configuration) AS saved_configurations,
       (SELECT COUNT(*) FROM assembled_pc) AS assembled_pcs,
       (SELECT COUNT(*) FROM stock_movement) AS stock_movements;
