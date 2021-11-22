package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

public class Butter extends Product {

    private final double weight;
    private final int storagePoints;
    private final String productName;

    public Butter(){
        this.weight = 0.25;
        this.storagePoints = 2;
        this.productName = "Butter";
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