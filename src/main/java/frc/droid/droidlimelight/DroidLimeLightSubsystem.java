package frc.droid.droidlimelight;

import org.xero1425.base.Subsystem;
import org.xero1425.base.limelight.LimeLightSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;

public class DroidLimeLightSubsystem extends LimeLightSubsystem {
    private double distance_ ;
    private double yaw_ ;
    private double camera_angle_ ;
    private double camera_height_ ;
    private double target_height_ ;

    public static final String SubsystemName = "droid-limelight" ;

    public DroidLimeLightSubsystem(Subsystem parent) throws BadParameterTypeException, MissingParameterException {
        super(parent, SubsystemName) ;

        camera_angle_ = getSettingsValue("camera_angle").getDouble() ;
        camera_height_ = getSettingsValue("camera_height").getDouble() ;
        target_height_ = getSettingsValue("target_height").getDouble() ;
        distance_ = 0 ;

        setCamMode(CamMode.VisionProcessing) ;
        setLedMode(LedMode.ForceOff) ;
    }

    @Override
    public void computeMyState() {
        super.computeMyState();
        
        if (isLimeLightConnected() && isTargetDetected())
        {
            distance_ = (target_height_ - camera_height_) / Math.tan(Math.toRadians(camera_angle_ + getTY())) ;
            yaw_ = getTX() ;

            putDashboard("dll-distance", DisplayType.Verbose, distance_);
            putDashboard("dll-yaw", DisplayType.Verbose, yaw_) ;
        }
    }

    public double getDistance() {
        return distance_ ;
    }

    public double getYaw() {
        return yaw_ ;
    }
}