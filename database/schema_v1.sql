-- PC Inventory & Configuration Management System
-- Database design v1 (DRAFT / CHO NHOM RA SOAT)
-- Derived from baseline v0.10 (03/11/2026); MySQL >= 8.0.16, InnoDB.
-- Run on an EMPTY database; this script does not DROP tables or destroy data.
-- All application transactions are handled by Java Service/DAO via JDBC.
-- This schema DOES NOT create Supabase, APIs, orders, customers, or payments.

CREATE DATABASE IF NOT EXISTS pc_inventory
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pc_inventory;

CREATE TABLE user_account (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(60) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  role VARCHAR(10) NOT NULL DEFAULT 'EMPLOYEE',
  account_status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_username (username),
  CONSTRAINT chk_user_role CHECK (role IN ('EMPLOYEE','ADMIN')),
  CONSTRAINT chk_user_status CHECK (account_status IN ('ACTIVE','LOCKED'))
) ENGINE=InnoDB;

CREATE TABLE component_category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  PRIMARY KEY (id),
  UNIQUE KEY uq_category_name (category_name)
) ENGINE=InnoDB;

CREATE TABLE brand (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  brand_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  PRIMARY KEY (id),
  UNIQUE KEY uq_brand_name (brand_name)
) ENGINE=InnoDB;

CREATE TABLE component (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  component_code VARCHAR(50) NOT NULL,
  component_name VARCHAR(200) NOT NULL,
  component_type VARCHAR(20) NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  brand_id BIGINT UNSIGNED NOT NULL,
  current_unit_price DECIMAL(15,2) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_component_code (component_code),
  UNIQUE KEY uq_component_id_type (id, component_type),
  KEY idx_component_type_active (component_type, is_active),
  KEY idx_component_name (component_name),
  CONSTRAINT fk_component_category FOREIGN KEY (category_id)
    REFERENCES component_category(id) ON DELETE RESTRICT,
  CONSTRAINT fk_component_brand FOREIGN KEY (brand_id)
    REFERENCES brand(id) ON DELETE RESTRICT,
  CONSTRAINT chk_component_type CHECK (component_type IN
    ('CPU','MAINBOARD','RAM','STORAGE','GPU','PSU','CPU_COOLER','CASE','CASE_FAN')),
  CONSTRAINT chk_component_price CHECK (current_unit_price >= 0),
  CONSTRAINT chk_component_active CHECK (is_active IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE cpu_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'CPU',
  socket VARCHAR(40) NOT NULL,
  has_integrated_graphics BOOLEAN NOT NULL,
  includes_stock_cooler BOOLEAN NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_cpu_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_cpu_spec_type CHECK (component_type = 'CPU'),
  CONSTRAINT chk_cpu_boolean CHECK (has_integrated_graphics IN (0,1) AND includes_stock_cooler IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE mainboard_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'MAINBOARD',
  socket VARCHAR(40) NOT NULL,
  supported_ram_type VARCHAR(16) NOT NULL,
  max_ram_capacity_gb INT UNSIGNED NOT NULL,
  form_factor VARCHAR(40) NOT NULL,
  sata_port_count INT UNSIGNED NOT NULL,
  m2_slot_count INT UNSIGNED NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_mainboard_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_mainboard_spec_type CHECK (component_type = 'MAINBOARD'),
  CONSTRAINT chk_mb_ram_capacity CHECK (max_ram_capacity_gb > 0)
) ENGINE=InnoDB;

CREATE TABLE ram_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'RAM',
  ram_type VARCHAR(16) NOT NULL,
  bus_mhz INT UNSIGNED NOT NULL,
  capacity_gb INT UNSIGNED NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_ram_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_ram_spec_type CHECK (component_type = 'RAM'),
  CONSTRAINT chk_ram_capacity CHECK (capacity_gb > 0),
  CONSTRAINT chk_ram_bus CHECK (bus_mhz > 0)
) ENGINE=InnoDB;

CREATE TABLE storage_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'STORAGE',
  storage_type VARCHAR(8) NOT NULL,
  capacity_gb INT UNSIGNED NOT NULL,
  interface_type VARCHAR(12) NOT NULL,
  form_factor VARCHAR(8) NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_storage_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_storage_spec_type CHECK (component_type = 'STORAGE'),
  CONSTRAINT chk_storage_type CHECK (storage_type IN ('SSD','HDD')),
  CONSTRAINT chk_storage_interface CHECK (interface_type IN ('SATA','M2_NVME')),
  CONSTRAINT chk_storage_form CHECK (form_factor IN ('2_5','3_5','M_2')),
  CONSTRAINT chk_storage_capacity CHECK (capacity_gb > 0),
  CONSTRAINT chk_storage_pair CHECK (
    (interface_type = 'SATA' AND form_factor IN ('2_5','3_5')) OR
    (interface_type = 'M2_NVME' AND form_factor = 'M_2'))
) ENGINE=InnoDB;

