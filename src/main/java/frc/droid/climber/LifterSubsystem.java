package frc.droid.climber;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motorsubsystem.MotorEncoderSubsystem;

public class LifterSubsystem extends MotorEncoderSubsystem {
    
    private double low_power_height_ ;
    private double low_power_limit_ ;
    private boolean calibrated_ ;

    public LifterSubsystem(Subsystem parent, String name) throws Exception {
        super(parent, name, false);

        low_power_limit_ = getSettingsValue("low_power_limit").getDouble() ;
        low_power_height_ = getSettingsValue("low_power_height").getDouble() ;
        calibrated_ = false ;
    }

    public boolean isCalibarated() {
        return calibrated_ ;
    }

    @Override
    public void computeMyState() throws Exception {
        super.computeMyState(); 

        if (getAction() == null)
            putDashboard("ClimberState", DisplayType.Always, "IDLE") ;
    }

    protected void setCalibrated() {
        calibrated_ = true ;
        reset() ;
    }

    protected double limitPower(double power) {
        double ret = power ;

        ClimberSubsystem climber = (ClimberSubsystem)getParent() ;

        if (power < 0.0) {
            //
            // We are going down
            //
            if (climber.isInFieldMode())
            {
                if (getPosition() < 0)
                {
                    //
                    // We are at the bottom, do not go any further
                    //
                    ret = 0.0 ;
                }
                else if (getPosition() < low_power_height_)
                {
                    if (Math.abs(power) > low_power_limit_)
                        ret = Math.signum(power) * low_power_limit_ ;
                }
            }
            else
            {
                //
                // We are in the PIT mode here
                //
                if (Math.abs(power) > low_power_limit_)
                    ret = Math.signum(power) * low_power_limit_ ;
            }
        }
        else
        {
            //
            // We are going up
            //
            if (getPosition() > climber.getMaxHeight())
                ret = 0.0 ;
        }

        return ret ;
    }
}