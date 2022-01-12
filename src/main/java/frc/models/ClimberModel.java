package frc.models;

import org.xero1425.base.motors.SparkMaxMotorController;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.SimMotorController;

public class ClimberModel extends SimulationModel {
    private SimMotorController climb_ ;
    private SimMotorController traverse_ ;
    private double ticks_per_second_per_volt_ ;
    private double ticks_ ;
    
    public ClimberModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);
    }

    public boolean create() {
        climb_ = new SimMotorController(this, "climber") ;
        if (!climb_.createMotor())
            return false ;

        climb_.setEncoder(0.0);

        traverse_ = new SimMotorController(this, "traverse") ;
        if (!traverse_.createMotor())
            return false ;

        try {
            ticks_per_second_per_volt_ = getProperty("ticks_per_second_per_volt").getDouble();
        } catch (BadParameterTypeException e) {
            MessageLogger logger = getEngine().getMessageLogger() ;
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create model ").addQuoted(getModelName()).add(" instance ").addQuoted(getInstanceName()) ;
            logger.add(" - missing parameter ").addQuoted("ticks_per_second_per_volt").endMessage();
            return false ;
        }
        ticks_ = 0 ;

        setCreated();
        return true ;
    }

    public boolean processEvent(String name, SettingsValue value) {
        return false ;
    }

    public void run(double dt) {
        double power = climb_.getPower() + 0.001 ;
        double dist = power * ticks_per_second_per_volt_ * dt ;
        ticks_ += dist ;

        if (ticks_ < 0.0)
            ticks_ = 0.0 ;
            
        climb_.setEncoder(ticks_ / (double)SparkMaxMotorController.TicksPerRevolution) ;
    }
}
