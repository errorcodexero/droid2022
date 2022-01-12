package frc.droid.gamepiecemanipulator.conveyor;

import frc.droid.gamepiecemanipulator.conveyor.WaitForSensor.SensorEvent;

public class ConveyorPrepareToEmitAction extends ConveyorStateAction {
    private static final String DoneLabel = "done" ;
    private static final String SetStateLabel = "setstate" ;

    public ConveyorPrepareToEmitAction(ConveyorSubsystem sub) throws Exception {
        super(sub) ;

        prepare_emit_intake_power_ = sub.getSettingsValue("prepare_emit:intake_side_power").getDouble() ;
        prepare_emit_shooter_power_ = sub.getSettingsValue("prepare_emit:shooter_side_power").getDouble() ;

        BaseState[] states = new BaseState[] {
            new BranchState(DoneLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isEmpty(); }),

            new BranchState(DoneLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().isStagedForFire(); }),

            new BranchState(SetStateLabel, (ConveyorStateAction act) -> {
                return act.getSubsystem().getBallCount() == 5 ; }),

            new DoWorkState("set motors prepare emit power", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(prepare_emit_intake_power_, prepare_emit_shooter_power_) ;
                return ConveyorStateStatus.NextState; }),

            new WaitForSensor(ConveyorSensorThread.Sensor.D, SensorEvent.HIGH_TO_LOW),

            new DoWorkState(SetStateLabel, "set conveyor state", (ConveyorStateAction act) -> {
                act.getSubsystem().setStagedForCollect(false);
                act.getSubsystem().setStagedForFire(true);
                act.getSubsystem().setCollecting(false);
                return ConveyorStateStatus.NextState; }),            
                
            new DoWorkState(DoneLabel, "set motor power zero", (ConveyorStateAction act) -> {
                act.getSubsystem().setMotorsPower(0.0, 0.0) ;
                return ConveyorStateStatus.NextState; }),                
        } ;

        setStates(states);
    }

    @Override
    protected void conveyorActionStarted() {
    }

    @Override
    protected void conveyorActionRunning() {
    }

    @Override
    protected void conveyorActionFinished() {
    }

    public String toString(int indent) {
        return prefix(indent) + "ConveyorPrepareToEmitAction" ;
    }

    private double prepare_emit_intake_power_ ;
    private double prepare_emit_shooter_power_ ;
}