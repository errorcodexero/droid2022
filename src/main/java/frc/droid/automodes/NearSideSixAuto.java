package frc.droid.automodes;

//
// Start along the side of our trench at the start line against the edge of the field.
// Drive to the front edge of the trench and shoot the three loaded in the robot.  Drive
// through the trench and collect the first three balls in the trench.  Drive back to the front of
// the trench and score the three.
//
// Status: works as planned
//
public class NearSideSixAuto extends DroidAutoMode {
    public NearSideSixAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideSix") ;

        //
        // Initial ball count, 3 balls loaded into the robot at start
        //
        setInitialBallCount(3);

        //
        // The first drive and fire
        //
        driveAndFire("six_ball_auto_fire", false, 20.0) ;

        //
        // Now, collect new balls
        //
        driveAndCollect("six_ball_auto_collect", 2, 0.0) ;

        //
        // Now fire the new set of balls
        //
        driveAndFire("six_ball_auto_fire2", true, 0.0) ;
    }
}