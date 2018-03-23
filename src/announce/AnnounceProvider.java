package announce;


import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;

import utils.ZipCompressUtils;

public class AnnounceProvider {
	private volatile AnnounceFeed _announces;
	private volatile Announce_TripFeed _trips;
	private String FEED_DIRECTORY = "announce_feed";
	private String ZIP_FILE = "announce_feed.zip";
	private String _userdirectorypath;

	/**
	 *
	 * @param userdirectorypath ユーザーディレクトリのパス
	 * @throws IOException
	 */
	AnnounceProvider(String userdirectorypath) throws IOException{
		_userdirectorypath = userdirectorypath;
	}



	public void writeDataToFile() throws IOException{
		new File(_userdirectorypath+"/"+FEED_DIRECTORY).mkdirs();
		if(_announces!=null){_announces.writeDataToFile();}
		else{System.out.print("at AnnounceProvider announces not found");}
		if(_trips!=null){_trips.writeDataToFile();}
		else{System.out.print("at AnnounceProvider trips not found");}
		ZipCompressUtils.compressDirectory(_userdirectorypath+"/"+ZIP_FILE, _userdirectorypath+"/"+FEED_DIRECTORY);
	}

	public AnnounceFeed getAnnounces() {
		return _announces;
	}

	public void setAnnounces(AnnounceFeed announces) {
		this._announces = announces;
	}

	public void setAnnounces(ResultSet result){
		try {
			_announces = new AnnounceFeed(_userdirectorypath,result);
		} catch (NumberFormatException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}


	public Announce_TripFeed getAnnounce_Trips() {
		return _trips;
	}

	public void setAnnounce_Trips(Announce_TripFeed announce_trips) {
		this._trips = announce_trips;
	}

	public void setAnnounce_Trips(ResultSet result){
		try {
			_trips = new Announce_TripFeed(_userdirectorypath,result);
		} catch (NumberFormatException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}

