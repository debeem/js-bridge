// src/utils.js
export function serializable(obj) {
    return JSON.parse(JSON.stringify(obj, (key, value) =>
    typeof value === 'bigint'
    ? value.toString()
    : value
    ));
}