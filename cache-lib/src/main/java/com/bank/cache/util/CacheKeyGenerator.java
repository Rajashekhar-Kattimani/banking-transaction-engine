package com.bank.cache.util;

import com.bank.cache.constant.CacheConstants;

public final class CacheKeyGenerator {

    private CacheKeyGenerator() {
    }

    public static String account(Long accountId) {
        return CacheConstants.ACCOUNT + accountId;
    }

    public static String customer(Long customerId) {
        return CacheConstants.CUSTOMER + customerId;
    }

    public static String refreshToken(Long userId) {
        return CacheConstants.REFRESH_TOKEN + userId;
    }

    public static String jwtBlacklist(String tokenId) {
        return CacheConstants.JWT_BLACKLIST + tokenId;
    }

    public static String session(Long userId) {
        return CacheConstants.USER_SESSION + userId;
    }

    public static String otp(String mobile) {
        return CacheConstants.OTP + mobile;
    }

    public static String idempotency(String key) {
        return CacheConstants.IDEMPOTENCY + key;
    }

    public static String branch(String branchCode) {
        return CacheConstants.BRANCH + branchCode;
    }
}