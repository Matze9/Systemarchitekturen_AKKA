public class Milk extends Product{

    private final double weight;
    private final int storagePoints;
    private final String productName;

    public Banana(){
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