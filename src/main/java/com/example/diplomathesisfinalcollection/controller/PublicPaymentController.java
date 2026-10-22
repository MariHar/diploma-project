package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.services.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
@Controller
@RequiredArgsConstructor
public class PublicPaymentController {

    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;

    // 📱 ՍԱ ԿԲՌՆԻ QR ԿՈԴԻ ՀԱՐՑՈՒՄԸ ՄԻԱՆԳԱՄԻՑ
    @GetMapping("/pay/invoice/{id}")
    public String payInvoiceFromQR(@PathVariable String id,HttpServletRequest request) {
        try {
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(null)
                    .build()
                    .toUriString();

            // ⚠️ Փոխանցում ենք baseUrl-ը
            String checkoutUrl = paymentService.createCheckoutSessionForInvoice(invoice, baseUrl);
            return "redirect:" + checkoutUrl;

        } catch (StripeException e) {
            System.out.println("❌ Սխալ Stripe սեսիա ստեղծելիս (QR-ից): " + e.getMessage());
            return "redirect:/error";
        }
    }
}