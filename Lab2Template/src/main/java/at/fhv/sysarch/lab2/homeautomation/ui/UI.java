package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.*;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<Fridge.FridgeCommand> fridge;


    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, fridge));
    }

    private  UI(ActorContext<Void> context, ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);
        // TODO: implement actor and behavior as needed
        // TODO: move UI initialization to appropriate place
        this.airCondition = airCondition;
        this.tempSensor = tempSensor;
        this.fridge = fridge;
        new Thread(() -> { this.runCommandLine(); }).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";
        Stock stock= new Stock();


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {
                System.out.println("Temperature Command");
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(Double.valueOf(command[1]))));
            }
            if(command[0].equals("a")) {
                this.airCondition.tell(new AirCondition.PowerAirCondition(Optional.of(Boolean.valueOf(command[1]))));
            }
            if(command[0].equals("f")){
                Product p = null;
                LinkedList<Product> products = new LinkedList<>();

                //place order
                if(command[1].equals("p")){

                    if(command[2].equals("a")){
                        p = new Butter();
                    }
                    if(command[2].equals("b")){
                        p = new Milk();
                    }
                    if(command[2].equals("c")){
                        p = new Banana();
                    }

                    products.add(p);
                    this.fridge.tell(new Fridge.PlaceOrder(products, stock));

                 //get all products the fridge contains
                }else if (command[1].equals("g")){
                    this.fridge.tell(new Fridge.GetProducts());

                //get order history from fridge
                }else if (command[1].equals("h")){
                    this.fridge.tell(new Fridge.GetItemsFromOrderHistory());

                //consume a certain product from fridge
                }else if(command[1].equals("c")){
                    if(command[2].equals("a")){
                        p = new Butter();
                    }
                    if(command[2].equals("b")){
                        p = new Milk();
                    }
                    if(command[2].equals("c")){
                        p = new Banana();
                    }
                    this.fridge.tell(new Fridge.ConsumeProduct(p));
                }


            }


            // TODO: process Input
        }
        getContext().getLog().info("UI done");
    }
}
