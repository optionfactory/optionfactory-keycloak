// preview stub for the rfc4648 module (keycloak common theme): only
// base64url.parse/stringify are used by the base login scripts
const lookup = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
export const base64url = {
    parse(input) {
        const clean = input.replace(/=+$/, "");
        const bytes = [];
        let buffer = 0, bits = 0;
        for (const c of clean) {
            buffer = (buffer << 6) | lookup.indexOf(c);
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                bytes.push((buffer >> bits) & 0xff);
            }
        }
        return new Uint8Array(bytes);
    },
    stringify(bytes) {
        let out = "";
        let buffer = 0, bits = 0;
        for (const b of bytes) {
            buffer = (buffer << 8) | b;
            bits += 8;
            while (bits >= 6) {
                bits -= 6;
                out += lookup[(buffer >> bits) & 0x3f];
            }
        }
        if (bits > 0) {
            out += lookup[(buffer << (6 - bits)) & 0x3f];
        }
        return out;
    },
};
