package frc.droid.automodes;

//
// Start on the side with the opponents trench.  Go get the two balls in the
// trench then drive to a spot where we can shoot all five.
//
// Status: This mode worked but was slower than desired because of the path
//         required to collect the two balls in the trench.  We need a collector
//         that can collect two balls concurrently.
//
public class FarSideAuto extends DroidAutoMode {
    public FarSideAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "FarSide") ;

        //
        // Initial ball count, 3 balls loaded into the robot at start
        //
        setInitialBallCount(3);
        
        //
        // Initialize the climber
        //
        initializeClimber() ;

        //
        // Collect the extra two balls
        //
        driveAndCollect("five_ball_auto_collect", 0.0, 2.0) ;

        //
        // Drive near the center to fire
        //
        driveAndFire("five_ball_auto_fire", true, 0.0) ;
    }
}