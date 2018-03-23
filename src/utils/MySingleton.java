package utils;

/**
 * Created by ryosuke on 2015/12/25.
 */
public class MySingleton {
    private static MySingleton _this = new MySingleton();
    private static final String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";
    private static final String ANNOUNCE_PROVIDER_NAME = "announceFeed";
    private static final String ANNOUNCE_UPDATE = "announce_update";
    private static final String waki = "dc8cd7a7b010b2bed5f447";
    private static final int DB_TIMEOUT = 5;


	private MySingleton(){}


    public static MySingleton getInstance(){
        return _this;
    }

    public static int getDbTimeout(){
    	return DB_TIMEOUT;
    }

    public String getUserDataDirectory(){return USERDATA_DIRECTORY;}

    public static String getAnnounceProviderName() {
		return ANNOUNCE_PROVIDER_NAME;
	}

    public static String getAnnounceUpdateName(){
    	return ANNOUNCE_UPDATE;
    }

    public static String getWaki(){
    	return waki;
    }

}

