package org.route;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;

/** Gets ShippingQuoteService two ways: from the factory and from beans.xml. */
public class WiringDemo {

    public static void main(String[] args) {
        Governorate giza = Governorate.GIZA;
        BigDecimal subtotal = new BigDecimal("4350");

        ShippingQuoteService fromFactory = ObjectFactory.createShippingQuoteService();
        System.out.println("Factory: Giza, subtotal 4,350 -> " + fromFactory.quote(giza, subtotal));

        try (var context = new ClassPathXmlApplicationContext("beans.xml")) {
            ShippingQuoteService fromXml = context.getBean("shippingQuoteService", ShippingQuoteService.class);
            System.out.println("XML:     Giza, subtotal 4,350 -> " + fromXml.quote(giza, subtotal));
        }
    }
}
