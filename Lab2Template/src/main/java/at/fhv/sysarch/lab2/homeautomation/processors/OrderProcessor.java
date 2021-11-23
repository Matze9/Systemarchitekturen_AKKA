package at.fhv.sysarch.lab2.homeautomation.processors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;

import java.util.LinkedList;
import java.util.List;

public class OrderProcessor extends AbstractBehavior<OrderProcessor.OrderProcessorCommand> {



    public interface OrderProcessorCommand{}

    public static class SpaceValidationResponse implements OrderProcessorCommand{
        private String msg;
        private ActorRef<FridgeSpaceSensor.ValidateSpace> from;

        public SpaceValidationResponse(String msg, ActorRef<FridgeSpaceSensor.ValidateSpace> from){
            this.msg = msg;
            this.from = from;
        }

        public String getMsg() {
            return msg;
        }

        public ActorRef<FridgeSpaceSensor.ValidateSpace> getFrom() {
            return from;
        }
    }

    public static class CallFridgeSpaceSensor implements OrderProcessorCommand{
        private LinkedList<Product> products;
        public final ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor;

        public CallFridgeSpaceSensor(LinkedList<Product> products, ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor){
            this.products= products;
            this.spaceSensor = spaceSensor;
        }
    }




    public static Behavior<OrderProcessorCommand>create(ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessorCommand){
        return Behaviors.setup(context -> new OrderProcessor(context, orderProcessorCommand));
    }

    private static Stock stock;
    private final ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessorCommand;

    public OrderProcessor(ActorContext<OrderProcessorCommand>context, ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessorCommand){
        super(context);
        stock = new Stock();
        this.orderProcessorCommand = orderProcessorCommand;

        getContext().getLog().info("OrderProcessor activated");

    }

    private Behavior<OrderProcessorCommand> onFridgeSpaceSensorCall(CallFridgeSpaceSensor c){
        c.spaceSensor.tell(new FridgeSpaceSensor.ValidateSpace(c.products, stock, getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<OrderProcessorCommand> onFridgeSpaceSensorResponse(SpaceValidationResponse s){
        getContext().getLog().info("[ORDERPROCESSOR] reponse recieved from space sensor: " + s.getMsg());
        return this;
    }


    @Override
    public Receive<OrderProcessorCommand> createReceive() {
        return newReceiveBuilder().onMessage(CallFridgeSpaceSensor.class, this::onFridgeSpaceSensorCall).onMessage(SpaceValidationResponse.class, this::onFridgeSpaceSensorResponse).build();
    }


}
