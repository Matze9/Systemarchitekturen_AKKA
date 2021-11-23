package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.Actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeWeightSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;
import scala.concurrent.impl.Promise;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {



    public interface FridgeCommand {}

    //places an order and adds products to the fridge if possible
    public static final class PlaceOrder implements FridgeCommand{
        public final LinkedList<Product> orderItems;
        public final Stock stock;

        public PlaceOrder( LinkedList<Product> orderItems, Stock stock){
            this.orderItems = orderItems;
            this.stock = stock;
        }
    }

    //get all products from the fridge
    public static final class GetProducts implements FridgeCommand{
        private HashMap<String, LinkedList<Product>> products;

        public GetProducts (){products = stock.getAllProducts();}
    }


    //removes item from fridge. TODO: place order if item runs empty
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
    private ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor;
    private ActorRef<FridgeWeightSensor.ValidateWeight> weightSensor;
    private ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessor;
    private static Stock stock;



    public Fridge(ActorContext<FridgeCommand> context, ActorRef<Fridge.FridgeCommand> fridge, String groupId, String deviceId){
        super(context);
        this.fridge = fridge;
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.stock = new Stock();

        this.spaceSensor = context.spawn(FridgeSpaceSensor.create(), "space-sensor");
        this.weightSensor = context.spawn(FridgeWeightSensor.create(), "weight-sensor");
        getContext().getLog().info("Fridge successfully started");
    }





    private Behavior<FridgeCommand> onOrderPlaced(PlaceOrder p){
        orderProcessor = getContext().spawn(OrderProcessor.create(orderProcessor, stock, p.orderItems), "processor");
        orderProcessor.tell(new OrderProcessor.CallFridgeWeightSensor(p.orderItems, weightSensor));
        orderProcessor.tell(new OrderProcessor.CallFridgeSpaceSensor(p.orderItems, spaceSensor));
        return this;
    }




    private Behavior<FridgeCommand> onGetProducts (GetProducts g){
        getContext().getLog().info("All items in your fridge: ");
        stock.getAllProducts().forEach((productName, productList) ->{
            productList.forEach(product -> getContext().getLog().info(product.getProductName() + ", added: " + product.getAddedOn() ));
        });
        return this;
    }


    //TODO: place order if item is empty
    private Behavior<FridgeCommand> onProductConsumption(ConsumeProduct c){
        if(stock.getAllProducts().containsKey(c.product.getProductName())){
           stock.removeProduct(c.product);
           getContext().getLog().info("Sucessfully removed " + c.product.getProductName(), c.product.getProductName());
        }else{
            getContext().getLog().info("You tried to remove a product which seems to be not in the fridge...");
       }
        return this;
    }


    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConsumeProduct.class, this::onProductConsumption)
                .onMessage(PlaceOrder.class, this::onOrderPlaced)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }


    private Fridge onPostStop(){
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
