package com.saravanatimbers.palletbuilderbackend.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final Integer EXPIRE_MINS = 5;
    private LoadingCache<String, String> otpCache;

    public OtpService() {
        super();
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    public String load(String key) {
                        return "";
                    }
                });
    }

    public String generateOtp(String key) {
        Random random = new Random();
        String otp = String.valueOf(100000 + random.nextInt(900000));
        otpCache.put(key, otp);
        return otp;
    }

    public String getOtp(String key) {
        try {
            return otpCache.get(key);
        } catch (Exception e) {
            return "";
        }
    }

    public void clearOtp(String key) {
        otpCache.invalidate(key);
    }

    public boolean validateOtp(String key, String otp) {
        String cachedOtp = getOtp(key);
        if (cachedOtp != null && cachedOtp.equals(otp)) {
            clearOtp(key);
            return true;
        }
        return false;
    }
} 