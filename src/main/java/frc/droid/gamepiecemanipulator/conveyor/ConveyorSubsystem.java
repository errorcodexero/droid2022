package frc.droid.gamepiecemanipulator.conveyor;

import org.xero1425.base.Subsystem;
import org.xero1425.base.motors.MotorController;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;

public class ConveyorSubsystem extends Subsystem {
    
    private int ball_count_ ;                       // The number of balls stored in the conveyor
    private boolean staged_for_collect_ ;           // Is true, if balls are positioned for collecting
    private boolean staged_for_fire_ ;              // Is true, if balls are positioned for firing
    private boolean collecting_ ;                   // Is true, when when collecting balls

    private MotorController intake_motor_ ;         // The motor for the flash intake piece
    private MotorController shooter_motor_ ;        // The motor for the shooter sidef of the conveyor
    private int sensor_logger_id_ ;                 // The message logger id just for sensors
    private ConveyorSensorThread sensor_thread_ ;
    
    public static final String SubsystemName = "conveyor";
    public static final String SensorLoggerName = "conveyor:sensors:messages";
    public static final int MAX_BALLS = 5;

    public static final String SensorSubsystemName = null;

    public ConveyorSubsystem(Subsystem parent) throws Exception {
        super(parent, SubsystemName);


        sensor_logger_id_ = getRobot().getMessageLogger().registerSubsystem(SensorLoggerName);

        ball_count_ = 0 ;

        staged_for_collect_ = false;
        staged_for_fire_ = false;
        collecting_ = false;

        intake_motor_ = getRobot().getMotorFactory().createMotor("intake", "subsystems:conveyor:hw:motors:intake");
        shooter_motor_ = getRobot().getMotorFactory().createMotor("shooter", "subsystems:conveyor:hw:motors:shooter");

        sensor_thread_ = new ConveyorSensorThread(this) ;
        sensor_thread_.start() ;
    }

    public int getSensorLoggerID() {
        return sensor_logger_id_ ;
    }

    public SettingsValue getProperty(String name) {
        SettingsValue v = null ;
        
        if (name.equals("ballcount")) {
            v = new SettingsValue(ball_count_) ;
        }
        else if (name.equals("readyToCollect")) {
            v = new SettingsValue(isStagedForCollect()) ;
        }
        else if (name.equals("readyToFire")) {
            v = new SettingsValue(isStagedForFire()) ;
        }

        return v ;
    }

    public ConveyorSensorThread getSensorThread() {
        return sensor_thread_ ;
    }

    public boolean isFull() {
        return ball_count_ == MAX_BALLS ;
    }

    public boolean isEmpty() {
        return ball_count_ == 0 ;
    }

    public boolean isStagedForCollect() {
        if (isFull())
            return false ;
            
        return staged_for_collect_ ;
    }

    @Override
    public void run() throws Exception {
        super.run() ;
        sensor_thread_.endRobotLoop();
    }

    public void setStagedForCollect(boolean staged) {
        staged_for_collect_ = staged;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("setStagedForCollect", staged_for_collect_);
        logger.endMessage();
    }

    public boolean isStagedForFire() {
        return staged_for_fire_ ;
    }

    public void setStagedForFire(boolean staged) {
        staged_for_fire_ = staged;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("setStagedForFire", staged_for_fire_);
        logger.endMessage();
    }

    public void setCollecting(boolean collecting) {
        collecting_ = collecting;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("collecting", collecting_);
        logger.endMessage();
    }

    @Override
    public void postHWInit() {
        setDefaultAction(new ConveyorStopAction(this));
    }

    @Override
    public void computeMyState() throws Exception {
        putDashboard("staged-fire", DisplayType.Verbose, staged_for_fire_) ;
        putDashboard("staged-collect", DisplayType.Verbose, staged_for_collect_);
        putDashboard("ballcount", DisplayType.Always, ball_count_);
    }

    public boolean isCollecting() {
        return collecting_ ;
    }

    public int getBallCount() {
        return ball_count_ ;
    }

    protected void setBallCount(int n) {
        ball_count_ = n ;
    }

    protected void incrementBallCount() {
        ball_count_++ ;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("ballcount", ball_count_);
        logger.endMessage();
    }

    protected void decrementBallCount() {
        ball_count_-- ;

        MessageLogger logger = getRobot().getMessageLogger();
        logger.startMessage(MessageType.Debug, getLoggerID());
        logger.add("Conveyor:").add("ballcount", ball_count_);
        logger.endMessage();
    }    

    protected void setMotorsPower(double intake, double shooter) {
        try {
            MessageLogger logger = getRobot().getMessageLogger();
            logger.startMessage(MessageType.Debug, getLoggerID());
            logger.add("Conveyor:").add("intake_power", intake) ;
            logger.add(" shooter_power", shooter) ;
            logger.endMessage();

            intake_motor_.set(intake) ;
            shooter_motor_.set(shooter) ;
        }
        catch(Exception ex) {
        }
    }  
} ;