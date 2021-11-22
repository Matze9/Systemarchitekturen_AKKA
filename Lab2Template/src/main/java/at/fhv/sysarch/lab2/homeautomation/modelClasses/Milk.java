package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

public class Milk extends Product {

    private final double weight;
    private final int storagePoints;
    private final String productName;

    public Milk(){
        this.weight = 1.0;
        this.storagePoints = 3;
        this.productName = "Milk";
    }

    public double getWeight() {
        return weight;
    }

    public int getStoragePoints() {
        return storagePoints;
    }

    public String getProductName() {
        return productName;
    }
}