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
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

public class HomeAutomationController extends AbstractBehavior<Void>{
    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private  ActorRef<AirCondition.AirConditionCommand> airCondition;
    private  ActorRef<MediaStation.MediaCommand> mediaStation;
    private ActorRef<Blackboard.BlackBoardCommand> blackBoard;
    private ActorRef<Fridge.FridgeCommand> fridge;

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?

        //TODO:Devices
        this.airCondition = getContext().spawn(AirCondition.create("2", "1"), "AirCondition");


        //TODO:BLACKBOARD
        this.blackBoard = getContext().spawn(Blackboard.create(this.airCondition), "Blackboard");

        //TODO:SENSORS
        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.airCondition, this.blackBoard, "1", "1"), "temperatureSensor");
        this.weatherSensor = getContext().spawn(WeatherSensor.create(this.blackBoard), "weatherSensor");
        this.mediaStation = getContext().spawn(MediaStation.create(this.blackBoard), "mediaStation");

        ActorRef<Void> ui = getContext().spawn(UI.create(this.tempSensor, this.weatherSensor, this.airCondition, this.mediaStation), "UI");

        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.airCondition, "1", "1"), "temperatureSensor");
        this.fridge = getContext().spawn(Fridge.create(this.fridge, "3", "1"), "Fridge");
        ActorRef<Void> ui = getContext().spawn(UI.create(this.tempSensor, this.airCondition, this.fridge), "UI");
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
