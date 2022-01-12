package frc.droid.gamepiecemanipulator;

import frc.droid.gamepiecemanipulator.conveyor.ConveyorEmitAction;
import frc.droid.gamepiecemanipulator.shooter.ShooterSubsystem;
import frc.droid.gamepiecemanipulator.shooter.ShooterVelocityAction;
import frc.droid.gamepiecemanipulator.shooter.ShooterSubsystem.HoodPosition;
import frc.droid.targettracker.TargetTrackerSubsystem;
import frc.droid.turret.TurretSubsystem;
import org.xero1425.base.Subsystem.DisplayType;
import org.xero1425.base.actions.Action;
import org.xero1425.base.motors.BadMotorRequestException;
import org.xero1425.base.motors.MotorRequestFailedException;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.base.utils.PieceWiseLinear;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;

public class FireAction extends Action {

    static public final String moduleName = "fireaction" ;

    static private int logger_id_ = -1 ;
    
    private GamePieceManipulatorSubsystem sub_ ;
    private TargetTrackerSubsystem tracker_ ;
    private TurretSubsystem turret_ ;
    private TankDriveSubsystem db_ ;

    ShooterSubsystem.HoodPosition hood_pos_ ;

    private boolean is_firing_ ;
    private double db_velocity_threshold_ ;

    private int event_ ;

    double power_port_power_ ;

    private double hood_down_a_ ;
    private double hood_down_b_ ;
    private double hood_down_c_ ;
    private double hood_down_d_ ;
    private double hood_down_e_ ;
    
    private double hood_up_a_ ;
    private double hood_up_b_ ;
    private double hood_up_c_ ;
    private double hood_up_d_ ;
    private double hood_up_e_ ;

    private double green_velo_ ;
    private double blue_velo_ ;
    private double red_velo_ ;
    private double yellow_velo_ ;

    private double max_hood_up_distance_ ;
    private double min_hood_down_distance_ ;

    private ShooterVelocityAction shooter_velocity_action_ ;
    private ShooterVelocityAction shooter_stop_action_ ;
    private ConveyorEmitAction emit_action_ ;

    private PieceWiseLinear pwl_down_ ;
    private PieceWiseLinear pwl_up_ ;

    private double prev_good_dist_ ;
    private boolean prev_good_dist_valid_ ;
    private int prev_good_dist_count_ ;

    private double start_time_ ;
    private int plot_id_ ;
    static final String[] plot_columns_ = { 
        "time",

        "is_firing",
        "ready",
        "ready_except_shooter",

        "limelight_ready",
        "turret_ready",
        "shooter_ready",
        "drivebase_ready",
    } ;
    
    public FireAction(GamePieceManipulatorSubsystem gp, TargetTrackerSubsystem tracker, 
                    TurretSubsystem turret, TankDriveSubsystem db) throws Exception {
        super(gp.getRobot().getMessageLogger()) ;

        if (logger_id_ == -1)
        {
            logger_id_ = db.getRobot().getMessageLogger().registerSubsystem(moduleName) ;
        }

        sub_ = gp ;
        tracker_ = tracker ;
        turret_ = turret ;
        db_ = db ;

        shooter_velocity_action_ = new ShooterVelocityAction(gp.getShooter(), 5700.0, ShooterSubsystem.HoodPosition.Down, true) ;
        shooter_stop_action_ = new ShooterVelocityAction(gp.getShooter(), 0.0, ShooterSubsystem.HoodPosition.Down, false) ;
        emit_action_ = new ConveyorEmitAction(gp.getConveyor()) ;

        db_velocity_threshold_ = gp.getSettingsValue("fire:max_drivebase_velocity").getDouble() ;

        event_ = gp.getSettingsValue("event").getInteger() ;
        power_port_power_ = gp.getSettingsValue("power_port_power").getDouble() ;

        green_velo_ = gp.getSettingsValue("green").getDouble();
        blue_velo_ = gp.getSettingsValue("blue").getDouble();
        red_velo_ = gp.getSettingsValue("red").getDouble();
        yellow_velo_ = gp.getSettingsValue("yellow").getDouble();

        hood_down_a_ = gp.getSettingsValue("aim:polynomial:hood_down:a").getDouble() ;
        hood_down_b_ = gp.getSettingsValue("aim:polynomial:hood_down:b").getDouble() ;
        hood_down_c_ = gp.getSettingsValue("aim:polynomial:hood_down:c").getDouble() ;
        hood_down_d_ = gp.getSettingsValue("aim:polynomial:hood_down:d").getDouble() ;
        hood_down_e_ = gp.getSettingsValue("aim:polynomial:hood_down:e").getDouble() ;

        hood_up_a_ = gp.getSettingsValue("aim:polynomial:hood_up:a").getDouble() ;
        hood_up_b_ = gp.getSettingsValue("aim:polynomial:hood_up:b").getDouble() ;
        hood_up_c_ = gp.getSettingsValue("aim:polynomial:hood_up:c").getDouble() ;
        hood_up_d_ = gp.getSettingsValue("aim:polynomial:hood_up:d").getDouble() ;
        hood_up_e_ = gp.getSettingsValue("aim:polynomial:hood_up:e").getDouble() ;

        max_hood_up_distance_ = gp.getSettingsValue("aim:max_hood_up").getDouble() ;
        min_hood_down_distance_ = gp.getSettingsValue("aim:min_hood_down").getDouble() ;

        plot_id_ = gp.initPlot("FireAction") ;

        hood_pos_ = HoodPosition.Down ;        

        pwl_down_ = new PieceWiseLinear(sub_.getRobot().getSettingsSupplier(), "subsystems:gamepiecemanipulator:aim:pwl:hood_down") ;
        pwl_up_ = new PieceWiseLinear(sub_.getRobot().getSettingsSupplier(), "subsystems:gamepiecemanipulator:aim:pwl:hood_up") ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;

        is_firing_ = false ;
        shooter_velocity_action_.setReadyFlagEnabled(false);
        sub_.getShooter().setAction(shooter_velocity_action_, true) ;

        start_time_ = sub_.getRobot().getTime() ;
        sub_.startPlot(plot_id_, plot_columns_) ;

        prev_good_dist_valid_ = false ;
        prev_good_dist_ = 0.0 ;
    }

