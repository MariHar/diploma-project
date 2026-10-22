package com.example.diplomathesisfinalcollection.services;

import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.medicine.PharmacyOrder;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    // --- METHOD 1: FOR CLINIC VISITS / LABS (Invoices) ---
    public String createCheckoutSession(Invoice invoice) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
// PaymentService.java -ում
                .setSuccessUrl("http://localhost:8080/patient/payment-success?invoiceId=" + invoice.getId() + "&session_id={CHECKOUT_SESSION_ID}")                .setCancelUrl("http://localhost:8080/patient/invoices")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("amd")
                                .setUnitAmount((long)(invoice.getTotalAmount() * 100)) // Stripe uses cents
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Medical Services Invoice #" + invoice.getId().substring(invoice.getId().length()-6))
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    // --- METHOD 2: FOR MEDICINES (PharmacyOrders) ---
    public String createPharmacyCheckoutSession(PharmacyOrder order) throws StripeException {
        String drugList = String.join(", ", order.getPurchasedDrugs());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/pharmacy/payment-success?orderId=" + order.getId())
                .setCancelUrl("http://localhost:8080/profile")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("amd")
                                .setUnitAmount((long)(order.getTotalAmount() * 100))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Pharmacy Order #" + order.getId().substring(order.getId().length()-6))
                                        .setDescription(drugList)
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    // Ավելացրու սա գոյություն ունեցող PaymentService-ում
    public String createCheckoutSessionForInvoice(Invoice invoice, String baseUrl) throws StripeException {
        // Պատրաստում ենք բոլոր Line Items-ները Stripe-ի համար
        List<SessionCreateParams.LineItem> stripeItems = new ArrayList<>();

        for (Invoice.LineItem item : invoice.getLineItems()) {
            stripeItems.add(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("amd")
                            .setUnitAmount((long) (item.getUnitPrice() * 100)) // Stripe-ը սենթերով է հաշվում
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getDescription())
                                    .build())
                            .build())
                    .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // Հղումը, որտեղ կգնա վճարումից հետո (ավտոմատ չեկի համար)
                .setSuccessUrl(baseUrl + "/patient/payment-success?invoiceId=" + invoice.getId())
                .setCancelUrl(baseUrl + "/patient/invoices")
                .addAllLineItem(stripeItems)
                .build();

        Session session = Session.create(params);
        return session.getUrl(); // Սա այն հղումն է, որը պետք է ուղարկել մեյլով
    }
}