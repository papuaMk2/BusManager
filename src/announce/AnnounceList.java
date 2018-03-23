package announce;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by ryosuke on 2016/05/25.
 * 音声ファイルの詳細のJSON元オブジェクト
 * JSONに変換してサーバに送信
 */
public class AnnounceList {
    private int trip_id = -1;
    private List <AnnounceData> list;

    public AnnounceList(){
        list = new ArrayList<AnnounceData>();
    }

    public List<AnnounceData> getList() {
        return list;
    }

    public void setList(List<AnnounceData> list) {
        this.list = list;
    }

    public int getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }

    public void addAnnounceData(double longitude, double latitude, String filename,String stop_sequences){
        list.add(new AnnounceData(longitude, latitude, filename, stop_sequences));
    }

    public class AnnounceData {
        private double longitude;
        private double latitude;
        private String filename;
        private String _stopSequences;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getFileName() {
            return filename;
        }

        public String getStopSequences(){return _stopSequences;}


        public AnnounceData(double longitude, double latitude, String filename,String stop_sequences) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.filename = filename;
            this._stopSequences = stop_sequences;
        }
    }

}
