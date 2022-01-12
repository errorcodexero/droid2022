package frc.droid.climber;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.actions.Action;
import org.xero1425.base.motorsubsystem.MotorPowerAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class ClimberMoveAction extends Action {
    
    private ClimberSubsystem sub_ ;
    private MotorPowerAction lift_ ;
    private double trav_ ;
    private String state_ ;

    public ClimberMoveAction(ClimberSubsystem sub, double lift, double trav, String state) {
        super(sub.getRobot().getMessageLogger());

        lift_ = new MotorPowerAction(sub.getLifter(), lift) ;
        trav_ = trav;
        sub_ = sub ;
        state_ = state ;
    }

    public ClimberMoveAction(ClimberSubsystem sub, String lift, String trav, String state)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        double power = sub.getSettingsValue(lift).getDouble() ;
        lift_ = new MotorPowerAction(sub.getLifter(), power) ;
        trav_ = sub.getSettingsValue(trav).getDouble() ;
        sub_ = sub ;
        state_ = state ;
    }    

    public ClimberMoveAction(ClimberSubsystem sub, String lift, double trav, String state)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        double power = sub.getSettingsValue(lift).getDouble() ;
        lift_ = new MotorPowerAction(sub.getLifter(), power) ;
        trav_ = trav ;
        sub_ = sub ;
        state_ = state ;
    }      

    public ClimberMoveAction(ClimberSubsystem sub, double lift, String trav, String state)
            throws BadParameterTypeException, MissingParameterException {
        super(sub.getRobot().getMessageLogger());

        lift_ = new MotorPowerAction(sub.getLifter(), lift) ;
        trav_ = sub.getSettingsValue(trav).getDouble() ;
        sub_ = sub ;
        state_ = state ;
    }      

    @Override
    public void start() throws Exception {
        super.start() ;
        sub_.setAction(lift_) ;
        sub_.setTraverserPower(trav_);
        setDone() ;

        sub_.putDashboard("ClimberState", DisplayType.Always, state_);
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ClimberMoveAction lifter=" + lift_.getPower() +
                " traverse=" + trav_;
    }

}