package com.super_spring.inventory_service.config;

public final class ApplicationConstants {

    public static final String PRODUCTS_PATH = "/products";
    public static final String PRODUCT_ID_PATH = "/{id}";
    public static final String EXPORT_PATH = "/export";
    public static final String EXPORT_FILE_PATH = "/export/{fileName}";
    public static final String IMPORT_PATH = "/import";
    public static final String ACTUATOR_PATH = "/actuator/**";
    public static final String INVENTORY_PATH = "/inventory";
    public static final String INVENTORY_BATCH_PATH = "/inventory/batch";
    public static final String INVENTORY_PRODUCT_PATH = "/inventory/{productId}";
    public static final String TOTAL_COUNT_HEADER = "X-Total-Count";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String ATTACHMENT_CONTENT_DISPOSITION = "attachment; filename=\"";
    public static final String FILE_REQUEST_PARAM = "file";
    public static final String CSV_MEDIA_TYPE = "text/csv";
    public static final String CSV_FILE_SUFFIX = ".csv";
    public static final String OWNER_FILE_SUFFIX = ".owner";
    public static final String EXPORT_FILE_PREFIX = "export-";
    public static final String CSV_FILE_NAME_REGEX = "export-[0-9a-f-]+\\.csv";
    public static final String CSV_HEADER =
            "id,categoryId,name,description,price,tax,uploadingDate,imageUrls,brand,attributes,rating,totalReviews,active,stock";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String MANAGER_ROLE = "MANAGER";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String KEYCLOAK_REALM_ACCESS_CLAIM = "realm_access";
    public static final String KEYCLOAK_ROLES_CLAIM = "roles";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    public static final String EXPORT_OPERATION = "export";
    public static final String IMPORT_OPERATION = "import";
    public static final int DEFAULT_IMPORT_BATCH_SIZE = 200;
    public static final String COMPLETED_STATUS = "COMPLETED";
    public static final String FAILED_STATUS = "FAILED";
    public static final String METRIC_OPERATION_TAG = "operation";
    public static final String COMPLETED_METRIC = "product.import_export.completed";
    public static final String FAILED_METRIC = "product.import_export.failed";
    public static final String DURATION_METRIC = "product.import_export.duration";
    public static final String PRODUCT_OPERATIONS_METRIC = "product.operations";
    public static final String INVENTORY_CLIENT_REQUESTS_METRIC = "product.inventory.client.requests";
    public static final String INVENTORY_CLIENT_DURATION_METRIC = "product.inventory.client.duration";
    public static final String INVENTORY_EVENTS_METRIC = "product.inventory.events";
    public static final String NOTIFICATION_METRIC = "product.notifications";
    public static final String BATCH_SIZE_METRIC = "product.import.batch.size";
    public static final String RESULT_TAG = "result";
    public static final String SUCCESS_RESULT = "success";
    public static final String FAILURE_RESULT = "failure";
    public static final String EVENT_TAG = "event";
    public static final String EVENT_INVENTORY_UPDATED = "inventory.updated";
    public static final String METHOD_TAG = "method";
    public static final String STATUS_TAG = "status";
    public static final String OPERATION_DURATION_DESCRIPTION =
            "Duration of product import and export operations";
    public static final String EXPORT_TASK_EXECUTOR = "exportTaskExecutor";
    public static final String IMPORT_TASK_EXECUTOR = "importTaskExecutor";
    public static final String EXPORT_THREAD_PREFIX = "Export-";
    public static final String IMPORT_THREAD_PREFIX = "Import-";
    public static final String DEFAULT_OFFSET = "0";
    public static final String DEFAULT_LIMIT = "10";

    public static final String PRODUCT_MUST_NOT_BE_NULL = "Product must not be null";
    public static final String FILE_MUST_NOT_BE_NULL_OR_EMPTY = "File must not be null or empty";
    public static final String PUBLISHED_NOTIFICATION_LOG =
            "Published {} completion notification with status {} to {}";
    public static final String FAILED_NOTIFICATION_LOG =
            "Failed to publish {} completion notification to {}";
    public static final String UNABLE_TO_READ_EXPORT_LOG = "Unable to read export {}";
    public static final String EXPORT_FAILED_LOG = "Product export {} failed";
    public static final String IMPORT_FAILED_LOG = "Product import {} failed";
    public static final String UNABLE_TO_CLEAN_EXPORT_DIRECTORY_LOG =
            "Unable to clean export directory {}";
    public static final String UNABLE_TO_DELETE_LOG = "Unable to delete {}";

    public static final String PRODUCT_PAGINATION_QUERY =
            "SELECT * FROM products p ORDER BY p.id LIMIT :limit OFFSET :offset";

    private ApplicationConstants() {
    }
}