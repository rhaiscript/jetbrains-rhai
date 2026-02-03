//! Utility functions module
//! Provides common array and string operations

/// Double each element in an array
///
/// # Arguments
/// * `arr` - Array of numbers
///
/// # Returns
/// Array with doubled values
pub fn map_double(arr) {
    arr.map(|x| x * 2)
}

/// Triple each element in an array
pub fn map_triple(arr) {
    arr.map(|x| x * 3)
}

/// Sum all elements in an array
///
/// # Arguments
/// * `arr` - Array of numbers
///
/// # Returns
/// Sum of all elements
pub fn sum_array(arr) {
    arr.reduce(0, |acc, x| acc + x)
}

/// Find the product of all elements
pub fn product_array(arr) {
    if len(arr) == 0 {
        return 0;
    }
    arr.reduce(1, |acc, x| acc * x)
}

/// Filter positive numbers
pub fn filter_positive(arr) {
    arr.filter(|x| x > 0)
}

/// Filter negative numbers
pub fn filter_negative(arr) {
    arr.filter(|x| x < 0)
}

/// Check if array contains a value
pub fn contains_value(arr, value) {
    arr.some(|x| x == value)
}

/// Count occurrences of a value
pub fn count_occurrences(arr, value) {
    arr.filter(|x| x == value).len()
}

/// Remove duplicates from array
pub fn unique(arr) {
    let result = [];
    for item in arr {
        if !result.contains(item) {
            result.push(item);
        }
    }
    result
}

/// Reverse an array (returns new array)
pub fn reverse_copy(arr) {
    let result = [];
    let i = len(arr) - 1;
    while i >= 0 {
        result.push(arr[i]);
        i -= 1;
    }
    result
}

/// Flatten nested arrays (one level)
pub fn flatten(arr) {
    let result = [];
    for item in arr {
        if type_of(item) == "array" {
            for sub_item in item {
                result.push(sub_item);
            }
        } else {
            result.push(item);
        }
    }
    result
}

/// Zip two arrays together
pub fn zip(arr1, arr2) {
    let result = [];
    let min_len = if len(arr1) < len(arr2) { len(arr1) } else { len(arr2) };

    for i in 0..min_len {
        result.push([arr1[i], arr2[i]]);
    }
    result
}

/// Take first n elements
pub fn take(arr, n) {
    let result = [];
    let count = if n < len(arr) { n } else { len(arr) };

    for i in 0..count {
        result.push(arr[i]);
    }
    result
}

/// Skip first n elements
pub fn skip(arr, n) {
    let result = [];
    let start = if n < len(arr) { n } else { len(arr) };

    for i in start..len(arr) {
        result.push(arr[i]);
    }
    result
}

/// String utilities

/// Repeat a string n times
pub fn repeat_string(s, n) {
    let result = "";
    for _ in 0..n {
        result += s;
    }
    result
}

/// Pad string to the left
pub fn pad_left(s, length, char) {
    let padding = length - len(s);
    if padding <= 0 {
        return s;
    }
    repeat_string(char, padding) + s
}

/// Pad string to the right
pub fn pad_right(s, length, char) {
    let padding = length - len(s);
    if padding <= 0 {
        return s;
    }
    s + repeat_string(char, padding)
}

/// Check if string is empty or whitespace
pub fn is_blank(s) {
    s.trim().len() == 0
}

/// Capitalize first letter
pub fn capitalize(s) {
    if len(s) == 0 {
        return s;
    }
    s[0].to_upper() + s.sub_string(1, len(s) - 1)
}
