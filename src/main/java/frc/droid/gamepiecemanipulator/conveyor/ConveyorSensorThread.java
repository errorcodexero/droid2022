package frc.droid.gamepiecemanipulator.conveyor;

import java.util.ArrayList;
import org.xero1425.base.Subsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

import edu.wpi.first.wpilibj.DigitalInput;

//
// This class is a thread the monitors the conveyor sensor inputs.  This
// thread runs on a 5 ms loop time to be sure conveyor sensor events are not
// missed.  Not only is the latest state for a sensor stored, but any low to 
// high or high to low transitions are stored and can be queried by the actions
// that operate on the conveyor.  
//

public class ConveyorSensorThread extends Thread {
    public static final int SENSOR_COUNT = Sensor.values().length;
    public static final int LOOP_INTERVAL_MS = 5;

    private ConveyorSubsystem sub_; // The subsystem for the conveyor
    private boolean running_; // If true the sensor thread is running
    private DigitalInput[] sensors_; // The array of ball detect sensors
    private boolean[] sensor_states_; // The states of ball detect sensors
    private boolean[] prev_sensor_states_; // The states of ball detect sensors last robot loop
    private ArrayList<ArrayList<SensorTransition>> transitions_; // A list of transitions that have occurred since the
                                                                 // last robot loops
    private Object lock_; // An object used to lock the data members where necessary

    public enum SensorTransition {
        HIGH_TO_LOW, LOW_TO_HIGH
    };

    public enum Sensor {
        A(0), B(1), C(2), D(3);

        public final int value;

        private Sensor(int value) {
            this.value = value;
        }

        public static Sensor fromInt(int id) throws Exception {
            Sensor[] As = Sensor.values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].value == id)
                    return As[i];
            }
            throw new Exception("invalid integer in Sensor.fromInt");
        }
    }

    public ConveyorSensorThread(ConveyorSubsystem sub) throws BadParameterTypeException, MissingParameterException {
        sub_ = sub;
        running_ = true;

        lock_ = new Object();
        sensors_ = new DigitalInput[SENSOR_COUNT];
        sensor_states_ = new boolean[SENSOR_COUNT];
        prev_sensor_states_ = new boolean[SENSOR_COUNT];
        transitions_ = new ArrayList<ArrayList<SensorTransition>>();

        int num;
        int basech = (int) 'a';
        for (int i = 0; i < SENSOR_COUNT; i++) {
            String name = "hw:sensors:" + (char) (basech + i);
            num = sub.getSettingsValue(name).getInteger() ;
            sensors_[i] = new DigitalInput(num);
            sensor_states_[i] = false;
            prev_sensor_states_[i] = false;
            transitions_.add(new ArrayList<SensorTransition>());
            String sname = Character.toString((char) ('A' + i));
            sub_.putDashboard(sname, Subsystem.DisplayType.Verbose, sensor_states_[i]);
        }
    }

    public void endRobotLoop() {
        //
        // Remove all stored transitions to be ready for the next robot loop
        //
        for (int i = 0; i < SENSOR_COUNT; i++) {
            transitions_.get(i).clear();
        }
    }

    public void stopThread() {
        running_ = false;
    }

    public void run() {
        MessageLogger logger = sub_.getRobot().getMessageLogger();

        while (running_) {

            for (int i = 0; i < SENSOR_COUNT; i++) {
                synchronized (lock_) {
                    //
                    // Get the sensor state
                    //
                    prev_sensor_states_[i] = sensor_states_[i];
                    sensor_states_[i] = !sensors_[i].get();

                    if (sensor_states_[i] && !prev_sensor_states_[i]) {
                        transitions_.get(i).add(SensorTransition.LOW_TO_HIGH);
                        try {
                            Sensor s = Sensor.fromInt(i);
                            logger.startMessage(MessageType.Debug, sub_.getLoggerID());
                            logger.add("Conveyor:").add("sensor ").add(s.toString());
                            logger.add(" saw LOW_TO_HIGH");
                            logger.endMessage();
                        } catch (Exception ex) {
                        }
                    } else if (!sensor_states_[i] && prev_sensor_states_[i]) {
                        transitions_.get(i).add(SensorTransition.HIGH_TO_LOW);
                        try {
                            Sensor s = Sensor.fromInt(i);
                            logger.startMessage(MessageType.Debug, sub_.getLoggerID());
                            logger.add("Conveyor:").add("sensor ").add(s.toString());
                            logger.add(" saw HIGH_TO_LOW");
                            logger.endMessage();

                        } catch (Exception ex) {
                        }
                    }

                    if (prev_sensor_states_[i] != sensor_states_[i]) {
                        try {
                            Sensor s = Sensor.fromInt(i);
                            logger.startMessage(MessageType.Debug, sub_.getLoggerID());
                            logger.add("Conveyor:").add("sensor ").add(s.toString());
                            logger.add(" transitioned to ").add(sensor_states_[i]);
                            logger.endMessage();
                            sub_.putDashboard(s.toString(), Subsystem.DisplayType.Verbose, sensor_states_[i]);
                        } catch (Exception ex) {
                        }
                    }
                }
            }

            if (prev_sensor_states_[Sensor.D.value] == true && sensor_states_[Sensor.D.value] == false) {
                sub_.setStagedForFire(true);
            }

            try {
                Thread.sleep(LOOP_INTERVAL_MS);
            } catch (Exception ex) {
            }
        }
    }

    public boolean getSensorState(Sensor s) {
        boolean ret = false;

        synchronized (lock_) {
            ret = sensor_states_[s.value];
        }

        return ret;
    }

    public boolean didSensorLowToHigh(Sensor s) {
        boolean ret = false;

        synchronized (lock_) {
            MessageLogger logger = sub_.getRobot().getMessageLogger();
            logger.startMessage(MessageType.Debug, sub_.getLoggerID());
            logger.add("didSensorLowToHigh ") ;
            for(int i = 0 ; i < transitions_.size() ; i++) {
                logger.add(" " ) ;
                logger.add(transitions_.get(i).toString()) ;
            }
            logger.endMessage();

            ret = transitions_.get(s.value).contains(SensorTransition.LOW_TO_HIGH);
        }

        return ret;
    }

    public boolean didSensorHighToLow(Sensor s) {
        boolean ret = false;

        synchronized (lock_) {
            MessageLogger logger = sub_.getRobot().getMessageLogger();
            logger.startMessage(MessageType.Debug, sub_.getLoggerID());
            logger.add("didSensorLowToHigh ") ;
            for(int i = 0 ; i < transitions_.size() ; i++) {
                logger.add(" " ) ;
                logger.add(transitions_.get(i).toString()) ;
            }
            logger.endMessage();            
            ret = transitions_.get(s.value).contains(SensorTransition.HIGH_TO_LOW);
        }

        return ret;
    }
}
