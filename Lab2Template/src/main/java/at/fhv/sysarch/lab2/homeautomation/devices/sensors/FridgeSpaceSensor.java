package at.fhv.sysarch.lab2.homeautomation.devices.sensors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;

import java.util.List;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.FridgeSpaceCommand> {


    public interface FridgeSpaceCommand{}

    public Behavior<FridgeSpaceCommand>create(ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> fridgeSpaceSensor, String groupId, String deviceId){
        return Behaviors.setup(context -> new FridgeSpaceSensor(context, fridgeSpaceSensor, groupId, deviceId));
    }

    public static final class ValidateSpace implements FridgeSpaceCommand{
        private List<Product> orderedProducts;
        private Stock stock;

        public ValidateSpace(List<Product> orderedProducts, Stock stock){
            this.orderedProducts = orderedProducts;
            this.stock = stock;
        }

    }



    private String groupId;
    private String deviceId;
    private ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> fridgeSpaceSensor;

    public FridgeSpaceSensor(ActorContext<FridgeSpaceCommand> context, ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> fridgeSpaceSensor, String groupId, String deviceId){
        super(context);
        this.fridgeSpaceSensor = fridgeSpaceSensor;
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("Fridge Space Sensor successfully started.");

    }


    @Override
    public Receive<FridgeSpaceCommand> createReceive() {
        //return newReceiveBuilder().onMessage(ValidateSpace.class, this::onSpaceValidation).onSignal(PostStop.class, signal -> onPostStop()).build();
        return newReceiveBuilder().onMessage(ValidateSpace.class, this::onSpaceValidation).onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    public Behavior<FridgeSpaceSensor.FridgeSpaceCommand> onSpaceValidation(ValidateSpace v){

        int orderedProductsSpace = 0;
        int currentSpace;
        for (Product p : v.orderedProducts){
            orderedProductsSpace += p.getStoragePoints();
        }

        currentSpace = orderedProductsSpace + v.stock.getCurrentSpace();

        if(currentSpace <= v.stock.getMaxSpace()){

            //TODO: send ok message to fridge
            getContext().getLog().info("Fridge space is OK");
        }else{
            //TODO: send error message to fridge
            getContext().getLog().info("Fridge space is not OK");
        }

        return this;
    }

    private FridgeSpaceSensor onPostStop(){
        getContext().getLog().info("Fridge sapce sensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}
