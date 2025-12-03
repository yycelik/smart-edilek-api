package com.smart.edilek.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.service.GenericServiceImp;
import com.smart.edilek.entity.LicensePackage;
import com.smart.edilek.entity.User;
import com.smart.edilek.entity.UserOrder;
import com.smart.edilek.entity.lookup.Currency;
import com.smart.edilek.entity.lookup.PaymentProvider;
import com.smart.edilek.entity.lookup.UserOrderStatus;
import com.smart.edilek.models.UserSyncRequest;

@Service
public class UserService {

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private GenericServiceImp<UserOrder> userOrderService;
    
    @Autowired
    private GenericServiceImp<LicensePackage> licensePackageService;
    
    @Autowired
    private GenericServiceImp<UserOrderStatus> userOrderStatusService;
    
    @Autowired
    private GenericServiceImp<PaymentProvider> paymentProviderService;
    
    @Autowired
    private GenericServiceImp<Currency> currencyService;

    /**
     * Sync user from Keycloak to database
     * Creates user if not exists, updates if exists
     */
    @Transactional
    public User syncUserFromKeycloak(UserSyncRequest request) {
        User user = null;

        // Search for user by keycloakId
        if (request.getKeycloakId() != null && !request.getKeycloakId().isEmpty()) {
            List<User> users = userGenericService.find(
                User.class, 
                "id", 
                request.getKeycloakId(), 
                MatchMode.equals, 
                1
            );

            if (!users.isEmpty()) {
                user = users.get(0);
            }
        }

        // If user not found, create new one
        if (user == null) {
            user = new User();
            user.setId(request.getKeycloakId());
            user.setUsername(request.getUsername());
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setActive(true);
            
            userGenericService.add(user);
            
            createFreeOrderForUser(user);
        } else {
            user.setUsername(request.getUsername());
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            
            user = userGenericService.modify(user);
        }

        return user;
    }

    private void createFreeOrderForUser(User user) {
        try {
            // Find free package (FREE_P3)
            LicensePackage freePackage = licensePackageService.get(LicensePackage.class, 4);
            
            // Find status 'PAID'
            UserOrderStatus status = userOrderStatusService.get(UserOrderStatus.class, 5);
            
            // Find payment provider 'CAMPAIGN'
            PaymentProvider provider = paymentProviderService.get(PaymentProvider.class, 2);
            
            // Find currency 'TRY'
            Currency currency = currencyService.get(Currency.class, 1);

            UserOrder order = new UserOrder();
            order.setUser(user);
            order.setLicensePackage(freePackage);
            order.setUserOrderStatus(status);
            order.setPaymentProvider(provider);
            order.setPrice(BigDecimal.ZERO);
            order.setCurrency(currency);
            order.setPurchasedAt(LocalDateTime.now());
            order.setCreatedBy(user.getId());
            order.setActive(true);
            
            userOrderService.add(order);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
