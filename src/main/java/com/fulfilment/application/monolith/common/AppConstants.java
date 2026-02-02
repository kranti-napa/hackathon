package com.fulfilment.application.monolith.common;

public final class AppConstants {

    private AppConstants() {}

    public static final String ERR_WAREHOUSE_NULL = "Warehouse must not be null";
    public static final String ERR_STORE_NULL = "Store request must not be null";

    public static final String ERR_WAREHOUSE_NOT_FOUND =
            "Warehouse not found with business unit: %s";

    public static final String ERR_STORE_NOT_FOUND =
            "Store not found with id: %s";

    public static final String ERR_STORE_ID_REQUIRED =
            "Store id must be provided";

    public static final String ERR_STORE_ID_NOT_ALLOWED =
            "Store id must be null for create";

    public static final String ERR_STORE_ID_MISMATCH =
            "Store id in path and body must match";

    public static final String ERR_PRODUCT_NULL =
            "Product request must not be null";

    public static final String ERR_PRODUCT_ID_REQUIRED =
            "Product id must be provided";

    public static final String ERR_PRODUCT_ID_NOT_ALLOWED =
            "Product id must be null for create";

    public static final String ERR_PRODUCT_ID_MISMATCH =
            "Product id in path and body must match";

    public static final String ERR_PRODUCT_NAME_REQUIRED =
            "Product name must be provided";

    public static final String ERR_PRODUCT_NOT_FOUND =
            "Product not found with id: %s";

    public static final String ERR_WAREHOUSE_ALREADY_EXISTS =
            "Warehouse with business unit code already exists";

    public static final String ERR_WAREHOUSE_MAX_PER_LOCATION =
            "Maximum number of warehouses reached for location";

    public static final String ERR_WAREHOUSE_INVALID_CAPACITY =
            "Invalid warehouse capacity";

    public static final String ERR_WAREHOUSE_INVALID_STOCK =
            "Invalid warehouse stock";

    public static final String ERR_WAREHOUSE_INVALID_ARCHIVE =
            "Invalid warehouse for archiving";

    public static final String ERR_WAREHOUSE_STOCK_IMMUTABLE =
            "Stock must remain unchanged when replacing a warehouse";

    public static final String ERR_WAREHOUSE_CAPACITY_REQUIRED =
            "Capacity must be greater than zero";

    public static final String ERR_WAREHOUSE_CAPACITY_INSUFFICIENT =
            "Warehouse capacity must be greater than or equal to stock";

    public static final String ERR_LOCATION_NOT_FOUND =
            "No location found for identifier: %s";

    public static final String ERR_ASSIGN_MAX_WAREHOUSES_PER_PRODUCT =
            "Product can be fulfilled by max 2 warehouses per store";

    public static final String ERR_ASSIGN_MAX_WAREHOUSES_PER_STORE =
            "Store can be fulfilled by max 3 warehouses";

    public static final String ERR_ASSIGN_MAX_PRODUCTS_PER_WAREHOUSE =
            "Warehouse can store max 5 products";

    public static final String ERR_FULFILMENT_REQUEST_NULL =
            "Fulfilment request must not be null";

    public static final String ERR_INTERNAL_SERVER =
            "Internal Server Error";
}
