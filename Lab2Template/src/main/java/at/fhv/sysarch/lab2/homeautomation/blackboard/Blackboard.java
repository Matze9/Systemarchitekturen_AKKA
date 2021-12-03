package at.fhv.sysarch.lab2.homeautomation.blackboard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class Blackboard extends AbstractBehavior<Blackboard.BlackBoardCommand> {

    //////////////COMMANDS//////////////////
    public interface BlackBoardCommand {}


    public static class updateTemperature implements BlackBoardCommand {
        public final Double temperature;

        public updateTemperature (Double newTemperature){
            this.temperature = newTemperature;
        }
    }

    public static class updateWeather implements BlackBoardCommand {
        public final WeatherSensor.Weather weather;

        public updateWeather (WeatherSensor.Weather newWeather){
            this.weather = newWeather;
        }
    }

    public static class startStopMovie implements BlackBoardCommand {
        public final boolean isPlaying;

        public startStopMovie (boolean isPlaying){
            this.isPlaying = isPlaying;
        }
    }

    public static class setAirconditionState implements BlackBoardCommand {
        public final String airconditionID;
        public final boolean airConditionIsActive;

        public setAirconditionState (boolean airConditionIsActive, String airconditionID){
            this.airConditionIsActive = airConditionIsActive;
            this.airconditionID = airconditionID;
        }
    }

    public static class addAirconditionsList implements BlackBoardCommand {
        public final LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions;

        public addAirconditionsList (LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditions){
            this.airConditions = airConditions;
        }
    }

    public static class addBlindsList implements BlackBoardCommand {
        public final LinkedList<ActorRef<Blinds.BlindsCommand>> blindsList;

        public addBlindsList ( LinkedList<ActorRef<Blinds.BlindsCommand>> blindsList){
            this.blindsList = blindsList;
        }
    }

    public static class changeBlindState implements BlackBoardCommand {
        public final String blindsId;
        public final Blinds.BlindState blindState;

        public changeBlindState (String blindsId, Blinds.BlindState blindState){
            this.blindState = blindState;
            this.blindsId = blindsId;
        }
    }

    ////////////TODO:BLACKBOARD/////////////////////

    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<Blinds.BlindsCommand> blinds;

    private LinkedList<ActorRef<AirCondition.AirConditionCommand>> airConditionsList = new LinkedList<>();
    private HashMap<String, Boolean> airConditionStates = new HashMap<>();

    private LinkedList<ActorRef<Blinds.BlindsCommand>> blindsList = new LinkedList<>();
    private HashMap<String, Blinds.BlindState> blindStates = new HashMap<>();

    private Double temperature = 0.0;
    private WeatherSensor.Weather weather = WeatherSensor.Weather.SUNNY;
    private boolean movieIsPlaing = false;



    public static Behavior<Blackboard.BlackBoardCommand> create(){
        return Behaviors.setup(context -> new Blackboard(context));
    }

    private Blackboard(ActorContext<BlackBoardCommand> context) {
        super(context);
        getContext().getLog().info("Blackboard: Active");
    }


    @Override
    public Receive<BlackBoardCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(updateTemperature.class, this::onUpdateTemperature)
                .onMessage(updateWeather.class, this::onUpdateWeather)
                .onMessage(startStopMovie.class, this::onStartStopMovie)
                .onMessage(changeBlindState.class, this::onchangeBlindState)
                .onMessage(setAirconditionState.class, this::onsetAirconditionState)
                .onMessage(addAirconditionsList.class, this::onaddAirConditionsList)
                .onMessage(addBlindsList.class, this::onaddBlindsList)
                .build();
    }

    private Behavior<BlackBoardCommand> onaddAirConditionsList (addAirconditionsList command){
        this.airConditionsList = command.airConditions;
        getContext().getLog().info("Blackboard: "+ command.airConditions.size() + " Airconditions registred");
        return this;
    }

    private Behavior<BlackBoardCommand> onaddBlindsList (addBlindsList command){
        this.blindsList = command.blindsList;
        getContext().getLog().info("Blackboard: "+ command.blindsList.size() +" Blinds registred");
        return this;
    }

    private Behavior<BlackBoardCommand> onStartStopMovie (startStopMovie command){
        movieIsPlaing = command.isPlaying;
        getContext().getLog().info("Blackboard: Updated movie is playing to " + this.movieIsPlaing);
        getContext().getLog().info("Blackboard: Current weather = " + this.weather.toString());

        for (ActorRef<Blinds.BlindsCommand>  blinds : this.blindsList){
            blinds.tell(new Blinds.HandleStateChange(this.weather, this.movieIsPlaing));
        }

       // this.blinds.tell(new Blinds.HandleStateChange(this.weather, command.isPlaying));
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateTemperature(updateTemperature command){
        temperature = command.temperature;
        getContext().getLog().info("Blackboard: updated Temperatur to " + temperature);
        for (ActorRef<AirCondition.AirConditionCommand> airCon : airConditionsList){
            airCon.tell(new AirCondition.EnrichedTemperature(Optional.ofNullable(command.temperature), Optional.of("Celsius")));
        }
       // this.airCondition.tell(new AirCondition.EnrichedTemperature(Optional.ofNullable(command.temperature), Optional.of("Celsius")));
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateWeather (updateWeather command){
        this.weather = command.weather;
        getContext().getLog().info("Blackboard: updated Weather to " + command.weather.toString());
        getContext().getLog().info("Blackboard: Movie is Playing = " + this.movieIsPlaing);

        for (ActorRef<Blinds.BlindsCommand>  blinds : this.blindsList){
            blinds.tell(new Blinds.HandleStateChange(this.weather, this.movieIsPlaing));
        }
      //  this.blinds.tell(new Blinds.HandleStateChange(this.weather, this.movieIsPlaing));
        return this;
    }

    private Behavior<BlackBoardCommand> onchangeBlindState (changeBlindState command){
//        this.blindState = command.blindState;
        this.blindStates.put(command.blindsId, command.blindState);
        getContext().getLog().info("Blackboard: updated Blindstate of Blind Nr. " + command.blindsId +" to " + command.blindState);
        return this;
    }

    private Behavior<BlackBoardCommand> onsetAirconditionState (setAirconditionState command){
        this.airConditionStates.put(command.airconditionID, command.airConditionIsActive);
        getContext().getLog().info("Blackboard: set State of  Aircondition nr. "+ command.airconditionID + " to " + command.airConditionIsActive);
        return this;
    }



}
