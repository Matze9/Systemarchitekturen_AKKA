public class Banana extends Product{

    private final double weight;
    private final int storagePoints;
    private final String productName;

    public Banana(){
        this.weight = 0.5;
        this.storagePoints = 5;
        this.productName = "Banana";
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