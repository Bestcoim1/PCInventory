-- FICTIONAL DEMONSTRATION DATA ONLY. No real inventory or product specs.
-- Intended for a CLEAN schema_v1.sql installation. Do not use as production data.
USE pc_inventory;

INSERT INTO component_category (id,category_name) VALUES
 (1,'CPU'),(2,'Mainboard'),(3,'RAM'),(4,'Storage'),(5,'GPU'),
 (6,'PSU'),(7,'CPU Cooler'),(8,'Case'),(9,'Case Fan');
INSERT INTO brand (id,brand_name) VALUES (1,'Demo Parts');

INSERT INTO component
  (id, component_code, component_name, component_type, category_id, brand_id, current_unit_price)
VALUES
 (1,'DEMO-CPU-AM5','Demo CPU AM5','CPU',1,1,3500000),
 (2,'DEMO-CPU-LGA','Demo CPU LGA1700','CPU',1,1,3000000),
 (3,'DEMO-MB-AM5','Demo Mainboard AM5 DDR5','MAINBOARD',2,1,2600000),
 (4,'DEMO-MB-LGA','Demo Mainboard LGA1700 DDR4','MAINBOARD',2,1,2200000),
 (5,'DEMO-RAM-DDR5','Demo RAM DDR5 5600 16GB','RAM',3,1,1100000),
 (6,'DEMO-RAM-DDR4','Demo RAM DDR4 3200 16GB','RAM',3,1,800000),
 (7,'DEMO-SSD-SATA','Demo SSD SATA 2.5 512GB','STORAGE',4,1,950000),
 (8,'DEMO-GPU','Demo GPU 250mm','GPU',5,1,5000000),
 (9,'DEMO-PSU','Demo PSU ATX 650W','PSU',6,1,1400000),
 (10,'DEMO-COOLER','Demo AIR Cooler 155mm','CPU_COOLER',7,1,600000),
 (11,'DEMO-CASE','Demo Case Micro ATX','CASE',8,1,900000),
 (12,'DEMO-FAN','Demo Case Fan 120mm','CASE_FAN',9,1,130000);

INSERT INTO cpu_spec (component_id,socket,has_integrated_graphics,includes_stock_cooler) VALUES
 (1,'AM5',1,1),(2,'LGA1700',0,0);
INSERT INTO mainboard_spec
 (component_id,socket,supported_ram_type,max_ram_capacity_gb,form_factor,sata_port_count,m2_slot_count)
 VALUES
 (3,'AM5','DDR5',64,'MICRO_ATX',4,2),
 (4,'LGA1700','DDR4',64,'MICRO_ATX',4,2);
INSERT INTO mainboard_ram_bus (mainboard_id,bus_mhz) VALUES
 (3,5200),(3,5600),(3,6000),(4,2666),(4,3200);
INSERT INTO ram_spec (component_id,ram_type,bus_mhz,capacity_gb) VALUES
 (5,'DDR5',5600,16),(6,'DDR4',3200,16);
INSERT INTO storage_spec (component_id,storage_type,capacity_gb,interface_type,form_factor) VALUES
 (7,'SSD',512,'SATA','2_5');
INSERT INTO gpu_spec (component_id,length_mm,minimum_psu_wattage) VALUES (8,250,600);
INSERT INTO psu_spec (component_id,real_wattage,psu_form_factor) VALUES (9,650,'ATX');
INSERT INTO cpu_cooler_spec (component_id,cooling_type,height_mm,radiator_size) VALUES
 (10,'AIR',155,'NONE');
INSERT INTO cooler_supported_socket (cooler_id,socket) VALUES
 (10,'AM5'),(10,'LGA1700');
INSERT INTO pc_case_spec
 (component_id,max_cpu_cooler_height_mm,max_gpu_length_mm,max_case_fan_count,
  included_case_fan_count,drive_bay_25_count,drive_bay_35_count,max_radiator_size)
 VALUES (11,165,330,5,1,2,1,'MM240');
INSERT INTO case_supported_mb_form (case_id,form_factor) VALUES (11,'MICRO_ATX');
INSERT INTO case_supported_psu_form (case_id,form_factor) VALUES (11,'ATX');
INSERT INTO case_supported_fan_size (case_id,size_mm) VALUES (11,120);
INSERT INTO case_fan_spec (component_id,size_mm) VALUES (12,120);

-- StockBalance is initialized at zero on component creation. Seed quantities for demo only.
INSERT INTO stock_balance (component_id,quantity_on_hand) VALUES
 (1,10),(2,10),(3,10),(4,10),(5,10),(6,10),
 (7,10),(8,10),(9,10),(10,10),(11,10),(12,10);

INSERT INTO supplier (id,supplier_name,phone) VALUES
 (1,'Demo Supplier','0000000000');

-- IMPORTANT: No user_account is created here: generate real password_hash
-- with the application's approved hash algorithm before testing UC01.
-- No stock_movement rows are fabricated for setup quantities.
-- For reconciliation tests, start with zero stock then confirm an import receipt
-- instead of using stock values seeded directly.