CREATE TABLE gpu_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'GPU',
  length_mm INT UNSIGNED NOT NULL,
  minimum_psu_wattage INT UNSIGNED NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_gpu_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_gpu_spec_type CHECK (component_type = 'GPU'),
  CONSTRAINT chk_gpu_length CHECK (length_mm > 0),
  CONSTRAINT chk_gpu_psu CHECK (minimum_psu_wattage > 0)
) ENGINE=InnoDB;

CREATE TABLE psu_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'PSU',
  real_wattage INT UNSIGNED NOT NULL,
  psu_form_factor VARCHAR(40) NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_psu_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_psu_spec_type CHECK (component_type = 'PSU'),
  CONSTRAINT chk_psu_wattage CHECK (real_wattage > 0)
) ENGINE=InnoDB;

CREATE TABLE cpu_cooler_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'CPU_COOLER',
  cooling_type VARCHAR(8) NOT NULL,
  height_mm INT UNSIGNED NULL,
  radiator_size VARCHAR(8) NOT NULL DEFAULT 'NONE' ,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_cpu_cooler_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_cpu_cooler_spec_type CHECK (component_type = 'CPU_COOLER'),
  CONSTRAINT chk_cooler_type CHECK (cooling_type IN ('AIR','LIQUID')),
  CONSTRAINT chk_cooler_rad CHECK (radiator_size IN ('NONE','MM240','MM360')),
  CONSTRAINT chk_cooler_shape CHECK (
    (cooling_type = 'AIR' AND height_mm IS NOT NULL AND height_mm > 0 AND radiator_size = 'NONE') OR
    (cooling_type = 'LIQUID' AND height_mm IS NULL AND radiator_size IN ('MM240','MM360')))
) ENGINE=InnoDB;

CREATE TABLE pc_case_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'CASE',
  max_cpu_cooler_height_mm INT UNSIGNED NOT NULL,
  max_gpu_length_mm INT UNSIGNED NOT NULL,
  max_case_fan_count INT UNSIGNED NOT NULL,
  included_case_fan_count INT UNSIGNED NOT NULL DEFAULT 0,
  drive_bay_25_count INT UNSIGNED NOT NULL DEFAULT 0,
  drive_bay_35_count INT UNSIGNED NOT NULL DEFAULT 0,
  max_radiator_size VARCHAR(8) NOT NULL DEFAULT 'NONE' ,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_pc_case_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_pc_case_spec_type CHECK (component_type = 'CASE'),
  CONSTRAINT chk_case_radiator CHECK (max_radiator_size IN ('NONE','MM240','MM360')),
  CONSTRAINT chk_case_gpu_height CHECK (max_cpu_cooler_height_mm > 0 AND max_gpu_length_mm > 0),
  CONSTRAINT chk_case_fan_counts CHECK (included_case_fan_count <= max_case_fan_count)
) ENGINE=InnoDB;

CREATE TABLE case_fan_spec (
  component_id BIGINT UNSIGNED NOT NULL,
  component_type VARCHAR(20) NOT NULL DEFAULT 'CASE_FAN',
  size_mm INT UNSIGNED NOT NULL,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_case_fan_spec_component FOREIGN KEY (component_id, component_type)
    REFERENCES component(id, component_type) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT chk_case_fan_spec_type CHECK (component_type = 'CASE_FAN'),
  CONSTRAINT chk_casefan_size CHECK (size_mm IN (120,140))
) ENGINE=InnoDB;

CREATE TABLE mainboard_ram_bus (
  mainboard_id BIGINT UNSIGNED NOT NULL,
  bus_mhz INT UNSIGNED NOT NULL,
  PRIMARY KEY (mainboard_id,bus_mhz),
  CONSTRAINT fk_mb_bus_mb FOREIGN KEY (mainboard_id)
    REFERENCES mainboard_spec(component_id) ON DELETE CASCADE,
  CONSTRAINT chk_mb_bus_positive CHECK (bus_mhz > 0)
) ENGINE=InnoDB;

