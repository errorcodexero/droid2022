package frc.droid.targettracker;
import frc.droid.droidlimelight.DroidLimeLightSubsystem;
import frc.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem;
import org.xero1425.base.limelight.LimeLightSubsystem.LedMode;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

//
// The purpose of the tracker class is to generate two things.  It generates
// the desired angle for the TURRET pid controller and it generates the distance
// from the robot camera to the target.  This goes to false if the lime light loses
// the target for an extended number of robot loops.
//
public class TargetTrackerSubsystem extends Subsystem {
    private boolean enabled_ ;
    private double desired_turret_angle_ ;
    private DroidLimeLightSubsystem ll_ ;
    private TurretSubsystem turret_ ;
    private double distance_ ;
    private int lost_count_ ;
    private int max_lost_count_ ;
    boolean has_target_ ;

    private double camera_offset_angle_ ;

    public static final String SubsystemName = "targettracker" ;

    public TargetTrackerSubsystem(Subsystem parent, DroidLimeLightSubsystem ll, TurretSubsystem turret)
            throws BadParameterTypeException, MissingParameterException {

        super(parent, SubsystemName);

        ll_ = ll ;
        turret_ = turret ;

        desired_turret_angle_ = 0.0 ;
        lost_count_ = 0 ;
        has_target_ = false;
        
        //
        // Camera offset angle is determined empirically and deals with any angular offset in the
        // mounting or manufacturing of the camera.
        //
        camera_offset_angle_ = getSettingsValue("camera_offset_angle").getDouble() ;

        //
        // This is the number of robot loops that the lime light can lose the target before we assume
        // the target is lost for firing.  When a ball is fired, it temporarily blocks the view of the target
        // for the limelight.  Therefore if we lose the target for short amounts of time, we just maintain the
        // values we last calculated until the target is seen again.  Only after this number of robot loops
        // without a target do we actually consider the target lost and stop the firing operation.
        //
        max_lost_count_ = getSettingsValue("lost_count").getInteger() ;

        //
        // Turn off the LEDs unless we are actually wanting to track a target
        //
        enable(true) ;
    }

    public void enable(boolean b) {
        b = true ;
        enabled_ = b ;

        if (b)
        {
            ll_.setLedMode(LedMode.ForceOn);
        }
        else
        {
            ll_.setLedMode(LedMode.ForceOff);
        }
    }

    public boolean hasTarget() {
        return has_target_ ;
    }

    public double getDesiredTurretAngle() {
        return desired_turret_angle_ ;
    }

    public double getDistance() {
        return distance_ ;
    }

    @Override
    public void computeMyState() {
        MessageLogger logger = getRobot().getMessageLogger() ;

        if (enabled_)
        {
            if (ll_.isTargetDetected())
            {
                distance_ = ll_.getDistance() ;
               
                double yaw = ll_.getYaw() - camera_offset_angle_ ;
                desired_turret_angle_ = -yaw + turret_.getPosition() ;
                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("yaw", yaw).add("distance", distance_) ;
                logger.add(" ll", ll_.getYaw()).add(" offset", camera_offset_angle_) ;
                logger.add(" tpos", turret_.getPosition()).add(" desired", desired_turret_angle_);
                logger.endMessage();

                has_target_ = true ;
                lost_count_ = 0 ;
            }
            else
            {
                lost_count_++ ;
                
                if (lost_count_ > max_lost_count_)
                    has_target_ = false ;

                logger.startMessage(MessageType.Debug, getLoggerID());
                logger.add("targettracker: lost target ").add(" lost count", lost_count_) ;
                logger.add(" has_target", has_target_).endMessage();
            }
        }
        else
        {
            //
            // If the target tracker is disabled, we set the desired angle to zero, which is
            // straight ahead.
            //
            distance_ = 0.0 ;
            desired_turret_angle_ = 0.0 ;
        }
    }
}
