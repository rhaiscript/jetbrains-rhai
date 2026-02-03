//! Main entry point for the Rhai sample project
//! Demonstrates multi-file project structure

import "utils" as utils;
import "math" as math;
import "config" as cfg;

/// Application state
let app_state = #{
    name: cfg::APP_NAME,
    version: cfg::VERSION,
    initialized: false,
    counter: 0
};

/// Initialize the application22
fn init() {
    print(`Initializing ${app_state.name} v${app_state.version}...`);

    app_state.initialized = true;
    app_state.counter = 0;

    // Load configuration
    let config = cfg::get_default_config();
    print(`Debug mode: ${config.debug}`);
    print(`Max items: ${config.max_items}`);

    print("Application initialized successfully!");
}

/// Main processing function
fn process(items) {
    if !app_state.initialized {
        throw "Application not initialized!";
    }

    print(`Processing ${len(items)} items...`);

    // Use utility functions
    let filtered = items.filter(|x| x > 0);
    let doubled = utils::map_double(filtered);
    let sum = utils::sum_array(doubled);

    // Use math functions
    let avg = math::average(doubled);
    let stats = math::statistics(doubled);

    print(`Sum: ${sum}`);
    print(`Average: ${avg}`);
    print(`Min: ${stats.min}, Max: ${stats.max}`);

    app_state.counter += 1;

    #{
        processed: len(doubled),
        sum: sum,
        average: avg,
        stats: stats
    }
}

/// Cleanup resources
fn cleanup() {
    print("Cleaning up...");
    app_state.initialized = false;
    app_state.counter = 0;
    print("Cleanup complete.");
}

/// Run the main application
fn main() {
    init();

    // Sample data
    let data = [1, -2, 3, 4, -5, 6, 7, 8, 9, 10];

    // Process multiple batches
    for i in 0..3 {
        print(`\n--- Batch ${i + 1} ---`);
        let result = process(data);
        print(`Batch result: ${result}`);
    }

    print(`\nTotal batches processed: ${app_state.counter}`);

    cleanup();
}

// Run the application
main();
