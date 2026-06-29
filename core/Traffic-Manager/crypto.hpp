#ifndef CRYPTO_HPP
#define CRYPTO_HPP

extern "C" {
#include "aes.h"
}
#include <string>

// Note: Hardcoded keys are for PoC/Portfolio purposes only.
const uint8_t AES_KEY[] = "TajnyKlucz123456";
const uint8_t AES_IV[] = "WektorInicjuj123";

// AES block ciphers require data length to be an exact multiple of 16 bytes.
// This function pads the remaining space with the byte value of the padding length.
inline std::string addPadding(const std::string& input) {
    size_t padLen = 16 - (input.length() % 16);
    std::string padded = input;
    padded.append(padLen, (char)padLen);
    return padded;
}

inline std::string removePadding(const std::string& input) {
    if (input.empty()) return input;
    char padLen = input.back();
    if (padLen > 0 && padLen <= 16) {
        return input.substr(0, input.length() - padLen);
    }
    return input;
}

inline std::string encryptPayload(const std::string& plainText) {
    std::string padded = addPadding(plainText);
    struct AES_ctx ctx{};
    AES_init_ctx_iv(&ctx, AES_KEY, AES_IV);
    AES_CBC_encrypt_buffer(&ctx, (uint8_t*)padded.data(), padded.length());
    return padded;
}

inline std::string decryptPayload(const std::string& cipherText) {
    if (cipherText.length() % 16 != 0) return cipherText;
    std::string decrypted = cipherText;
    struct AES_ctx ctx{};
    AES_init_ctx_iv(&ctx, AES_KEY, AES_IV);
    AES_CBC_decrypt_buffer(&ctx, (uint8_t*)decrypted.data(), decrypted.length());
    return removePadding(decrypted);
}

#endif