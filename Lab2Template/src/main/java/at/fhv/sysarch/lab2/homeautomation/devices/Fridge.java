package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;

import java.util.List;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {



    public interface FridgeCommand {}

    public static final class GetProducts implements FridgeCommand{
        private List<Product> products;

        public GetProducts (){products = stock.getAllProducts();}
    }

    public static final class AddProduct implements FridgeCommand{
        private Product product;
        public AddProduct(Product product){
            this.product = product;
            stock.addProduct(product);
        }
    }

    public static final class ConsumeProduct implements FridgeCommand {
        private Product product;
        public ConsumeProduct(Product product){
            this.product = product;
        }
    }

    public static Behavior<FridgeCommand> create (ActorRef<Fridge.FridgeCommand> fridge, String groupId, String deviceId){
        return Behaviors.setup(context -> new Fridge(context, fridge, groupId, deviceId));
    }

    private final String groupId;
    private final String deviceId;
    private ActorRef<Fridge.FridgeCommand> fridge;
    private static Stock stock;


    public Fridge(ActorContext<FridgeCommand> context, ActorRef<Fridge.FridgeCommand> fridge, String groupId, String deviceId){
        super(context);
        this.fridge = fridge;
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.stock = new Stock();

        getContext().getLog().info("Fridge successfully started");
    }


    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder().onMessage(AddProduct.class, this::onAddProduct).onMessage(ConsumeProduct.class, this::onProductConsumption).onSignal(PostStop.class, signal -> onPostStop()).build();
    }







    private Behavior<FridgeCommand> onGetProducts (GetProducts g){
        getContext().getLog().info("Your Fridge has following items: ");
        stock.getAllProducts().forEach(product -> getContext().getLog().info(product.getProductName()));
        return this;
    }

    private Behavior<FridgeCommand> onAddProduct (AddProduct p){
        getContext().getLog().info("Adding item to your fridge: " + p.product.getProductName() , p.product.getProductName());


        getContext().getLog().info("All items in your fridge: ");
        stock.getAllProducts().forEach(product -> getContext().getLog().info(product.getProductName()));
        return this;
    }

    private Behavior<FridgeCommand> onProductConsumption(ConsumeProduct c){
        if(stock.getAllProducts().contains(c.product)){
           stock.removeProduct(c.product);
           getContext().getLog().info("Sucessfully removed " + c.product.getProductName(), c.product.getProductName());
        }else{
            getContext().getLog().info("You tried to remove a product which seems to be not in the fridge...");
       }
        return this;
    }

    private Fridge onPostStop(){
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
