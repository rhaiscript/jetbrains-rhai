//! Mathematical functions module
//! Provides statistical and mathematical operations

/// Mathematical constants
const PI = 3.14159265358979;
const E = 2.71828182845904;
const PHI = 1.61803398874989; // Golden ratio

/// Calculate the average of an array
///
/// # Arguments
/// * `arr` - Array of numbers
///
/// # Returns
/// Average value or 0 for empty array
pub fn average(arr) {
    if len(arr) == 0 {
        return 0.0;
    }

    let sum = arr.reduce(0, |acc, x| acc + x);
    sum / len(arr)
}

/// Calculate the minimum value
pub fn min(arr) {
    if len(arr) == 0 {
        throw "Cannot find min of empty array";
    }

    let result = arr[0];
    for item in arr {
        if item < result {
            result = item;
        }
    }
    result
}

/// Calculate the maximum value
pub fn max(arr) {
    if len(arr) == 0 {
        throw "Cannot find max of empty array";
    }

    let result = arr[0];
    for item in arr {
        if item > result {
            result = item;
        }
    }
    result
}

/// Calculate statistics for an array
///
/// # Returns
/// Object with min, max, sum, average, count
pub fn statistics(arr) {
    if len(arr) == 0 {
        return #{
            min: 0,
            max: 0,
            sum: 0,
            average: 0.0,
            count: 0
        };
    }

    let min_val = arr[0];
    let max_val = arr[0];
    let sum = 0;

    for item in arr {
        if item < min_val { min_val = item; }
        if item > max_val { max_val = item; }
        sum += item;
    }

    #{
        min: min_val,
        max: max_val,
        sum: sum,
        average: sum / len(arr),
        count: len(arr)
    }
}

/// Calculate variance
pub fn variance(arr) {
    if len(arr) < 2 {
        return 0.0;
    }

    let avg = average(arr);
    let sum_sq_diff = arr.reduce(0.0, |acc, x| {
        let diff = x - avg;
        acc + diff * diff
    });

    sum_sq_diff / (len(arr) - 1)
}

/// Calculate standard deviation
pub fn std_deviation(arr) {
    sqrt(variance(arr))
}

/// Calculate the median
pub fn median(arr) {
    if len(arr) == 0 {
        throw "Cannot find median of empty array";
    }

    // Sort the array
    let sorted = [];
    for item in arr {
        sorted.push(item);
    }
    sorted.sort();

    let mid = len(sorted) / 2;

    if len(sorted) % 2 == 0 {
        (sorted[mid - 1] + sorted[mid]) / 2.0
    } else {
        sorted[mid]
    }
}

/// Calculate factorial
pub fn factorial(n) {
    if n < 0 {
        throw "Factorial not defined for negative numbers";
    }
    if n <= 1 {
        return 1;
    }

    let result = 1;
    for i in 2..=n {
        result *= i;
    }
    result
}

/// Calculate Fibonacci number
pub fn fibonacci(n) {
    if n < 0 {
        throw "Fibonacci not defined for negative numbers";
    }
    if n <= 1 {
        return n;
    }

    let a = 0;
    let b = 1;

    for _ in 2..=n {
        let temp = a + b;
        a = b;
        b = temp;
    }
    b
}

/// Check if number is prime
pub fn is_prime(n) {
    if n < 2 {
        return false;
    }
    if n == 2 {
        return true;
    }
    if n % 2 == 0 {
        return false;
    }

    let i = 3;
    while i * i <= n {
        if n % i == 0 {
            return false;
        }
        i += 2;
    }
    true
}

/// Greatest common divisor (GCD)
pub fn gcd(a, b) {
    let x = abs(a);
    let y = abs(b);

    while y != 0 {
        let temp = y;
        y = x % y;
        x = temp;
    }
    x
}

/// Least common multiple (LCM)
pub fn lcm(a, b) {
    if a == 0 || b == 0 {
        return 0;
    }
    abs(a * b) / gcd(a, b)
}

/// Calculate power (integer exponent)
pub fn power(base, exp) {
    if exp < 0 {
        return 1.0 / power(base, -exp);
    }
    if exp == 0 {
        return 1;
    }

    let result = 1;
    for _ in 0..exp {
        result *= base;
    }
    result
}

/// Clamp value between min and max
pub fn clamp(value, min_val, max_val) {
    if value < min_val {
        min_val
    } else if value > max_val {
        max_val
    } else {
        value
    }
}

/// Linear interpolation
pub fn lerp(a, b, t) {
    a + (b - a) * clamp(t, 0.0, 1.0)
}

/// Map value from one range to another
pub fn map_range(value, in_min, in_max, out_min, out_max) {
    let t = (value - in_min) / (in_max - in_min);
    lerp(out_min, out_max, t)
}

/// Calculate distance between two points
pub fn distance(x1, y1, x2, y2) {
    let dx = x2 - x1;
    let dy = y2 - y1;
    sqrt(dx * dx + dy * dy)
}

/// Convert degrees to radians
pub fn deg_to_rad(deg) {
    deg * PI / 180.0
}

/// Convert radians to degrees
pub fn rad_to_deg(rad) {
    rad * 180.0 / PI
}
