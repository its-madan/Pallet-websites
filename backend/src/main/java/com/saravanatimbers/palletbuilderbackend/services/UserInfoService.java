package com.saravanatimbers.palletbuilderbackend.services;

import com.saravanatimbers.palletbuilderbackend.models.UserInfo;
import com.saravanatimbers.palletbuilderbackend.repositories.UserInfoRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.QuoteRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.OrderRepository;
import com.saravanatimbers.palletbuilderbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import com.saravanatimbers.palletbuilderbackend.models.Order;

@Service
public class UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    public Optional<UserInfo> getUserInfoByUserId(String userId) {
        return userInfoRepository.findByUserId(userId);
    }

    public UserInfo saveUserInfo(UserInfo userInfo) {
        userInfo.setUpdatedAt(Instant.now());
        if (userInfo.getCreatedAt() == null) {
            userInfo.setCreatedAt(Instant.now());
        }
        // Only auto-assign if discount category is null (not manually set)
        if (userInfo.getDiscountCategory() == null) {
            assignDiscountCategory(userInfo);
        }
        return userInfoRepository.save(userInfo);
    }

    public UserInfo updateUserInfo(String userId, UserInfo updatedUserInfo) {
        Optional<UserInfo> existingUserInfo = userInfoRepository.findByUserId(userId);
        
        if (existingUserInfo.isPresent()) {
            UserInfo userInfo = existingUserInfo.get();
            userInfo.setFullName(updatedUserInfo.getFullName());
            userInfo.setEmail(updatedUserInfo.getEmail());
            userInfo.setPhone(updatedUserInfo.getPhone());
            userInfo.setCompany(updatedUserInfo.getCompany());
            userInfo.setAddress(updatedUserInfo.getAddress());
            userInfo.setState(updatedUserInfo.getState());
            userInfo.setPincode(updatedUserInfo.getPincode());
            userInfo.setUpdatedAt(Instant.now());
            // Respect manually set discount category (from admin panel)
            if (updatedUserInfo.getDiscountCategory() != null) {
                System.out.println("[UserInfoService] Preserving manually set discount category: " + updatedUserInfo.getDiscountCategory());
                userInfo.setDiscountCategory(updatedUserInfo.getDiscountCategory());
            } else {
                // Only auto-assign if no discount category is provided
                System.out.println("[UserInfoService] Auto-assigning discount category");
                assignDiscountCategory(userInfo);
            }
            
            return userInfoRepository.save(userInfo);
        } else {
            // If no existing user info, create new one
            updatedUserInfo.setUserId(userId);
            // Respect manually set discount category (from admin panel)
            if (updatedUserInfo.getDiscountCategory() != null) {
                // Keep the manually set category
            } else {
                // Only auto-assign if no discount category is provided
                assignDiscountCategory(updatedUserInfo);
            }
            return saveUserInfo(updatedUserInfo);
        }
    }

    // Special method for admin manual discount category override
    public UserInfo updateUserInfoWithManualDiscountOverride(String userId, UserInfo updatedUserInfo) {
        Optional<UserInfo> existingUserInfo = userInfoRepository.findByUserId(userId);
        
        if (existingUserInfo.isPresent()) {
            UserInfo userInfo = existingUserInfo.get();
            userInfo.setFullName(updatedUserInfo.getFullName());
            userInfo.setEmail(updatedUserInfo.getEmail());
            userInfo.setPhone(updatedUserInfo.getPhone());
            userInfo.setCompany(updatedUserInfo.getCompany());
            userInfo.setAddress(updatedUserInfo.getAddress());
            userInfo.setState(updatedUserInfo.getState());
            userInfo.setPincode(updatedUserInfo.getPincode());
            userInfo.setUpdatedAt(Instant.now());
            
            // For manual admin override, always respect the provided discount category (including null)
            System.out.println("[UserInfoService] Manual admin override - setting discount category to: " + updatedUserInfo.getDiscountCategory());
            userInfo.setDiscountCategory(updatedUserInfo.getDiscountCategory());
            
            return userInfoRepository.save(userInfo);
        } else {
            // If no existing user info, create new one
            updatedUserInfo.setUserId(userId);
            // For manual admin override, always respect the provided discount category (including null)
            System.out.println("[UserInfoService] Manual admin override - creating new user with discount category: " + updatedUserInfo.getDiscountCategory());
            return saveUserInfo(updatedUserInfo);
        }
    }



    public boolean userInfoExists(String userId) {
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(userId);
        if (userInfoOpt.isEmpty()) return false;
        UserInfo userInfo = userInfoOpt.get();
        return isProfileComplete(userInfo);
    }

    private boolean isProfileComplete(UserInfo userInfo) {
        boolean complete = userInfo.getFullName() != null && !userInfo.getFullName().trim().isEmpty()
            && userInfo.getEmail() != null && !userInfo.getEmail().trim().isEmpty()
            && userInfo.getPhone() != null && !userInfo.getPhone().trim().isEmpty()
            && userInfo.getAddress() != null && !userInfo.getAddress().trim().isEmpty()
            && userInfo.getState() != null && !userInfo.getState().trim().isEmpty()
            && userInfo.getPincode() != null && !userInfo.getPincode().trim().isEmpty();
        System.out.println("[isProfileComplete] fullName='" + userInfo.getFullName() + "', email='" + userInfo.getEmail() + "', phone='" + userInfo.getPhone() + "', address='" + userInfo.getAddress() + "', state='" + userInfo.getState() + "', pincode='" + userInfo.getPincode() + "' => " + complete);
        return complete;
    }

    public void deleteUserInfo(String userId) {
        // Delete all quotes
        quoteRepository.deleteByUserId(userId);
        // Delete all orders
        orderRepository.deleteByUserId(userId);
        // Delete user account
        userRepository.deleteByEmail(userId);
        // Delete user info
        Optional<UserInfo> userInfo = userInfoRepository.findByUserId(userId);
        userInfo.ifPresent(userInfoRepository::delete);
    }

    public List<UserInfo> getAllUserInfos() {
        return userInfoRepository.findAll();
    }

    // Assign discount category based on order history
    public void assignDiscountCategory(UserInfo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) return;
        List<Order> orders = orderRepository.findByUserId(userInfo.getUserId());
        int orderCount = orders.size();
        double totalValue = orders.stream().mapToDouble(this::getTotalPrice).sum();
        boolean isBulk = orders.stream().anyMatch(this::isBulkOrder);
        // Instant does not support MONTHS directly. Subtract months in UTC using ZonedDateTime and convert back to Instant.
        Instant sixMonthsAgo = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(6).toInstant();
        long recentOrders = orders.stream().filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(sixMonthsAgo)).count();
        
        String oldCategory = userInfo.getDiscountCategory();
        if (orderCount > 10 || totalValue > 1000000) {
            userInfo.setDiscountCategory("VIP");
        } else if (isBulk) {
            userInfo.setDiscountCategory("Bulk Orders");
        } else if (recentOrders > 5) {
            userInfo.setDiscountCategory("Frequent Buyer");
        } else {
            userInfo.setDiscountCategory(null);
        }
        System.out.println("[UserInfoService] Auto-assigned discount category for user " + userInfo.getUserId() + ": " + oldCategory + " -> " + userInfo.getDiscountCategory() + " (orders: " + orderCount + ", value: " + totalValue + ", bulk: " + isBulk + ", recent: " + recentOrders + ")");
    }

    // Helper to extract totalPrice from quoteDetails map
    private double getTotalPrice(Order o) {
        if (o.getQuoteDetails() != null && o.getQuoteDetails().get("totalPrice") != null) {
            try {
                return Double.parseDouble(o.getQuoteDetails().get("totalPrice").toString());
            } catch (Exception e) { return 0.0; }
        }
        return 0.0;
    }
    // Helper to check if order is bulk
    private boolean isBulkOrder(Order o) {
        if (o.getQuoteDetails() != null && o.getQuoteDetails().get("quantity") != null) {
            try {
                return Integer.parseInt(o.getQuoteDetails().get("quantity").toString()) > 100;
            } catch (Exception e) { return false; }
        }
        return false;
    }
} 