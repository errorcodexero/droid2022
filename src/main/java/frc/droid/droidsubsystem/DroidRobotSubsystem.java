package frc.droid.droidsubsystem;

import frc.droid.climber.ClimberSubsystem;
import frc.droid.droidlimelight.DroidLimeLightSubsystem;
import frc.droid.droidoi.DroidOISubsystem;
import frc.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import frc.droid.targettracker.TargetTrackerSubsystem;
import frc.droid.turret.TurretSubsystem;
import org.xero1425.base.RobotSubsystem;
import org.xero1425.base.XeroRobot;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

public class DroidRobotSubsystem extends RobotSubsystem {
    public final static String SubsystemName = "droid" ;
    public final static String TankdriveSubsystemName = "tankdrive" ;

    public DroidRobotSubsystem(XeroRobot robot, boolean climber) throws Exception {
        super(robot, SubsystemName) ;

        db_ = new TankDriveSubsystem(this, TankdriveSubsystemName, "tankdrive") ;
        addChild(db_) ;

        if (climber)
        {
            climber_ = new ClimberSubsystem(this) ;
            addChild(climber_) ;
        }
        else
        {
            climber_ = null ;
        }

        manip_ = new GamePieceManipulatorSubsystem(this, db_) ;
        addChild(manip_) ;

        turret_ = new TurretSubsystem(this) ;
        addChild(turret_) ;

        limelight_ = new DroidLimeLightSubsystem(this) ;
        addChild(limelight_) ;

        tracker_ = new TargetTrackerSubsystem(this, limelight_, turret_) ;
        addChild(tracker_) ;

        oi_ = new DroidOISubsystem(this, db_, climber) ;
        addChild(oi_) ;        
    }

    @Override
    public void run() throws Exception {
        super.run() ;
    }

    public TankDriveSubsystem getTankDrive() {
        return db_ ;
    }

    public DroidOISubsystem getOI() {
        return oi_ ;
    }

    public ClimberSubsystem getClimber() {
        return climber_ ;
    }

    public GamePieceManipulatorSubsystem getGamePieceManipulator() {
        return manip_ ;
    }

    public TurretSubsystem getTurret() {
        return turret_ ;
    }

    public DroidLimeLightSubsystem getLimeLight() {
        return limelight_ ;
    }

    public TargetTrackerSubsystem getTracker() {
        return tracker_ ;
    }

    private TankDriveSubsystem db_ ;
    private DroidOISubsystem oi_ ;
    private ClimberSubsystem climber_ ;
    private GamePieceManipulatorSubsystem manip_ ;
    private TurretSubsystem turret_ ;
    private DroidLimeLightSubsystem limelight_ ;
    private TargetTrackerSubsystem tracker_ ;
}