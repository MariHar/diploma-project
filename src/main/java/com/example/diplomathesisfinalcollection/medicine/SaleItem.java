package com.example.diplomathesisfinalcollection.medicine;


import java.time.LocalDate;

public class SaleItem {
    private String medicineId;
    private String medicineName;
    private String category;
    private int quantity;
    private double priceAtSale; // Գինը վաճառքի պահին (որպեսզի եթե հետո գինը փոխվի, անդորրագիրը չխախտվի)
    private LocalDate batchExpirationDate; // Որ ժամկետով խմբաքանակից է հանվել

    // Ստեղծիր դատարկ Constructor, և Getters / Setters բոլոր դաշտերի համար
    public SaleItem() {}

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(double priceAtSale) { this.priceAtSale = priceAtSale; }

    public LocalDate getBatchExpirationDate() { return batchExpirationDate; }
    public void setBatchExpirationDate(LocalDate batchExpirationDate) { this.batchExpirationDate = batchExpirationDate; }

    // Ընդհանուր գինը այս դեղի համար
    public double getTotalPrice() {
        return this.quantity * this.priceAtSale;
    }
}
