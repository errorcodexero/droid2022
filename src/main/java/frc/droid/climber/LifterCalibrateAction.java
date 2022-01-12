package frc.droid.climber;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.actions.Action;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.PIDCtrl;

public class LifterCalibrateAction extends Action {
    
    private enum State {
        DownSlowly,
        Holding
    } ;

    private LifterSubsystem sub_ ;
    private State state_ ;
    private double down_power_ ;
    private double threshold_ ;
    private double [] encoders_ ;
    private int samples_ ;
    private int captured_ ;
    private PIDCtrl pid_ ;

    public LifterCalibrateAction(LifterSubsystem lifter) throws Exception {
        super(lifter.getRobot().getMessageLogger());

        sub_ = lifter ;
        samples_ = lifter.getSettingsValue("calibrate:samples").getInteger() ;
        captured_ = 0 ;
        encoders_ = new double[samples_] ;
        down_power_ = lifter.getSettingsValue("calibrate:down_power").getDouble() ;
        if (down_power_ >= 0.0)
            throw new Exception("lifter calibrate down power must be negative") ;

        threshold_ = lifter.getSettingsValue("calibrate:threshold").getDouble() ;
        pid_ = new PIDCtrl(lifter.getRobot().getSettingsSupplier(), "subsystems:lifter:stay", false) ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        if (sub_.isCalibarated()) {
            state_ = State.Holding ;
            sub_.putDashboard("ClimberState", DisplayType.Always, "KEEPDOWN");
        }
        else {
            sub_.putDashboard("ClimberState", DisplayType.Always, "CALIBRATING");
            captured_ = 0 ;
            state_ = State.DownSlowly ;
            sub_.setPower(down_power_) ;
        }
    }

    @Override
    public void run() {
        switch(state_)
        {
            case DownSlowly:
                if (addEncoderPosition(sub_.getPosition())) {
                    sub_.setCalibrated();
                    state_ = State.Holding ;
                    sub_.putDashboard("ClimberState", DisplayType.Always, "KEEPDOWN");
                }
                break ;
            case Holding:
                double out = pid_.getOutput(0, sub_.getPosition(), sub_.getRobot().getDeltaTime()) ;
                MessageLogger logger = sub_.getRobot().getMessageLogger() ;
                logger.startMessage(MessageType.Debug, sub_.getLoggerID()) ;
                logger.add("out", out) ;
                logger.add("pos", sub_.getPosition()) ;
                logger.endMessage(); ;
                sub_.setPower(out) ;
                break ;
        }
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "LifterCalibrationAction" ;
    }

    private boolean checkForStopped() {
        double vmax = encoders_[0] ;
        double vmin = encoders_[0] ;

        for(int i = 1 ; i < samples_ ; i++)
        {
            if (encoders_[i] < vmin)
                vmin = encoders_[i] ;

            if (encoders_[i] > vmax)
                vmax = encoders_[i] ;
        }

        return vmax - vmin < Math.abs(threshold_) ;        
    }

    private boolean addEncoderPosition(double pos) {
        boolean ret = false ;

        if (captured_ == samples_) {
            for(int i = samples_ - 1 ; i > 0 ; i--)
                encoders_[i] = encoders_[i - 1] ;
            encoders_[0] = pos ;
            ret = checkForStopped() ;
        }
        else {
            for(int i = captured_ - 1 ; i > 0 ; i--)
                encoders_[i] = encoders_[i - 1] ;
            encoders_[0] = pos ;
            
            captured_++ ;
        }
        return ret ;
    }
}