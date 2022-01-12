package frc.droid.automodes;

import frc.droid.climber.ClimberMoveAction;
import frc.droid.climber.ClimberSubsystem;
import frc.droid.droidsubsystem.DroidRobotSubsystem;
import frc.droid.gamepiecemanipulator.FireAction;
import frc.droid.gamepiecemanipulator.GamePieceManipulatorSubsystem;
import frc.droid.gamepiecemanipulator.ShootTestingAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorOnAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorReceiveAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorSetBallCountAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorStopAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorSubsystem;
import frc.droid.gamepiecemanipulator.intake.CollectOffAction;
import frc.droid.gamepiecemanipulator.intake.CollectOnAction;
import frc.droid.gamepiecemanipulator.intake.IntakeSubsystem;
import frc.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import frc.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import frc.droid.gamepiecemanipulator.shooter.ShooterSubsystem.HoodPosition;
import frc.droid.targettracker.TargetTrackerSubsystem;
import frc.droid.turret.FollowTargetAction;
import frc.droid.turret.TurretSubsystem;
import org.xero1425.base.actions.DelayAction;
import org.xero1425.base.actions.ParallelAction;
import org.xero1425.base.actions.SequenceAction;
import org.xero1425.base.controllers.TestAutoMode;
import org.xero1425.base.motorsubsystem.MotorEncoderGotoAction;
import org.xero1425.base.motorsubsystem.MotorEncoderPowerAction;
import org.xero1425.base.motorsubsystem.MotorEncoderVelocityAction;
import org.xero1425.base.tankdrive.TankDrivePathFollowerAction;
import org.xero1425.base.tankdrive.TankDrivePowerAction;
import org.xero1425.base.tankdrive.TankDrivePurePursuitPathAction;
import org.xero1425.base.tankdrive.TankDriveRamseteAction;
import org.xero1425.base.tankdrive.TankDriveScrubCharAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;

//
// This is a test automode.  It is used to run tests on various aspects of
// the robot.  Each subsystem has a set of tests that ensure the motors are
// working and to also test higher level actions.  There are also tests to help
// with the tuning of the robot.
//
public class DroidTestAutoMode extends TestAutoMode {

    private ShootTestingAction shoot_ = null ;

    public DroidTestAutoMode(DroidAutoController ctrl)
            throws Exception {
        super(ctrl, "Droid-Test-Mode") ;

        ParallelAction parallel ;
        SequenceAction seq ;
        
        DroidRobotSubsystem droid = (DroidRobotSubsystem)ctrl.getRobot().getRobotSubsystem() ;
        TankDriveSubsystem db = droid.getTankDrive() ;
        GamePieceManipulatorSubsystem gp = droid.getGamePieceManipulator() ;
        IntakeSubsystem intake = droid.getGamePieceManipulator().getIntake() ;
        ConveyorSubsystem conveyor = droid.getGamePieceManipulator().getConveyor() ;
        ShooterSubsystem shooter = droid.getGamePieceManipulator().getShooter() ;
        TurretSubsystem turret = droid.getTurret() ;
        ClimberSubsystem climber = droid.getClimber() ;
        TargetTrackerSubsystem tracker = droid.getTracker() ;     
    

        switch(getTestNumber()) {
            //
            // Numbers 0 - 9 are for the driverbase
            //
            case 0:         // Drive straight, used to test and get Kv number
                addSubActionPair(db, new TankDrivePowerAction(db, getPower(), getPower(), getDuration()), true);
                break ;

            case 1:         // Drive curve to the right
                addSubActionPair(db, new TankDrivePowerAction(db, getPower(), getPower() / 2, getDuration()), true);
                break ;

            case 2:         // Drive curve to the left
                addSubActionPair(db, new TankDrivePowerAction(db, getPower() / 2, getPower(), getDuration()), true);
                break ;

            case 3:         // Rotate robot to get scrub factor
                addSubActionPair(db, new TankDriveScrubCharAction(db, getPower(), getPosition()), true);
                break ; 

            case 4:         // Run the path follower to follow a named path
                addSubActionPair(db, new TankDrivePathFollowerAction(db, getNameParam(), false), true) ;
                break ;

            case 5:         // Run the path follower to follow a named path
                addSubActionPair(db, new TankDrivePurePursuitPathAction(db, getNameParam(), false), true) ;
                break ;

            case 6:         // Run the path follower to follow a named path
                addSubActionPair(db, new TankDriveRamseteAction(db, getNameParam(), false), true) ;
                break ;                

            //
            // Numbers 10 - 19 are for the intake
            //
            case 10:        // Calculate the Kv number
                addSubActionPair(intake, new MotorEncoderPowerAction(intake, getPower(), getDuration()), true);
                break ;

            case 11:        // Test to goto position action
                addSubActionPair(intake, new MotorEncoderGotoAction(intake, getPosition(), true), true) ;
                break ;                

            case 12:        // Test the high level collect on and collect off action
                addSubActionPair(intake, new CollectOnAction(intake), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(intake, new CollectOffAction(intake), true);
                break ;

            //
            // Numbers 20 - 29 are for the conveyor
            //
            case 20:            // Run intake side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 1.0, 0.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 21:            // Run intake side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, -1.0, 0.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 22:            // Run shooter side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 1.0), true);                
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 23:            // Run shooter side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, -1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 24:            // Run shooter and intake side of conveyor in forward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 1.0, 1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 25:            // Run shooter and intake side of conveyor in backward direction
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, -1.0, -1.0), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));                
                addSubActionPair(conveyor, new ConveyorOnAction(conveyor, 0.0, 0.0), true);
                break ;