    @Override
    public void run() throws BadMotorRequestException, MotorRequestFailedException {
        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        ShooterSubsystem shooter = sub_.getShooter() ;

        boolean tracker_ready = tracker_.hasTarget() ;
        boolean turret_ready = turret_.isReadyToFire() ;
        boolean shooter_ready = shooter.isReadyToFire() ;
        boolean hood_ready_ = shooter.isHoodReady() ;
        boolean db_ready = (Math.abs(db_.getVelocity()) < db_velocity_threshold_) ;

        boolean ready_to_fire_except_shooter = tracker_ready && db_ready && turret_ready && hood_ready_ ;
        boolean ready_to_fire = tracker_ready && db_ready && turret_ready && shooter_ready && hood_ready_ ;

        sub_.putDashboard("tracker-ready", DisplayType.Verbose, tracker_ready) ;
        sub_.putDashboard("turret-ready", DisplayType.Verbose, turret_ready) ;
        sub_.putDashboard("shooter-ready", DisplayType.Verbose, shooter_ready);
        sub_.putDashboard("db-ready", DisplayType.Verbose, db_ready) ;
        sub_.putDashboard("rtf", DisplayType.Verbose, ready_to_fire);
        sub_.putDashboard("isfiring", DisplayType.Verbose, is_firing_);

        if (tracker_ready)
        {
            setTargetVelocity(); 
        }

        if (is_firing_) {
            if (sub_.getConveyor().isEmpty() && !sub_.getConveyor().isBusy()) {
                //
                // We are out of balls
                //
                sub_.endPlot(plot_id_);
                setDone() ;
                stopChildActions() ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: stopped firing, out of balls") ;
                logger.endMessage();

                //
                // When we are out of balls, stop the shooter motor
                //
                sub_.getShooter().setAction(shooter_stop_action_, true) ;
            }
            else if (!ready_to_fire) {
                //
                // We lost the target or the driver started driving or we got bumped and
                // are no longer aiming at the target
                // 
                emit_action_.stopFiring();
                is_firing_ = false ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: stopped firing, lost target") ;
                logger.endMessage();   
            }
        }
        else {
            if (sub_.getConveyor().isEmpty()) {
                sub_.endPlot(plot_id_);
                setDone() ;
                stopChildActions() ;  

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: out of balls, completing action") ;
                logger.endMessage();    
            }
            else if (ready_to_fire && !sub_.getConveyor().isBusy()) {
                sub_.getConveyor().setAction(emit_action_, true);
                is_firing_ = true ;

                logger.startMessage(MessageType.Debug, logger_id_) ;
                logger.add("fire-action: fire away ... !!!") ;
                logger.endMessage();                   
            }
        }

        logger.startMessage(MessageType.Debug, logger_id_) ;
        logger.add("fire-action:") ;
        logger.add("tracker", tracker_ready) ;
        logger.add("turret", turret_ready) ;
        logger.add("shooter", shooter_ready) ;
        logger.add("drivebase", db_ready) ;
        logger.add("hood", hood_pos_.toString()) ;
        logger.add("balls", sub_.getConveyor().getBallCount()) ;
        logger.endMessage();

        Double[] data = new Double[plot_columns_.length] ;
        data[0] = sub_.getRobot().getTime() - start_time_ ;
        data[1] = is_firing_ ? 1.0 : 0.0 ;
        data[2] = ready_to_fire ? 1.1 : 0.0 ;
        data[3] = ready_to_fire_except_shooter ? 1.2 : 0.0 ;
        data[4] = tracker_ready ? 1.3 : 0.0 ;
        data[5] = turret_ready ? 1.4 : 0.0 ;
        data[6] = shooter_ready ? 1.5 : 0.0 ;
        data[7] = db_ready ? 1.6 : 0.0 ;

        sub_.addPlotData(plot_id_, data);

        if (sub_.getConveyor().isEmpty()) {
            sub_.endPlot(plot_id_) ;
            setDone() ;            
            stopChildActions() ;
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        sub_.endPlot(plot_id_);
        stopChildActions();
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "FireAction" ;
    }

    private void stopChildActions() {
        shooter_velocity_action_.cancel() ;
        emit_action_.cancel() ;
    }

    private double getTargetVelocity(double dist, HoodPosition pos) {
        double ret = 0 ;

        if (event_ == 0)
            ret = getTargetVelocityPoly(dist, pos) ;
        else if (event_ == 1)
            ret = getTargetVelocityAccuracy2d(dist, pos);
        else if (event_ == 2)
            ret = getTargetVelocityPowerPort3d(dist, pos) ;
        else if (event_ == 3)
            ret = getTargetVelocityPWL(dist, pos) ;

        return ret ;
    }

    private double getTargetVelocityPoly(double dist, HoodPosition hood) {
        double target ;
        if (hood_pos_ == HoodPosition.Down) {
            //
            // Fit to a fifth order polynomial
            //
            target = hood_down_a_ * dist * dist * dist * dist + hood_down_b_ * dist * dist * dist + hood_down_c_ * dist * dist + hood_down_d_ * dist + hood_down_e_ ;
        }
        else {
            //
            // Fit to a fifth order polynomial
            //
            target = hood_up_a_ * dist * dist * dist * dist + hood_up_b_ * dist * dist * dist + hood_up_c_ * dist * dist + hood_up_d_ * dist + hood_up_e_ ;
        }

        return target ;
    }

    private double getTargetVelocityAccuracy2d(double dist, HoodPosition hood) {

        double ret = getTargetVelocityPoly(dist, hood) ;

        if (dist < 75)
        {
            //
            // Green Zone
            //
            ret = green_velo_ ;
        }
        else if (dist > 90 && dist < 130)
        {
            //
            // Yellow Zone
            //
            ret = yellow_velo_ ;
        }
        else if (dist > 140 && dist < 160)
        {
            //
            // Blue Zone
            //
            ret = blue_velo_ ;
        }
        if (dist > 190 && dist < 220)
        {
            //
            // Red Zone
            //
            ret = red_velo_ ;
        }
        return ret ;
    }

    private double getTargetVelocityPowerPort3d(double dist, HoodPosition pos) {
        return power_port_power_ ;
    }

    private double getTargetVelocityPWL(double dist, HoodPosition pos) {
        return (pos ==  HoodPosition.Down) ? pwl_down_.getValue(dist) : pwl_up_.getValue(dist) ;
    }

    private double getTargetDistance() {
        double dist = tracker_.getDistance() ;

        if (!prev_good_dist_valid_)
        {
            prev_good_dist_ = dist ;
            prev_good_dist_count_ = 0 ;
            prev_good_dist_valid_ = true ;
        }
        else
        {
            double pcnt = Math.abs((dist - prev_good_dist_) / prev_good_dist_) * 100.0 ;
            if (pcnt > 10)
            {
                prev_good_dist_count_++ ;
                if (prev_good_dist_count_ < 4)
                {
                    dist = prev_good_dist_ ;
                }
                else
                {
                    prev_good_dist_ = dist ;
                    prev_good_dist_count_ = 0 ;
                }
            }
        }

        return dist ;
    }

    private void setTargetVelocity() throws BadMotorRequestException, MotorRequestFailedException {
        double dist = getTargetDistance() ;
        double rawdist = tracker_.getDistance() ;

        if (dist > max_hood_up_distance_)
            hood_pos_ = HoodPosition.Down ;
        else if (dist < min_hood_down_distance_)
            hood_pos_ = HoodPosition.Up ;

        double target = getTargetVelocity(dist, hood_pos_) ;

        shooter_velocity_action_.setHoodPosition(hood_pos_);
        shooter_velocity_action_.setTarget(target);
        shooter_velocity_action_.setReadyFlagEnabled(true);

        MessageLogger logger = sub_.getRobot().getMessageLogger() ;
        logger.startMessage(MessageType.Debug, logger_id_) ;
        logger.add("fire-action: calculated target") ;
        logger.add("rawdist", rawdist) ;
        logger.add("distance", dist) ;
        logger.add("velocity", target) ;
        logger.add("hood", hood_pos_.toString()) ;
        logger.endMessage();   
    }
}