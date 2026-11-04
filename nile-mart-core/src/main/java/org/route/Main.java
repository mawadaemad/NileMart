package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Map<Integer, ShoppingCart> carts = new LinkedHashMap<>();
    private static int nextCartNumber = 1;

    public static void main(String[] args) {
        // closing the context (try-with-resources) runs the @PreDestroy methods
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class);
             var in = new Scanner(System.in)) {

            ProductService productService = context.getBean(ProductService.class);
            CheckoutService checkoutService = context.getBean(CheckoutService.class);

            while (true) {
                printMenu();
                if (!in.hasNextLine()) {
                    break;
                }
                String choice = in.nextLine().trim();
                if (choice.equals("0")) {
                    break;
                }
                try {
                    switch (choice) {
                        case "1" -> showProducts(productService);
                        case "2" -> createCart(context, in);
                        case "3" -> addToCart(in);
                        case "4" -> showCart(productService, in);
                        case "5" -> checkOut(checkoutService, in);
                        default -> System.out.println("Unknown option.");
                    }
                } catch (StoreException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            System.out.println("Goodbye!");
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== NILEMART ==========");
        System.out.println("1. Show products");
        System.out.println("2. Create a cart");
        System.out.println("3. Add a product to a cart");
        System.out.println("4. Show a cart");
        System.out.println("5. Check out a cart");
        System.out.println("0. Exit");
        System.out.print("> ");
    }

    private static void showProducts(ProductService productService) {
        System.out.printf("%-14s %-28s %-16s %12s %6s%n", "SKU", "Name", "Category", "Price (EGP)", "Stock");
        for (Product p : productService.findAll()) {
            System.out.printf("%-14s %-28s %-16s %12s %6d%n",
                    p.getSku(), p.getName(), p.getCategory(), Money.format(p.getPrice()), p.getStockQuantity());
        }
    }

    private static void createCart(AnnotationConfigApplicationContext context, Scanner in) {
        String name = ask(in, "Customer name: ");
        String phone = ask(in, "Phone: ");
        String email = ask(in, "Email (optional): ");
        if (name.isBlank() || phone.isBlank()) {
            throw new StoreException("Name and phone are required.");
        }
        ShoppingCart cart = context.getBean(ShoppingCart.class); // prototype: a new object each time
        cart.setCartNumber(nextCartNumber++);
        cart.setCustomer(new Customer(name, phone, email.isBlank() ? null : email));
        carts.put(cart.getCartNumber(), cart);
        System.out.println("Cart #" + cart.getCartNumber() + " created for " + name + ".");
    }

    private static void addToCart(Scanner in) {
        ShoppingCart cart = askCart(in);
        String sku = ask(in, "Product SKU: ");
        int qty = askInt(in, "Quantity: ");
        cart.addItem(sku, qty);
        System.out.println("Added " + qty + " x " + sku + " to cart #" + cart.getCartNumber() + ".");
    }

    private static void showCart(ProductService productService, Scanner in) {
        ShoppingCart cart = askCart(in);
        System.out.println("Cart #" + cart.getCartNumber() + " - " + cart.getCustomer().fullName());
        if (cart.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        cart.getLines().forEach((sku, qty) -> System.out.printf("  %-28s x%-3d %12s EGP%n",
                productService.findBySku(sku).getName(), qty, Money.format(productService.getPrice(sku, qty))));
        System.out.println("  Subtotal: " + Money.format(cart.subtotal()) + " EGP");
    }

    private static void checkOut(CheckoutService checkoutService, Scanner in) {
        ShoppingCart cart = askCart(in);
        Governorate[] all = Governorate.values();
        for (int i = 0; i < all.length; i++) {
            System.out.printf("%2d. %s%n", i + 1, all[i]);
        }
        int index = askInt(in, "Governorate number: ");
        if (index < 1 || index > all.length) {
            throw new StoreException("Invalid governorate number.");
        }
        Order order = checkoutService.placeOrder(cart, all[index - 1]);
        System.out.println("Order " + order.orderNumber() + " placed.");
        System.out.println("  Subtotal: " + Money.format(order.subtotal()) + " EGP");
        System.out.println("  VAT:      " + Money.format(order.vat()) + " EGP");
        System.out.println("  Shipping: " + Money.format(order.shippingFee()) + " EGP");
        System.out.println("  Total:    " + Money.format(order.total()) + " EGP");
    }

    private static ShoppingCart askCart(Scanner in) {
        int number = askInt(in, "Cart number: ");
        ShoppingCart cart = carts.get(number);
        if (cart == null) {
            throw new StoreException("Cart #" + number + " does not exist.");
        }
        return cart;
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private static int askInt(Scanner in, String prompt) {
        String text = ask(in, prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new StoreException("'" + text + "' is not a valid number.");
        }
    }
}
