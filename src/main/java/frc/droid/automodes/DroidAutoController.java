package frc.droid.automodes;

import org.xero1425.base.controllers.AutoController;
import org.xero1425.base.controllers.AutoMode;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.MissingParameterException;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Droid;

//
// This class is the automode controller for Droid.  It basically creates all of the
// automodes and picks one based on test mode and the automode controller selected.
//
public class DroidAutoController extends AutoController {
    private AutoMode test_mode_ ;
    private AutoMode [] modes_ ;
    private SendableChooser<AutoMode> chooser_ ;

    public DroidAutoController(Droid robot) throws MissingParameterException, BadParameterTypeException {
        super(robot, "droid-auto");

        MessageLogger logger = getRobot().getMessageLogger() ;
        modes_ = new AutoMode[10] ;
        
        try {
            test_mode_ = new DroidTestAutoMode(this);
            modes_[0] = new FarSideAuto(this) ;
            modes_[1] = new NearSideEightAuto(this) ;
            modes_[2] = new NopAuto(this, "Nop-2") ;
            modes_[3] = new NopAuto(this, "Nop-3") ;
            modes_[4] = new NopAuto(this, "Nop-4") ;
            modes_[5] = new NopAuto(this, "Nop-5") ;
            modes_[6] = new NopAuto(this, "Nop-6") ;
            modes_[7] = new NopAuto(this, "Nop-7") ;
            modes_[8] = new NopAuto(this, "Nop-8") ;
            modes_[9] = new NopAuto(this, "Nop-9") ;                                                                                    
        }
        catch(Exception e) {
            logger.startMessage(MessageType.Error) ;
            logger.add("cannot create automode 'DroidTestAutoMode', exception caught - ") ;
            logger.add(e.getMessage()) ;
            logger.endMessage();
        }

        chooser_ = new SendableChooser<AutoMode>() ;
        for(int i = 0 ; i < modes_.length ; i++)
        {
            chooser_.addOption(modes_[i].getName(), modes_[i]) ;
        }
        chooser_.setDefaultOption(modes_[0].getName(), modes_[0]) ;
        SmartDashboard.putData(chooser_) ;
    }

    public void updateAutoMode(int mode, String gamedata) throws Exception {
        if (isTestMode()) {
            setAutoMode(test_mode_) ;
        }
        else {
            setAutoMode(chooser_.getSelected()) ;
        }
    }
}
