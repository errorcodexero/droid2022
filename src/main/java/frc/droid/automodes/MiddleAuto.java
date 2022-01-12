package frc.droid.automodes;

import org.xero1425.base.tankdrive.TankDrivePathFollowerAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

//
// Start in the middle position aligned with the goal.  Score the three balls loaded
// into the robot and then back up to get off the starting line.
//
// Status: works as planned.
//
public class MiddleAuto extends DroidAutoMode {
    public MiddleAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "MiddleAuto") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ;        

        //
        // Initial ball count, 3 balls loaded into the robot at start
        //
        setInitialBallCount(3);

        //
        // Fire the three balls (note path = null means no driving)
        //
        driveAndFire(null, true, 0.0) ;

        //
        // Move forward a short distance to get off the initiation line
        //
        addSubActionPair(db, new TankDrivePathFollowerAction(db, "three_ball_auto_fire", true), true);
    }
}