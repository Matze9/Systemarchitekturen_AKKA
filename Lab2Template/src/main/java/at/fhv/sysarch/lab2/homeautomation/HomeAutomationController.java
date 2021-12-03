package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.TypedActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.blackboard.Blackboard;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

import java.util.LinkedList;

public class HomeAutomationController extends AbstractBehavior<Void>{
    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions = new LinkedList<>();
    private  ActorRef<MediaStation.MediaCommand> mediaStation;
    private ActorRef<Blackboard.BlackBoardCommand> blackBoard;
    private ActorRef<Fridge.FridgeCommand> fridge;
    private ActorRef<Blinds.BlindsCommand> blinds;

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        Stock stock = new Stock();
        System.out.println("Homeautomationcontroller started!");
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?

        //TODO:Devices

        this.fridge = getContext().spawn(Fridge.create(stock,"3", "1"), "Fridge");
        this.blinds = getContext().spawn(Blinds.create(), "Blinds");


        //TODO:Initialize Blackboard
        this.blackBoard = getContext().spawn(Blackboard.create(this.blinds), "Blackboard");

            //Add Airconditions to Blackboard
        this.airConditions.add(getContext().spawn(AirCondition.create("2", "1", this.blackBoard), "AirCondition1"));
        this.airConditions.add(getContext().spawn(AirCondition.create("2", "2", this.blackBoard), "AirCondition2"));
        this.airConditions.add(getContext().spawn(AirCondition.create("2", "3", this.blackBoard), "AirCondition3"));
        this.blackBoard.tell(new Blackboard.addAirconditionsList(airConditions));

            //Add Blinds to Blackboard


        //TODO:SENSORS
        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.blackBoard, "1", "1"), "temperatureSensor");
        this.weatherSensor = getContext().spawn(WeatherSensor.create(this.blackBoard), "weatherSensor");
        this.mediaStation = getContext().spawn(MediaStation.create(this.blackBoard), "mediaStation");

        //TODO: UI
        ActorRef<Void> ui = getContext().spawn(UI.create(this.tempSensor, this.weatherSensor, this.airConditions, this.mediaStation, this.fridge), "UI");

        System.out.println("Homeautomationcontroller started!");
        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
