import fs from "node:fs/promises";
import path from "node:path";

const SOURCE_URL = "https://raw.githubusercontent.com/ThangLeQuoc/vietnamese-provinces-database/v3.0.2/json/vn_only_simplified_json_generated_data_vn_units.json";

const OUTPUT_PATH = path.resolve(
    "src/main/resources/static/data/vietnam-addresses.json"
);

const EXPECTED_PROVINCE_COUNT = 34;
const EXPECTED_WARD_COUNT = 3321;

async function fetchJson(url) {
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`Cannot fetch ${url}. Status: ${response.status}`);
    }

    return response.json();
}

function normalizeCode(value, length) {
    return String(value ?? "").trim().padStart(length, "0");
}

function normalizeName(value) {
    return String(value ?? "").trim();
}

function getArray(payload, possibleKeys) {
    if (Array.isArray(payload)) {
        return payload;
    }

    for (const key of possibleKeys) {
        if (payload && Array.isArray(payload[key])) {
            return payload[key];
        }
    }

    return [];
}

function getProvinceCode(province) {
    return normalizeCode(
        province.code
        ?? province.Code
        ?? province.province_code
        ?? province.ProvinceCode
        ?? province.provinceCode
        ?? province.id
        ?? province.Id,
        2
    );
}

function getProvinceName(province) {
    return normalizeName(
        province.full_name
        ?? province.FullName
        ?? province.fullName
        ?? province.name
        ?? province.Name
        ?? province.province_name
        ?? province.ProvinceName
        ?? province.provinceName
    );
}

function getProvinceWards(province) {
    return getArray(province, [
        "wards",
        "Wards",
        "ward",
        "Ward",
        "communes",
        "Communes",
        "children",
        "Children"
    ]);
}

function getWardCode(ward) {
    return normalizeCode(
        ward.code
        ?? ward.Code
        ?? ward.ward_code
        ?? ward.WardCode
        ?? ward.wardCode
        ?? ward.id
        ?? ward.Id,
        5
    );
}

function getWardName(ward) {
    return normalizeName(
        ward.full_name
        ?? ward.FullName
        ?? ward.fullName
        ?? ward.name
        ?? ward.Name
        ?? ward.ward_name
        ?? ward.WardName
        ?? ward.wardName
    );
}

function uniqueByCode(items) {
    const map = new Map();

    for (const item of items) {
        if (!map.has(item.code)) {
            map.set(item.code, item);
        }
    }

    return Array.from(map.values());
}

function buildFromNestedProvinceArray(payload) {
    if (!Array.isArray(payload)) {
        return [];
    }

    return payload
        .map((province) => {
            const wards = getProvinceWards(province)
                .map((ward) => ({
                    code: getWardCode(ward),
                    name: getWardName(ward)
                }))
                .filter((ward) => ward.code && ward.name)
                .sort((a, b) => a.code.localeCompare(b.code, "vi"));

            return {
                code: getProvinceCode(province),
                name: getProvinceName(province),
                wards: uniqueByCode(wards)
            };
        })
        .filter((province) => province.code && province.name)
        .sort((a, b) => a.code.localeCompare(b.code, "vi"));
}

function printDebug(payload) {
    console.log("Không nhận diện được cấu trúc JSON.");
    console.log("Top-level type:", Array.isArray(payload) ? "array" : typeof payload);

    if (payload && typeof payload === "object" && !Array.isArray(payload)) {
        console.log("Top-level keys:", Object.keys(payload));
    }

    if (Array.isArray(payload) && payload.length > 0) {
        console.log("First item keys:", Object.keys(payload[0]));

        if (Array.isArray(payload[0].Wards) && payload[0].Wards.length > 0) {
            console.log("First ward keys:", Object.keys(payload[0].Wards[0]));
        }
    }
}

const payload = await fetchJson(SOURCE_URL);

const result = buildFromNestedProvinceArray(payload);

if (result.length === 0) {
    printDebug(payload);
    throw new Error("Cannot build vietnam-addresses.json from source data.");
}

for (const province of result) {
    console.log(`${province.code} - ${province.name}: ${province.wards.length} xã/phường`);
}

const provinceCount = result.length;
const wardCount = result.reduce((total, province) => total + province.wards.length, 0);

console.log("--------------------------------");
console.log(`Tổng tỉnh/thành: ${provinceCount}`);
console.log(`Tổng xã/phường: ${wardCount}`);

if (provinceCount !== EXPECTED_PROVINCE_COUNT) {
    throw new Error(`Invalid province count: ${provinceCount}. Expected ${EXPECTED_PROVINCE_COUNT}.`);
}

if (wardCount !== EXPECTED_WARD_COUNT) {
    throw new Error(`Invalid ward count: ${wardCount}. Expected ${EXPECTED_WARD_COUNT}.`);
}

await fs.mkdir(path.dirname(OUTPUT_PATH), { recursive: true });
await fs.writeFile(OUTPUT_PATH, JSON.stringify(result, null, 2), "utf8");

console.log(`Generated: ${OUTPUT_PATH}`);