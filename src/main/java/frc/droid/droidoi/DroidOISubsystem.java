package frc.droid.droidoi;

import org.xero1425.base.Subsystem;
import org.xero1425.base.oi.OISubsystem;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

public class DroidOISubsystem extends OISubsystem {
    private DroidOIDevice oi_ ;

    public final static String SubsystemName = "droidoi";
    private final static String OIHIDIndexName = "oi:index";

    public DroidOISubsystem(Subsystem parent, TankDriveSubsystem db, boolean climber) {
        super(parent, SubsystemName, GamePadType.Standard, db) ;

        int index ;
        MessageLogger logger = getRobot().getMessageLogger() ;

        //
        // Add the custom OI for droid to the OI subsystem
        //
        try {
            index = getSettingsValue(OIHIDIndexName).getInteger() ;
        } catch (BadParameterTypeException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(OIHIDIndexName) ;
            logger.add(" exists, but is not an integer").endMessage();
            index = -1 ;
        } catch (MissingParameterException e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("parameter ").addQuoted(OIHIDIndexName) ;
            logger.add(" does not exist").endMessage();
            index = -1 ;            
        }

        if (index != -1) {
            try {
                oi_ = new DroidOIDevice(this, index, getGamePad(), climber) ;
                addHIDDevice(oi_) ;
            }
            catch(Exception ex) {
                logger.startMessage(MessageType.Error) ;
                logger.add("OI HID device was not created ").endMessage();
            }
        }
    }
}