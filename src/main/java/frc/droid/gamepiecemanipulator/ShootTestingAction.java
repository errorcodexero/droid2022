package frc.droid.gamepiecemanipulator;

import frc.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToEmitAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorPrepareToReceiveAction;
import frc.droid.gamepiecemanipulator.conveyor.ConveyorReceiveAction;
import frc.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import frc.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import org.xero1425.base.actions.Action;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorRequestFailedException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;

public class ShootTestingAction extends Action {
    
    private GamePieceManipulatorSubsystem sub_ ;
    private State state_ ;
    private ShooterVelocityAction fire_ ;
    private ConveyorPrepareToReceiveAction prepare_receive_ ;
    private ConveyorPrepareToEmitAction prepare_emit_ ;
    private ConveyorReceiveAction receive_ ;
    private ConveyorEmitAction emit_ ;
    private double shoot_delay_ ;
    private double start_ ;
    private SimpleWidget widget_ ;
    
    public ShootTestingAction(GamePieceManipulatorSubsystem gp, ShooterSubsystem.HoodPosition pos) throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        sub_ = gp ;

        widget_ = makeWidget() ;
        fire_ = new ShooterVelocityAction(gp.getShooter(), 0, pos, true) ;
        prepare_receive_ = new ConveyorPrepareToReceiveAction(gp.getConveyor()) ;
        receive_ = new ConveyorReceiveAction(gp.getConveyor()) ;
        prepare_emit_ = new ConveyorPrepareToEmitAction(gp.getConveyor()) ;
        emit_ = new ConveyorEmitAction(gp.getConveyor()) ;

        shoot_delay_ = gp.getSettingsValue("shoottest:shoot_delay").getDouble() ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        state_ = State.WaitPrepareReceive ;
        fire_.setTarget(4000.0);
        sub_.getShooter().setAction(fire_, true) ;
        sub_.getConveyor().setAction(prepare_receive_, true) ;
    }

    @Override
    public void run() throws BadMotorRequestException, MotorRequestFailedException {
        double current = sub_.getShooter().getVelocity() ;
        double target = widget_.getEntry().getDouble(current) ;

        fire_.setTarget(target) ;

        MessageLogger logger = sub_.getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, sub_.getLoggerID());
        logger.add("current velocity ", current) ;
        logger.add("target velocity ", target) ;
        logger.endMessage();

        switch(state_) {
            case WaitPrepareReceive:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitReceive ;
                    sub_.getConveyor().setAction(receive_, true) ;
                }
                break ;

            case WaitReceive:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitPrepareShoot ;
                    sub_.getConveyor().setAction(prepare_emit_, true) ;
                }

                if (sub_.getConveyor().getBallCount() == 1) {
                    state_ = State.WaitPrepareShoot ;
                    sub_.getConveyor().setAction(prepare_emit_, true) ;
                }
                break ;

            case WaitPrepareShoot:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitShootDelay ;
                    start_ = sub_.getRobot().getTime() ;
                }
                break ;

            case WaitShootDelay:
                if (sub_.getRobot().getTime() - start_ > shoot_delay_) {
                    state_ = State.WaitShoot ;
                    sub_.getConveyor().setAction(emit_, true) ;
                }
                break ;

            case WaitShoot:
                if (!sub_.getConveyor().isBusy()) {
                    state_ = State.WaitPrepareReceive ;
                    sub_.getConveyor().setAction(prepare_receive_, true) ;
                }
                break ;            
        }
    }

    @Override
    public void cancel() {
        super.cancel() ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ShootTestingAction" ;
    }

    private SimpleWidget makeWidget() {
        SimpleWidget w = Shuffleboard.getTab("ShootTest2").add("Velocity", 0.0) ;
        return w.withWidget(BuiltInWidgets.kTextView) ;
    }

    private enum State {
        WaitPrepareReceive,
        WaitReceive,
        WaitPrepareShoot,
        WaitShootDelay,
        WaitShoot,        
    } ;

}