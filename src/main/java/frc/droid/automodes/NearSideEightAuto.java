package frc.droid.automodes;

//
// Start along the side of our trench at the start line against the edge of the field.
// Drive to the front edge of the trench and shoot the three loaded in the robot.  Drive
// through the trench and collect the five balls in the trench.  Drive back to the front of
// the trench and score the five.
//
// Status: This mode routinely collects and scores six balls, the original three in the robot
//         and the first three in the trench.  It has a hard time with the two on the other side
//         of the control panel.  We need a collector that can collect two balls concurrently.
//
public class NearSideEightAuto extends DroidAutoMode {
    public NearSideEightAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideEight") ;


        //
        // Initial ball count, 3 balls loaded into the robot at start
        //
        setInitialBallCount(3);

        //
        // Initialize the climber
        //
        initializeClimber() ;

        //
        // The first drive and fire
        //
        driveAndFire("eight_ball_auto_fire", false, 20.0) ;

        //
        // Now, collect new balls
        //
        driveAndCollect("eight_ball_auto_collect", 0.5, 0.0) ;

        //
        // Now fire the new set of balls
        //
        driveAndFire("eight_ball_auto_fire2", true, 0.0) ;
    }
}