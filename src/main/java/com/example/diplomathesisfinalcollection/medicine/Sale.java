package com.example.diplomathesisfinalcollection.medicine;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "sales")
public class Sale {
    @Id
    private String id;
    private LocalDateTime saleDate; // Երբ է վաճառվել
    private double totalAmount; // Ամբողջ կտրոնի գումարը
    private String sellerUsername; // Ով է վաճառել
    private List<SaleItem> items = new ArrayList<>(); // Վաճառված դեղերի ցանկը

    public Sale() {}

    // Ստեղծիր Getters և Setters բոլոր դաշտերի համար
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}
