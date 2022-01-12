package frc.droid.gamepiecemanipulator.conveyor ;

import frc.droid.gamepiecemanipulator.conveyor.WaitForSensor.SensorEvent;

public class ConveyorReceiveAction extends ConveyorStateAction {

    private final static String WaitForBallLabel = "waitForBall";
    private final static String FifthLabel = "fifth";
    private final static String DoneLabel = "done";
    private final static String TimeoutLabel = "timeout";

    public ConveyorReceiveAction(ConveyorSubsystem sub) throws Exception {
        super(sub);

        double timeout = sub.getSettingsValue("receive:timeout").getDouble();
        intake_motor_power_ = sub.getSettingsValue("receive:intake_side_power").getDouble() ;
        shooter_motor_power_ = sub.getSettingsValue("receive:shooter_side_power").getDouble() ;

        BaseState[] states = new BaseState[] { 
            new DoWorkState(WaitForBallLabel, "wait for ball", (ConveyorStateAction act) -> {
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState; }),

            new DoWorkState("turn off motors", (ConveyorStateAction act) -> {
                ConveyorReceiveAction rec = (ConveyorReceiveAction) act;
                rec.run_motors_ = false;
                return ConveyorStateStatus.NextState;
            }),

            new BranchState(DoneLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isFull();
            }),

            new AssertState(
                    "ConveyorReceiveAction called with balls out of position; was ConveyorPrepareToReceiveAction run?",
                    (ConveyorStateAction act) -> {
                        return act.getSubsystem().isStagedForCollect();
                    }),

            new WaitForSensor(ConveyorSensorThread.Sensor.A, SensorEvent.IS_HIGH),

            new DoWorkState("set collecting", (ConveyorStateAction act) -> {
                act.getSubsystem().setCollecting(true);
                return ConveyorStateStatus.NextState;
            }),

            new DelayState(0.03),

            new DoWorkState("turn on motors", (ConveyorStateAction act) -> {
                ConveyorReceiveAction rec = (ConveyorReceiveAction) act;
                rec.run_motors_ = true;
                return ConveyorStateStatus.NextState;
            }),

            new BranchState(FifthLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().getBallCount() == 4;
            }),

            new WaitForSensor(ConveyorSensorThread.Sensor.B, SensorEvent.IS_LOW),

            new WaitForSensor(ConveyorSensorThread.Sensor.B, SensorEvent.IS_HIGH, TimeoutLabel, timeout),

            new DoWorkState("increment ball count", (ConveyorStateAction act) -> {
                act.getSubsystem().incrementBallCount();
                return ConveyorStateStatus.NextState;
            }),

            //
            // This was in the C++ version, but it is not clear that it helps and seems it could
            // hurt.  I want to try this without the delay with the java code.
            //
            new DelayState(0.04),

            new GoToState(WaitForBallLabel),

            new WaitForSensor(FifthLabel, ConveyorSensorThread.Sensor.C, SensorEvent.IS_LOW),

            new WaitForSensor(ConveyorSensorThread.Sensor.C, SensorEvent.IS_HIGH, TimeoutLabel, timeout),

            new DoWorkState("set finishing false", (ConveyorStateAction act) -> {
                ConveyorReceiveAction rec = (ConveyorReceiveAction) act;
                rec.finishing_ = false;
                return ConveyorStateStatus.NextState;
            }),

            //
            // This was in the C++ version, but it is not clear that it helps and seems it could
            // hurt.  I want to try this without the delay with the java code.
            //
            new DelayState(0.03),

            new DoWorkState("increment ball count", (ConveyorStateAction act) -> {
                act.getSubsystem().incrementBallCount();
                return ConveyorStateStatus.NextState;
            }),

            new DoWorkState("set collecting to false", (ConveyorStateAction act) -> {
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState;
            }),

            new GoToState(DoneLabel),

            new DoWorkState(TimeoutLabel, "sensor timeout abort collect", (ConveyorStateAction act) -> {
                ConveyorReceiveAction rec = (ConveyorReceiveAction) act;
                rec.run_motors_ = false;
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState;
            }),

            new GoToState(WaitForBallLabel),

            new DoWorkState(DoneLabel, "set motor power zero", (ConveyorStateAction act) -> {
                ConveyorReceiveAction rec = (ConveyorReceiveAction) act;
                rec.finishing_ = false;
                rec.getSubsystem().setMotorsPower(0.0, 0.0);
                return ConveyorStateStatus.ActionDone ;} ),
        } ;

        setStates(states) ;
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "ConveyorReceiveAction" ;
    }

    @Override
    protected void conveyorActionStarted() {
        run_motors_ = false ;
        finishing_ = false ;
        getSubsystem().setMotorsPower(0.0, 0.0) ;
    }

    @Override
    protected void conveyorActionRunning() {
        if (run_motors_) {
            if (getSubsystem().isStagedForFire()) {
                getSubsystem().setMotorsPower(intake_motor_power_, 0.0);
            }
            else {
                getSubsystem().setMotorsPower(intake_motor_power_, shooter_motor_power_);
            }
        }
        else if (finishing_) {
            getSubsystem().setMotorsPower(-intake_motor_power_, -shooter_motor_power_);            
        }
        else {
            getSubsystem().setMotorsPower(0.0, 0.0) ;
        }
    }

    @Override
    protected void conveyorActionFinished() {
    }

    private boolean run_motors_ ;
    private boolean finishing_ ;
    private double intake_motor_power_ ;
    private double shooter_motor_power_ ;
}