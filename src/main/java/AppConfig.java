import org.aeonbits.owner.Config;

@Config.Sources({ "file:jbackuper.config" })
public interface AppConfig extends Config {

    String[] backup_1();
    String[] backup_2();

}
