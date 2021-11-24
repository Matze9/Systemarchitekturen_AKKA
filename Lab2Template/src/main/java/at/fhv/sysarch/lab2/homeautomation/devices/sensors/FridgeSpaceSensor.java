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
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;

import java.util.List;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.ValidateSpace> {



    public static final class ValidateSpace{
        private List<Product> orderedProducts;
        private Stock stock;
        public final ActorRef<OrderProcessor.OrderProcessorCommand> replyTo;

        public ValidateSpace(List<Product> orderedProducts, Stock stock, ActorRef<OrderProcessor.OrderProcessorCommand> replyTo){
            this.orderedProducts = orderedProducts;
            this.stock = stock;
            this.replyTo = replyTo;
        }
    }


    public static Behavior<ValidateSpace>create(){
        return Behaviors.setup(FridgeSpaceSensor::new);
    }

    public FridgeSpaceSensor(ActorContext<ValidateSpace> context){
        super(context);
        getContext().getLog().info("SPACE SENSOR ACTIVE.");
    }

    @Override
    public Receive<ValidateSpace> createReceive() {
        return newReceiveBuilder().onMessage(ValidateSpace.class, this::onSpaceValidation).build();
    }

    private Behavior<ValidateSpace> onSpaceValidation(ValidateSpace v){
        int orderedProductsSpace = 0;
        int currentSpace;
        for (Product p : v.orderedProducts){
            orderedProductsSpace += p.getStoragePoints();
        }

        currentSpace = orderedProductsSpace + v.stock.getCurrentSpace();
        v.stock.setCurrentSpace(currentSpace);
        if(currentSpace <= v.stock.getMaxSpace()){

            v.replyTo.tell(new OrderProcessor.SpaceValidationResponse("OK", getContext().getSelf()));
        }else{
            v.replyTo.tell(new OrderProcessor.SpaceValidationResponse("ERROR", getContext().getSelf()));
            getContext().getLog().info("Fridge space ERROR");
        }

        return this;
    }



}
