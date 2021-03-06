package frc.droid.gamepiecemanipulator.conveyor;

import java.util.List;

public abstract class BaseState {
    public BaseState() {
        name_ = null ;
    }

    public BaseState(String name) {
        name_ = name ;
    }

    public String getName() {
        return name_ ;
    }

    public abstract ConveyorStateStatus runState(ConveyorStateAction act) ;
    public abstract void addBranchTargets(List<String> targets) ;
    public abstract String humanReadableName() ;
    public void cancelState() {
    }

    private String name_ ;
}
