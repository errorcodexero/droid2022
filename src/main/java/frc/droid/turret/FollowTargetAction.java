package frc.droid.turret;

import frc.droid.targettracker.TargetTrackerSubsystem;
import org.xero1425.base.motorsubsystem.MotorAction;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.PIDCtrl;

//
// This action is assigned to the turret in order to have the turret follow any target
// seen by the limelight.  This action never completes and will have to be canceled by assigning
// another action to the turret.
//
public class FollowTargetAction extends MotorAction {
    
    // The turret must be less than the threshold from the desired angle (as determined by the target tracker)
    // in order to be ready to fire
    private double threshold_ ;

    // The PID controller that actually calculates the power required to have the turret track the target
    private PIDCtrl pid_ ;

    // The turret subsystem
    private TurretSubsystem sub_ ;

    // The target tacker subsystem, used to find the desired turret angle
    private TargetTrackerSubsystem tracker_ ;

    private double desired_ ;
    private double error_ ;

    public FollowTargetAction(TurretSubsystem sub, TargetTrackerSubsystem tracker)
            throws BadParameterTypeException, MissingParameterException {
        super(sub);

        sub_ = sub ;
        tracker_ = tracker ;
        threshold_ = sub.getSettingsValue("fire_threshold").getDouble() ;
    }

    public double getDesired() {
        return desired_ ;
    }

    public double getError() {
        return error_ ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        //
        // This PID controller does the work of following the target
        //
        pid_ = new PIDCtrl(getSubsystem().getRobot().getSettingsSupplier(), "subsystems:turret:follow", false);

        //
        // Enable the target tracker, this lights up the LED lights and starts the flow of data
        //
        tracker_.enable(true);
    }

    @Override
    public void run() {
        //
        // Ask the target tracker what angle the turret should be at to 
        // point at the target.
        //
        desired_ = tracker_.getDesiredTurretAngle() ;

        //
        // Update the turret motor power based on the current position of the turret and
        // the desired positon of the turret.
        //
        double out = pid_.getOutput(desired_, sub_.getPosition(), sub_.getRobot().getDeltaTime()) ;
        sub_.setPower(out) ;

        //
        // Determine if the turret is close enough to the desired position to enable 
        // firing of the balls
        //
        double error_ = Math.abs(desired_ - sub_.getPosition()) ;
        boolean ready = error_ < threshold_ ;
        sub_.setReadyToFire(ready) ;

        //
        // Print debug messages for this action
        //
        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, sub_.getLoggerID()) ;
        logger.add("FollowTargetAction:") ;
        logger.add(" desired", desired_) ;
        logger.add(" position", sub_.getPosition()) ;
        logger.add(" error", error_) ;
        logger.add(" output", out) ;
        logger.add(" ready", ready) ;
        logger.endMessage();
    }

    @Override
    public void cancel() {
        super.cancel() ;

        //
        // Disable the target tracker, this turns off the LED lights
        //
        tracker_.enable(false);

        //
        // Set the turret power to zero
        //
        sub_.setPower(0.0) ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "FollowTargetAction" ;
    }

}