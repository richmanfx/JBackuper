import org.apache.logging.log4j.Logger;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;

public class Main {

    public static void main(String[] args) {

        final Logger log = LogManager.getLogger();
        log.info("=--> Start JBackuper");

        final AppConfig appConfig = ConfigFactory.create(AppConfig.class);

        String[] backup_1 = appConfig.backup_1();
        if (null == backup_1) {
            log.error("Parameters from config file not found");
            System.exit(2);    // 78 - Configuration error (unix)
                                //  2 - ERROR_FILE_NOT_FOUND (win)
        }

        log.info("backup_1[0]: {}", backup_1[0]);
        log.info("backup_1[1]: {}", backup_1[1]);
    }
}