CREATE TABLE cooler_supported_socket (
  cooler_id BIGINT UNSIGNED NOT NULL,
  socket VARCHAR(40) NOT NULL,
  PRIMARY KEY (cooler_id,socket),
  CONSTRAINT fk_cooler_socket FOREIGN KEY (cooler_id)
    REFERENCES cpu_cooler_spec(component_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE case_supported_mb_form (
  case_id BIGINT UNSIGNED NOT NULL,
  form_factor VARCHAR(40) NOT NULL,
  PRIMARY KEY (case_id,form_factor),
  CONSTRAINT fk_case_mb_form FOREIGN KEY (case_id)
    REFERENCES pc_case_spec(component_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE case_supported_psu_form (
  case_id BIGINT UNSIGNED NOT NULL,
  form_factor VARCHAR(40) NOT NULL,
  PRIMARY KEY (case_id,form_factor),
  CONSTRAINT fk_case_psu_form FOREIGN KEY (case_id)
    REFERENCES pc_case_spec(component_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE case_supported_fan_size (
  case_id BIGINT UNSIGNED NOT NULL,
  size_mm INT UNSIGNED NOT NULL,
  PRIMARY KEY (case_id,size_mm),
  CONSTRAINT fk_case_fan_size FOREIGN KEY (case_id)
    REFERENCES pc_case_spec(component_id) ON DELETE CASCADE,
  CONSTRAINT chk_case_fan_size CHECK (size_mm IN (120,140))
) ENGINE=InnoDB;

CREATE TABLE supplier (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  supplier_name VARCHAR(160) NOT NULL,
  address VARCHAR(300),
  phone VARCHAR(30),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_supplier_name (supplier_name)
) ENGINE=InnoDB;

CREATE TABLE import_receipt (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  receipt_code VARCHAR(50) NOT NULL,
  supplier_id BIGINT UNSIGNED NOT NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
  total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_by BIGINT UNSIGNED NULL,
  confirmed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_receipt_code (receipt_code),
  KEY idx_receipt_status_date (status,created_at),
  CONSTRAINT fk_receipt_supplier FOREIGN KEY (supplier_id)
    REFERENCES supplier(id) ON DELETE RESTRICT,
  CONSTRAINT fk_receipt_creator FOREIGN KEY (created_by)
    REFERENCES user_account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_receipt_confirmer FOREIGN KEY (confirmed_by)
    REFERENCES user_account(id) ON DELETE RESTRICT,
  CONSTRAINT chk_receipt_status CHECK (status IN ('PENDING','CONFIRMED')),
  CONSTRAINT chk_receipt_amount CHECK (total_amount >= 0),
  CONSTRAINT chk_receipt_confirm CHECK (
    (status='PENDING' AND confirmed_by IS NULL AND confirmed_at IS NULL) OR
    (status='CONFIRMED' AND confirmed_by IS NOT NULL AND confirmed_at IS NOT NULL))
) ENGINE=InnoDB;

CREATE TABLE import_receipt_line (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  receipt_id BIGINT UNSIGNED NOT NULL,
  component_id BIGINT UNSIGNED NOT NULL,
  quantity INT UNSIGNED NOT NULL,
  purchase_unit_price DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_receipt_line_receipt (receipt_id),
  CONSTRAINT fk_receipt_line_receipt FOREIGN KEY (receipt_id)
    REFERENCES import_receipt(id) ON DELETE CASCADE,
  CONSTRAINT fk_receipt_line_component FOREIGN KEY (component_id)
    REFERENCES component(id) ON DELETE RESTRICT,
  CONSTRAINT chk_receipt_line_qty CHECK (quantity > 0),
  CONSTRAINT chk_receipt_line_price CHECK (purchase_unit_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE stock_balance (
  component_id BIGINT UNSIGNED NOT NULL,
  quantity_on_hand INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (component_id),
  CONSTRAINT fk_balance_component FOREIGN KEY (component_id)
    REFERENCES component(id) ON DELETE RESTRICT,
  CONSTRAINT chk_balance_nonnegative CHECK (quantity_on_hand >= 0)
) ENGINE=InnoDB;

CREATE TABLE pc_configuration (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  configuration_code VARCHAR(50) NOT NULL,
  configuration_name VARCHAR(180) NOT NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  saved_total_price DECIMAL(15,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_configuration_code (configuration_code),
  KEY idx_configuration_name (configuration_name),
  CONSTRAINT fk_configuration_creator FOREIGN KEY (created_by)
    REFERENCES user_account(id) ON DELETE RESTRICT,
  CONSTRAINT chk_config_price CHECK (saved_total_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE configuration_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  configuration_id BIGINT UNSIGNED NOT NULL,
  component_id BIGINT UNSIGNED NOT NULL,
  quantity INT UNSIGNED NOT NULL,
  saved_unit_price DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_config_item_conf (configuration_id),
  KEY idx_config_item_component (component_id),
  CONSTRAINT fk_config_item_config FOREIGN KEY (configuration_id)
    REFERENCES pc_configuration(id) ON DELETE CASCADE,
  CONSTRAINT fk_config_item_component FOREIGN KEY (component_id)
    REFERENCES component(id) ON DELETE RESTRICT,
  CONSTRAINT chk_config_item_qty CHECK (quantity > 0),
  CONSTRAINT chk_config_item_price CHECK (saved_unit_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE assembled_pc (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  pc_code VARCHAR(50) NOT NULL,
  pc_name VARCHAR(180) NOT NULL,
  source_configuration_id BIGINT UNSIGNED NOT NULL,
  assembled_by BIGINT UNSIGNED NOT NULL,
  assembled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(12) NOT NULL DEFAULT 'AVAILABLE',
  assembly_total_price DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pc_code (pc_code),
  KEY idx_pc_source_conf (source_configuration_id),
  KEY idx_pc_status (status),
  CONSTRAINT fk_pc_source_conf FOREIGN KEY (source_configuration_id)
    REFERENCES pc_configuration(id) ON DELETE RESTRICT,
  CONSTRAINT fk_pc_assembler FOREIGN KEY (assembled_by)
    REFERENCES user_account(id) ON DELETE RESTRICT,
  CONSTRAINT chk_pc_status CHECK (status IN ('AVAILABLE','INACTIVE')),
  CONSTRAINT chk_pc_price CHECK (assembly_total_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE assembled_pc_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  assembled_pc_id BIGINT UNSIGNED NOT NULL,
  component_id BIGINT UNSIGNED NOT NULL,
  component_code_snapshot VARCHAR(50) NOT NULL,
  component_name_snapshot VARCHAR(200) NOT NULL,
  quantity INT UNSIGNED NOT NULL,
  assembly_unit_price DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_pc_item_pc (assembled_pc_id),
  CONSTRAINT fk_pc_item_pc FOREIGN KEY (assembled_pc_id)
    REFERENCES assembled_pc(id) ON DELETE RESTRICT,
  CONSTRAINT fk_pc_item_component FOREIGN KEY (component_id)
    REFERENCES component(id) ON DELETE RESTRICT,
  CONSTRAINT chk_pc_item_qty CHECK (quantity > 0),
  CONSTRAINT chk_pc_item_price CHECK (assembly_unit_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE stock_movement (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  component_id BIGINT UNSIGNED NOT NULL,
  quantity_delta INT NOT NULL,
  movement_type VARCHAR(4) NOT NULL,
  source_receipt_id BIGINT UNSIGNED NULL,
  source_assembled_pc_id BIGINT UNSIGNED NULL,
  performed_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_movement_component_time (component_id,created_at),
  KEY idx_movement_receipt (source_receipt_id),
  KEY idx_movement_pc (source_assembled_pc_id),
  CONSTRAINT fk_movement_component FOREIGN KEY (component_id)
    REFERENCES component(id) ON DELETE RESTRICT,
  CONSTRAINT fk_movement_receipt FOREIGN KEY (source_receipt_id)
    REFERENCES import_receipt(id) ON DELETE RESTRICT,
  CONSTRAINT fk_movement_pc FOREIGN KEY (source_assembled_pc_id)
    REFERENCES assembled_pc(id) ON DELETE RESTRICT,
  CONSTRAINT fk_movement_user FOREIGN KEY (performed_by)
    REFERENCES user_account(id) ON DELETE RESTRICT,
  CONSTRAINT chk_movement_origin CHECK (
    (movement_type='IN' AND quantity_delta > 0 AND source_receipt_id IS NOT NULL AND source_assembled_pc_id IS NULL) OR
    (movement_type='OUT' AND quantity_delta < 0 AND source_receipt_id IS NULL AND source_assembled_pc_id IS NOT NULL))
) ENGINE=InnoDB;

-- End of schema.
