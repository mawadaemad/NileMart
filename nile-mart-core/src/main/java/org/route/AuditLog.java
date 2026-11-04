package org.route;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AuditLog {

    @Before("execution(* org.route.CheckoutService.placeOrder(..))")
    public void audit(JoinPoint jp) {
        Object[] args = jp.getArgs();
        ShoppingCart cart = (ShoppingCart) args[0];
        Governorate governorate = (Governorate) args[1];
        System.out.println("[AUDIT] placeOrder: cart #" + cart.getCartNumber()
                + ", governorate " + governorate);
    }
}
