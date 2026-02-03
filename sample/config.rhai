//! Configuration module
//! Application settings and constants

/// Application name
pub const APP_NAME = "Rhai Sample App";

/// Application version
pub const VERSION = "1.0.0";

/// Build number
pub const BUILD = 42;

/// Environment constants
pub const ENV_DEV = "development";
pub const ENV_PROD = "production";
pub const ENV_TEST = "test";

/// Default configuration
private fn create_default_config() {
    #{
        debug: true,
        verbose: false,
        max_items: 1000,
        timeout_ms: 5000,
        retry_count: 3,
        environment: ENV_DEV,
        features: #{
            logging: true,
            metrics: true,
            cache: false
        },
        paths: #{
            data: "./data",
            logs: "./logs",
            temp: "./temp"
        }
    }
}

/// Get default configuration
pub fn get_default_config() {
    create_default_config()
}

/// Get production configuration
pub fn get_prod_config() {
    let config = create_default_config();

    config.debug = false;
    config.environment = ENV_PROD;
    config.max_items = 10000;
    config.features.cache = true;
    config.paths.data = "/var/data";
    config.paths.logs = "/var/log/app";

    config
}

/// Get test configuration
pub fn get_test_config() {
    let config = create_default_config();

    config.environment = ENV_TEST;
    config.max_items = 100;
    config.timeout_ms = 1000;
    config.features.metrics = false;

    config
}

/// Merge two configurations (second overrides first)
pub fn merge_config(base, override_cfg) {
    let result = #{};

    // Copy base properties
    for key in keys(base) {
        result[key] = base[key];
    }

    // Override with new properties
    for key in keys(override_cfg) {
        if type_of(override_cfg[key]) == "map" && key in base && type_of(base[key]) == "map" {
            // Deep merge for nested objects
            result[key] = merge_config(base[key], override_cfg[key]);
        } else {
            result[key] = override_cfg[key];
        }
    }

    result
}

/// Validate configuration
pub fn validate_config(config) {
    let errors = [];

    if config.max_items <= 0 {
        errors.push("max_items must be positive");
    }

    if config.timeout_ms < 100 {
        errors.push("timeout_ms must be at least 100");
    }

    if config.retry_count < 0 {
        errors.push("retry_count cannot be negative");
    }

    let valid_envs = [ENV_DEV, ENV_PROD, ENV_TEST];
    if !(config.environment in valid_envs) {
        errors.push(`Invalid environment: ${config.environment}`);
    }

    #{
        valid: len(errors) == 0,
        errors: errors
    }
}

/// Log level constants
pub const LOG_DEBUG = 0;
pub const LOG_INFO = 1;
pub const LOG_WARN = 2;
pub const LOG_ERROR = 3;

/// Get log level name
pub fn log_level_name(level) {
    switch level {
        LOG_DEBUG => "DEBUG",
        LOG_INFO => "INFO",
        LOG_WARN => "WARN",
        LOG_ERROR => "ERROR",
        _ => "UNKNOWN"
    }
}

/// Format log message
pub fn format_log(level, message) {
    let level_name = log_level_name(level);
    let timestamp = timestamp();
    `[${timestamp}] [${level_name}] ${message}`
}
