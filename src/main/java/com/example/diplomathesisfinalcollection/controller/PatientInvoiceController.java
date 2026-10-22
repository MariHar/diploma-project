package com.example.diplomathesisfinalcollection.controller;

import com.example.diplomathesisfinalcollection.invoice.Invoice;
import com.example.diplomathesisfinalcollection.invoice.InvoiceRepository;
import com.example.diplomathesisfinalcollection.invoice.InvoiceStatus;
import com.example.diplomathesisfinalcollection.services.PaymentService;
import com.example.diplomathesisfinalcollection.user.User;
import com.example.diplomathesisfinalcollection.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.stripe.exception.StripeException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientInvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @GetMapping("/invoices")
    public String getMyInvoices(Model model, Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Invoice> pendingInvoices = invoiceRepository.findByPatientIdAndStatus(patient.getId(), InvoiceStatus.PENDING_PAYMENT);

        List<Invoice> paidInvoices = invoiceRepository.findByPatientIdAndStatus(patient.getId(), InvoiceStatus.PAID);

        model.addAttribute("pendingInvoices", pendingInvoices);
        model.addAttribute("paidInvoices", paidInvoices);
        return "patient/my-invoices";
    }

    // Վճարման կոճակի գործողությունը
    @PostMapping("/invoices/{id}/pay")
    public String payInvoice(@PathVariable String id, HttpServletRequest request) throws StripeException {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // ⚠️ ՓՈՓՈԽՎԱԾ Է. Կանչում ենք մեր նոր, դինամիկ ցուցակով մեթոդը
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        String checkoutUrl = paymentService.createCheckoutSessionForInvoice(invoice, baseUrl);
        return "redirect:" + checkoutUrl;
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return "redirect:/patient/invoices?success=true";
    }

    @GetMapping("/invoices/{id}/receipt")
    public String viewReceipt(@PathVariable String id, Model model, Principal principal) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Անվտանգության համար ստուգում ենք՝ արդյոք սա հենց այս պացիենտի հաշիվն է
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!invoice.getPatientId().equals(patient.getId())) {
            return "redirect:/error"; // Թույլ չտալ ուրիշի չեկը նայել
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("patient", patient);
        return "patient/receipt"; // կտանի պացիենտի պանակի receipt.html էջին
    }
}