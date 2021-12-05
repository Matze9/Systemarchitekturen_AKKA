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
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.*;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Banana;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Butter;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Milk;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private  ActorRef<MediaStation.MediaCommand> mediaStation;
    private ActorRef<Fridge.FridgeCommand> fridge;

    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor, LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions, ActorRef<MediaStation.MediaCommand> mediaStation, ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new UI(context, tempSensor, weatherSensor, airConditions, mediaStation, fridge));

    }

    private  UI(ActorContext<Void> context, ActorRef<TemperatureSensor.TemperatureCommand> tempSensor , ActorRef<WeatherSensor.WeatherCommand> weatherSensor, LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions, ActorRef<MediaStation.MediaCommand> mediaStation, ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);

        this.airConditions = airConditions;
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
        this.mediaStation = mediaStation;
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

        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";
        Stock stock= new Stock();
//test

        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");

            //Change Temperature
            if(command[0].equals("t")) {
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(Double.valueOf(command[1]))));
            }

            if(command[0].equals("a")) {
                for (ActorRef<AirCondition.AirConditionCommand> aircon : airConditions){
                    aircon.tell(new AirCondition.PowerAirCondition(Optional.of(Boolean.valueOf(command[1])), command[2]));
                }

            }
            if(command[0].equals("w")){
                WeatherSensor.Weather newWeather;
                if(command[1].equalsIgnoreCase("sunny")){
                    this.weatherSensor.tell(new WeatherSensor.updateWeather(WeatherSensor.Weather.SUNNY));
                } else if (command[1].equalsIgnoreCase("cloudy")){
                    this.weatherSensor.tell(new WeatherSensor.updateWeather(WeatherSensor.Weather.CLOUDY));
                }
            }

            if(command[0].equals("ms")){
                if(command[1].equalsIgnoreCase("start")){
                    this.mediaStation.tell(new MediaStation.playMovie());
                } else if (command[1].equalsIgnoreCase("stop")){
                    this.mediaStation.tell(new MediaStation.stopMovie());
                }
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

                 //requests space left
                }else if(command[1].equals("s")){
                    this.fridge.tell(new Fridge.GetFridgeSpaceLeft());
                }else if(command[1].equals("w")){
                    this.fridge.tell(new Fridge.GetFridgeWeightLeft());
                }


            }

            // TODO: process Input
        }
        getContext().getLog().info("UI done");
    }
}
