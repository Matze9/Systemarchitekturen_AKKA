package at.fhv.sysarch.lab2.homeautomation.modelClasses;

public abstract class Product{

    private double weight;
    private int storagePoints;
    private String productName;

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