package frc.droid.climber;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import org.xero1425.base.LoopType;
import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;

public class ClimberSubsystem extends Subsystem {
    
    private LifterSubsystem lifter_ ;
    private PWMSparkMax traverser_ ;
    private double max_height_ ;
    private boolean field_mode_ ;

    public static final String SubsystemName = "climber" ;

    public ClimberSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName);

        max_height_ = getSettingsValue("max_height").getDouble();
        lifter_ = new LifterSubsystem(this, "lifter");
        lifter_.getMotorController().setCurrentLimit(40);
        lifter_.getMotorController().setNeutralMode(MotorController.NeutralMode.Brake);
        lifter_.getMotorController().resetEncoder();
        addChild(lifter_);

        int travid = getSettingsValue("hw:traverser:pwmid").getInteger();
        traverser_ = new PWMSparkMax(travid);
    }

    public void setTraverserPower(double p) {
        traverser_.set(p);
    }

    public double getMaxHeight() {
        return max_height_;
    }

    public boolean isInFieldMode() {
        return field_mode_ ;
    }

    public LifterSubsystem getLifter() {
        return lifter_ ;
    }

    @Override
    public void init(LoopType ltype) {
        super.init(ltype) ;

        boolean b ;
        try {
            b = getSettingsValue("force_field_mode").getBoolean() ;
        }
        catch(Exception ex) {
            b = true ;
        }
        
        field_mode_ = DriverStation.isFMSAttached() || b ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState();

        putDashboard("climber", DisplayType.Always, lifter_.getPosition());
    }

}