            case 26:            // Test collect path
                parallel = new ParallelAction(ctrl.getRobot().getMessageLogger(), ParallelAction.DonePolicy.All) ;
                addAction(parallel);
                parallel.addSubActionPair(intake, new CollectOnAction(intake), false);
                seq = new SequenceAction(ctrl.getRobot().getMessageLogger()) ;
                parallel.addAction(seq) ;
                seq.addSubActionPair(conveyor, new ConveyorPrepareToReceiveAction(conveyor), true);
                seq.addSubActionPair(conveyor, new ConveyorReceiveAction(conveyor), true);
                break ;

            case 27:            // Test shoot path
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorEmitAction(conveyor), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(conveyor, new ConveyorStopAction(conveyor), true);
                break ;

            //
            // Numbers 30 - 39 are for the shooter
            //
            case 30:            // Run the shooter at a fixed power for a fixed duration, gets Kf for velocity
                addSubActionPair(shooter, new MotorEncoderPowerAction(shooter, getPower(), getDuration()), true);
                break ;

            case 31:            // Run the base motor encoder velocity action
                addSubActionPair(shooter, new MotorEncoderVelocityAction(shooter, "testmode-31", getPower()), true) ;
                break ;

            case 32:            // Set shooter to fixed velocity (hood down)
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Down, true), true);
                break ;

            case 33:            // Set shooter to fixed velocity (hood up)
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Up, true), false);
                break ;
                
            case 34:            // Characterize the shooter, gets velocity from smartdashboard - hood up
                if (shoot_ == null)
                    shoot_ = new ShootTestingAction(gp, HoodPosition.Up) ;
                addSubActionPair(gp, shoot_, true) ;
                break ;

            case 35:            // Characterize the shooter, gets velocity from smartdashboard - hood down
                if (shoot_ == null)
                    shoot_ = new ShootTestingAction(gp, HoodPosition.Down) ;
                addSubActionPair(gp, shoot_, true) ;
                break ;

            case 36:            // Test the hood
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Up, false), false);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Down, false), false);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Up, false), false);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Down, false), false);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, 0.0, HoodPosition.Up, false), false);             
                break ;
                
            case 37:            // Manual shoot, hood down
                addSubActionPair(conveyor, new ConveyorSetBallCountAction(conveyor, 5), false);
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Down, true), false);
                addAction(new DelayAction(ctrl.getRobot(), 5.0));
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorEmitAction(conveyor), true);
                break ;

            case 38:            // Manual shoot, hood up
                addSubActionPair(conveyor, new ConveyorSetBallCountAction(conveyor, 5), false);
                addSubActionPair(shooter, new ShooterVelocityAction(shooter, getPower(), HoodPosition.Up, true), false);
                addAction(new DelayAction(ctrl.getRobot(), 5.0));
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorEmitAction(conveyor), true);
                break ;

            //
            // Numbers 40 - 49 are for the turret
            //
            case 40:               // Run the turret at a fixed power for a fixed duration, gets Kv for velocity
                addSubActionPair(turret, new MotorEncoderPowerAction(turret, getPower(), getDuration()), true);
                break ;

            case 41:                // Go to specific angle
                addSubActionPair(turret, new MotorEncoderGotoAction(turret, -15, false), true);
                addAction(new DelayAction(ctrl.getRobot(), 3.0));
                addSubActionPair(turret, new MotorEncoderGotoAction(turret, 15, false), true);
                break ;

            case 42:                // Follow the target
                addSubActionPair(turret, new FollowTargetAction(turret, tracker), true);
                break ;
                
            //
            // Numbers 50 - 59 are for the control panel spinner
            //                
            case 50:
                break ;

            //
            // Numbers 60 - 69 are for the climber
            //
            case 60:
                addSubActionPair(climber.getLifter(), new MotorEncoderPowerAction(climber.getLifter(), getPower(), getDuration()), true);
                break ;

            case 61:
                addSubActionPair(climber.getLifter(), new MotorEncoderGotoAction(climber.getLifter(), getPosition(), true), true);
                break ;

            case 62:
                addSubActionPair(climber, new ClimberMoveAction(climber, getPower(), 0.0, "TEST-START"), true);
                addAction(new DelayAction(ctrl.getRobot(), getDuration()));
                addSubActionPair(climber, new ClimberMoveAction(climber, 0.0, 0.0, "TEST-STOP"), true);                
                break ;

            case 63:
                addSubActionPair(climber, new ClimberMoveAction(climber, 0.0, getPower(), "TEST-START"), true);
                addAction(new DelayAction(ctrl.getRobot(), getDuration()));
                addSubActionPair(climber, new ClimberMoveAction(climber, 0.0, 0.0, "TEST-STOP"), true);                
                break ;

            // 100+ Whole robot test modes
            case 100:   // Complete shooting sequence
                addSubActionPair(turret, new FollowTargetAction(turret, tracker), false);
                addSubActionPair(intake, new CollectOnAction(intake), true);
                addSubActionPair(conveyor, new ConveyorPrepareToReceiveAction(conveyor), true);
                addSubActionPair(conveyor, new ConveyorReceiveAction(conveyor), true);
                addSubActionPair(intake, new CollectOffAction(intake), true);   
                addSubActionPair(conveyor, new ConveyorPrepareToEmitAction(conveyor), true);
                addSubActionPair(gp, new FireAction(gp, tracker, turret, db), true) ;
                break ;
        }
    }
}