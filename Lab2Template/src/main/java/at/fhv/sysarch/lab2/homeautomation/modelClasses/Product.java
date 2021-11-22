package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import java.time.LocalDate;

public abstract class Product{

    private double weight;
    private double price;
    private int storagePoints;
    private String productName;
    private LocalDate addedOn;

    public double getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }

    public int getStoragePoints() {
        return storagePoints;
    }

    public String getProductName() {
        return productName;
    }

    public LocalDate getAddedOn() {
        return addedOn;
    }
}