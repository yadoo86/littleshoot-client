package org.lastbamboo.client.util.settings;

import java.io.File;

import org.lastbamboo.client.util.UserUtils;
import org.lastbamboo.client.util.settings.types.BooleanSetting;
import org.lastbamboo.client.util.settings.types.FileSetting;
import org.lastbamboo.client.util.settings.types.IntSetting;


/**
 * Settings to deal with bugs
 */ 
public class BugSettings extends ShootSettings 
    {

    private BugSettings() 
        {
        // Nothing to construct.
        }

    /**
     * Setting for whether or not to automatically report bugs
     * to the bug servlet.
     */
    public static final BooleanSetting USE_BUG_SERVLET =
		FACTORY.createBooleanSetting("USE_BUG_SERVLET", false);
		
    /**
     * Setting for whether or not to completely ignore all bugs.
     */
    public static final BooleanSetting IGNORE_ALL_BUGS =
        FACTORY.createBooleanSetting("IGNORE_ALL_BUGS", false);
        
    /**
     * Setting for whether or not bugs should be logged locally.
     * Developers can easily change this if they wish to see all
     * bugs logged to disk for future review.
     */
    public static final BooleanSetting LOG_BUGS_LOCALLY =
        FACTORY.createBooleanSetting("LOG_BUGS_LOCALLY", false);
        
    /**
     * Setting for the filename of the local bugfile log.
     */
    public static final FileSetting BUG_LOG_FILE =
        FACTORY.createFileSetting("BUG_LOG_FILE",
            new File(UserUtils.getUserSettingsDir(), "bugs.log"));
            
    /**
     * Setting for the maximum filesize of the buglog.
     */
    public static final IntSetting MAX_BUGFILE_SIZE =
        FACTORY.createIntSetting("MAX_BUGFILE_SIZE", 1024 * 500); // 500k
        
    /**
     * Setting for the file to use when writing bugs (for serialization)
     * to disk.
     */
    public static final FileSetting BUG_INFO_FILE =
        FACTORY.createFileSetting("BUG_INFO_FILE",
            new File(UserUtils.getUserSettingsDir(), "bugs.data"));
    }
