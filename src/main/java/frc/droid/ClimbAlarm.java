package frc.droid;

import org.xero1425.base.alarms.AlarmSounder;
import org.xero1425.base.oi.Gamepad;

public class ClimbAlarm extends AlarmSounder {
    public ClimbAlarm(Gamepad gp) {
        super("ClimbAlarm") ;
        gp_ = gp ;
    }

    @Override
    public void signalAlarm() {
        gp_.rumble(1.0, 5.0);
    }

    private Gamepad gp_ ;
}
