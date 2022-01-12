package frc.droid.gamepiecemanipulator.conveyor;

import java.util.List;

public class DelayState extends BaseState {
    public DelayState(double delay) {
        active_ = false ;
        delay_time_ = delay ;
    }

    public DelayState(String label, double delay) {
        super(label) ;
        active_ = false ;
        delay_time_ = delay ;
    }    

    @Override
    public void addBranchTargets(List<String> targets) {
    }

    @Override
    public void cancelState() {
        active_ = false ;
    }

    @Override
    public ConveyorStateStatus runState(ConveyorStateAction act) {
        double now = act.getSubsystem().getRobot().getTime() ;
        ConveyorStateStatus st = ConveyorStateStatus.CurrentState ;

        if (active_ == false) {
            active_ = true ;
            start_time_ = now ;
        }
        else {
            double elapsed = now - start_time_ ;

            if (elapsed > delay_time_)
            {
                st = ConveyorStateStatus.NextState ;
                active_ = false ;
            }
        }

        return st ;
    }

    @Override
    public String humanReadableName() {
        return "DelayState " + delay_time_ ;
    }       

    private boolean active_ ;
    private double start_time_ ;
    private double delay_time_ ;
}