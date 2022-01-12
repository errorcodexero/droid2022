package frc.droid.climber;

import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;

public class ClimberDeployAction extends MotorEncoderGotoAction {
    public ClimberDeployAction(LifterSubsystem lifter) throws Exception {
        super(lifter, "climb_height", true) ;
    }

    @Override
    public void start() throws Exception {
        getSubsystem().putDashboard("ClimberState", DisplayType.Always, "DEPLOYING");
        super.start() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ClimberDeployAction" ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }
}
