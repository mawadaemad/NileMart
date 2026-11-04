package org.route;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(Customer customer, String message) {
        System.out.println("[SMS] to " + customer.phone() + ": " + message);
    }
}
