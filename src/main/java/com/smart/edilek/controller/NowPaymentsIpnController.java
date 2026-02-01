package com.smart.edilek.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.service.GenericServiceImp;
import com.smart.edilek.entity.UserOrder;
import com.smart.edilek.entity.lookup.UserOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/payment")
public class NowPaymentsIpnController {

    @Value("${nowpayments.ipn.secret}")
    private String ipnSecret;

    @Autowired
    private GenericServiceImp<UserOrder> userOrderService;

    @Autowired
    private GenericServiceImp<UserOrderStatus> userOrderStatusService;

    @PostMapping("/ipn-callback")
    public ResponseEntity<String> handleIpnCallback(
            @RequestHeader(value = "x-nowpayments-sig", required = false) String signature,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            // 1. Anahtarları alfabetik sırala
            Map<String, Object> sortedPayload = new TreeMap<>(payload);
            ObjectMapper mapper = new ObjectMapper();
            String sortedJson = mapper.writeValueAsString(sortedPayload);

            // 2. HMAC SHA-512 ile imzala
            String calculatedSig = hmacSha512(ipnSecret, sortedJson);

            // 3. Karşılaştır
            if (signature == null || !calculatedSig.equals(signature)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // 4. Update UserOrder status
            Object paymentIdObj = payload.get("payment_id");
            String paymentStatus = (String) payload.get("payment_status");

            if (paymentIdObj != null && paymentStatus != null) {
                String paymentId = String.valueOf(paymentIdObj);
                try {
                    java.util.List<UserOrder> orders = userOrderService.find(UserOrder.class, "paymentRef", paymentId, MatchMode.equals, 1);
                    
                    if (orders != null && !orders.isEmpty()) {
                        UserOrder order = orders.get(0);
                        String targetCode = mapStatusToCode(paymentStatus);
                        
                        if (targetCode != null) {
                            java.util.List<UserOrderStatus> statuses = userOrderStatusService.find(UserOrderStatus.class, "code", targetCode, MatchMode.equals, 1);
                            
                            if (statuses != null && !statuses.isEmpty()) {
                                UserOrderStatus newStatus = statuses.get(0);
                                // Only update if status is different and not already completed (unless specific logic requires it)
                                if (order.getUserOrderStatus().getId() != newStatus.getId()) {
                                    order.setUserOrderStatus(newStatus);
                                    userOrderService.modify(order);
                                }
                            }
                        }
                    } else {
                        System.out.println("Order not found for payment_id: " + paymentId);
                    }
                } catch (Exception ex) {
                    System.err.println("Error updating order status: " + ex.getMessage());
                    // Don't fail the request, just log error
                }
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private String hmacSha512(String secret, String message) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA512");
        sha512_HMAC.init(keySpec);
        byte[] macData = sha512_HMAC.doFinal(message.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder(2 * macData.length);
        for (byte b : macData) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private String mapStatusToCode(String nowPaymentStatus) {
        switch (nowPaymentStatus) {
            case "finished":
                return "COMPLETED";
            case "failed":
            case "expired":
                return "FAILED"; // Assuming FAILED code exists
            case "refunded":
                return "REFUNDED"; // Assuming REFUNDED code exists
            case "waiting":
            case "confirming":
            case "sending":
                return "PENDING";
            default:
                return null; // Ignore other statuses
        }
    }
}
