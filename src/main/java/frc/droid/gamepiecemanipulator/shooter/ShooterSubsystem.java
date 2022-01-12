package frc.droid.gamepiecemanipulator.shooter;

import edu.wpi.first.wpilibj.Servo;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class ShooterSubsystem extends MotorEncoderSubsystem {    
    private HoodPosition desired_ ;
    private HoodPosition actual_ ;
    private double change_time_ ;
    private boolean ready_to_fire_ ;
    private double hood_change_time_ ;
    private double hood_down_speed_ ;
    private double hood_up_pos_ ;
    private double hood_down_pos_ ;
    private Servo hood_servo_ ;
    private TankDriveSubsystem db_ ;

    public final static String SubsystemName = "shooter" ;

    public enum HoodPosition {
        Up,
        Down,
        Unknown
    } ;

    public ShooterSubsystem(Subsystem parent, TankDriveSubsystem db) throws Exception {
        super(parent, SubsystemName, false, 8) ;

        db_ = db ;

        int index = getSettingsValue("hw:hood:servo").getInteger() ;
        hood_servo_ = new Servo(index) ;

        hood_up_pos_ = getSettingsValue("hood:up").getDouble() ;
        hood_down_pos_ = getSettingsValue("hood:down").getDouble() ;
        hood_change_time_= getSettingsValue("hood:change_time").getDouble() ;
        hood_down_speed_= getSettingsValue("hood:down_speed").getDouble() ;

        getMotorController().setNeutralMode(MotorController.NeutralMode.Coast);


        change_time_ = getRobot().getTime() ;
        actual_ = HoodPosition.Unknown ;
        desired_ = HoodPosition.Down ;

        //
        // Set the correct velocity units for the shooter
        //
        double ticks_per_rev = 42 ;
        double seconds_per_minute = 60 ;
        double motor_to_shooter_gear_ratio  = 24.0 / 18.0 ;
        double factor =  seconds_per_minute / ticks_per_rev * motor_to_shooter_gear_ratio ;
        setVelocityConversion(factor);        
    }

    public void setHood(HoodPosition pos) {
        desired_ = pos ;
    }

    public HoodPosition getHood() {
        return actual_ ;
    }

    public boolean isHoodReady() {
        if (getRobot().getTime() - change_time_ > hood_change_time_)
            return true ;

        return false ;
    }

    public boolean isReadyToFire() {
        return ready_to_fire_ ;
    }

    public void setReadyToFire(boolean b) {
        ready_to_fire_ = b ;
    }

    @Override
    public void postHWInit() throws Exception {
        super.postHWInit();
        // getMotorController().setEncoderUpdateFrequncy(EncoderUpdateFrequency.Frequent);
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();
        putDashboard("s-input", DisplayType.Verbose, getMotorController().getInputVoltage()) ;

        putDashboard("hood", DisplayType.Always, hood_servo_.get());
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        updateHood() ;
    }

    private void setPhysicalHood(HoodPosition pos)
    {
        if (pos == HoodPosition.Down)
        {
            hood_servo_.set(hood_down_pos_) ;
            actual_ = pos  ;
        }
        else if (pos == HoodPosition.Up)
        {
            hood_servo_.set(hood_up_pos_) ;  
            actual_ = pos  ;              
        }

        change_time_ = getRobot().getTime() ;
    }

    private void updateHood() {
        if (db_.getVelocity() > hood_down_speed_)
            setPhysicalHood(HoodPosition.Down) ;
        else if (actual_ != desired_) {
            setPhysicalHood(desired_);
        }
    }

    protected double limitPower(double p) {
        if (Math.abs(p) < 0.1)
            p = 0.0 ;

        return p ;
    }
} ;