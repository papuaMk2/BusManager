package acount;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import announce.AnnounceList;
import announce.AnnounceList.AnnounceData;
import utils.MySingleton;

public class Pdo {
	private Pdo pdo = new Pdo();
	private static Connection conn = null;
//    private static String url = "jdbc:mysql://ilis.cis.ibaraki.ac.jp:3306/busApp?useUnicode=true&characterEncoding=utf8";
    private static String url = "jdbc:mysql://localhost/busApp?useUnicode=true&characterEncoding=utf8";
    private static String user = "busApp";
    private static String password = "busAppPW";
    private static int connectionTimeOut = 30;
/*
    private static String url = "jdbc:mysql://localhost/busapplicationuserdata";
    private static String user = "superuser";
    private static String password = "papuapassword118";
*/
	private Pdo(){
	}

	public static Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		if(conn == null){
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.setLoginTimeout(connectionTimeOut);
		    conn = DriverManager.getConnection(url, user, password);
		}
		System.out.println("at Pdo timeout"+DriverManager.getLoginTimeout());

//		System.out.println("connection:"+conn);
		return conn;
	}


	public static Connection connectionCheck() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Boolean valid = conn.isValid(MySingleton.getDbTimeout());
		System.out.println("at Pdo connectionCheck : connection is valid : "+valid);
		if(!valid){
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, user, password);
		}
		valid = conn.isValid(MySingleton.getDbTimeout());
		System.out.println("at Pdo connectionCheck : 2 connection is valid : "+valid);
		return conn;
	}


    public void destory(){
        try{
            if (conn != null){
                conn.close();
            }
        }catch (SQLException e){
            System.out.println("SQLException:" + e.getMessage());
        }
    }

    /**
     * その日に同じtripが登録されているかチェック
     * @param user_id
     * @param trip_id
     * @param vehicle_id
     * @return 登録されている場合vehicleposition_id、されていない場合-1
     */
    public static int checkVehiclePositionLog(int user_id, int trip_id, int vehicle_id){
    	int vehiclepositionid = -1;
    	//datetimeのフォーマットはhh:mm:ss
    	//まずはデータベースに挿入するタイムスタンプの取得
    	SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd ");
    	String date = dateFormat.format(new Date());
    	String starttime = "00:00:00";
    	String endtime = "23:59:59";
    	Timestamp startdatetime =null;
    	Timestamp enddatetime =null;
		try {
			startdatetime = new Timestamp(sdFormat.parse(date+starttime).getTime());
		} catch (ParseException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

		try {
			enddatetime = new Timestamp(sdFormat.parse(date+endtime).getTime());
		} catch (ParseException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		PreparedStatement stmt = null;
		String sql = "Select vehicleposition_id from vehicleposition_log where user_id = ? AND trip_id = ? AND vehicle_id = ? AND start_datetime >= ? AND start_datetime <= ?";
		System.out.print("at Pdo : sql = "+sql);
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, trip_id);
			stmt.setInt(3, vehicle_id);
			stmt.setTimestamp(4, startdatetime);//00:00:00
			stmt.setTimestamp(5, enddatetime);//23:59:59
			ResultSet result = stmt.executeQuery();
			if(result != null && result.next())vehiclepositionid = result.getInt(1);
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return vehiclepositionid;
    }



    /**
     * Logの設定登録
     * @param user_id
     * @param trip_id
     * @param vehicle_id
     * @param datetime datetime trip内で最初のバス停を出発する時間　hh:mm:ss
     * @return 成功したらそのid　失敗したら-1が返る
     */
    public static int insertVehiclePositionLog(int user_id, int trip_id, int vehicle_id,String datetime){
    	int result = -1;
    	//datetimeのフォーマットはhh:mm:ss
    	//まずはデータベースに挿入するタイムスタンプの取得
    	SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd ");
    	String date = dateFormat.format(new Date());
    	Timestamp startdatetime =null;
		try {
			if(datetime!=null) {
				startdatetime = new Timestamp(sdFormat.parse(date+datetime).getTime());
			}else {
				startdatetime = new Timestamp(dateFormat.parse(date).getTime());
			}
		} catch (ParseException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		PreparedStatement stmt = null;
		String sql = "INSERT INTO vehicleposition_log (user_id, trip_id, vehicle_id, start_datetime) values(?,?,?,?)";
		try {
			stmt = conn.prepareStatement(sql,new String[]{"vehicleposition_id"});
			stmt.setLong(1, user_id);
			stmt.setLong(2, trip_id);
			stmt.setInt(3, vehicle_id);
			stmt.setTimestamp(4, startdatetime);
			stmt.executeUpdate();
			ResultSet res = stmt.getGeneratedKeys();
			if(res != null && res.next()) {
			    result = res.getInt(1);
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return result;
    }


    /**
     * 詳細のインサート
     * @param vehicleposition_id
     * @param lat
     * @param lon
     */
    public static void insertVehiclePositionDetailesLog(int vehicleposition_id, double lat, double lon){
    	Timestamp time = new Timestamp(System.currentTimeMillis());
    	//datetimeのフォーマットはhh:mm:ss
    	//まずはデータベースに挿入するタイムスタンプの取得
		PreparedStatement stmt = null;
		String sql = "INSERT INTO vehicleposition_detailes_log (vehicleposition_id, lat, lon, date) values(?,?,?,?)";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, vehicleposition_id);
			stmt.setDouble(2, lat);
			stmt.setDouble(3, lon);
			stmt.setTimestamp(4, time);
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

    }

    /**
     * vehicleIdからprimaryキーを取得する
     * @param user_id
     * @param trainNumber
     * @return 見つかったらprimaryキーを返す　見つからなかったら-1を返す
     */
    public static int getVehicleID(int user_id, String trainNumber){
    	int vehicleid = -1;
		PreparedStatement stmt = null;
		String sql = "Select id from vehiclenumber where userid = ? AND vehicleid = ? ";
		System.out.println("at pdo : userid = "+user_id+"trainnumber = "+trainNumber);
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setString(2, trainNumber);
			ResultSet result = stmt.executeQuery();
			System.out.println("at pdo : userid = "+user_id+"trainnumber = "+trainNumber);
			if(result != null && result.next()){
				System.out.println("at pdo : vehicleid 見つかりました");
				vehicleid = result.getInt(1);
				System.out.println("at pdo : vehicleid = "+vehicleid +"result = "+result.getInt(1));
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return vehicleid;
    }

    /**
     * 利用可能か調べる
     * @return
     */

    public static Boolean checkVehicleIdAvailable(int user_id,String trainNumber){
    	int flag = 0;
		PreparedStatement stmt = null;
		String sql = "Select isengaged from vehiclenumber where userid = ? AND vehicleid = ? ";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setString(2, trainNumber);
			ResultSet result = stmt.executeQuery();
			System.out.println("at pdo : userid = "+user_id+"trainnumber = "+trainNumber);
			if(result != null && result.next()){
				System.out.println("vehicleid is available");
				flag = result.getInt(1);
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if(flag == 1){return false;}
		return true;

    }

    /**
     * トリップの最初のバス停の出発時刻を取得する
     * @param trip
     * @return
     */
    public static String getTripStartTime(int user_id ,int trip_id){
    	//stop_timesから取得する
    	String departure_time = null;
		PreparedStatement stmt = null;
		String sql = "Select departure_time from stop_times where user_id = ? AND trip_id = ? order by stop_sequences LIMIT 1";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, trip_id);
			ResultSet result = stmt.executeQuery();
			if(result != null && result.next())departure_time = result.getString(1);
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return departure_time;
    }

    /**
     * 指定したユーザのすべてのrouteデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getRoutes(int userid){
		String check_sql = "SELECT * FROM routes WHERE user_id =?";
		PreparedStatement check_stmt;
		try {
			check_stmt = conn.prepareStatement(check_sql);
			check_stmt.setInt(1, userid);
			return check_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }

    /**
     * 指定したユーザのすべてのcalendarデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getCalendars(int userid){
		try{
			String check_sql = "SELECT * FROM calender WHERE user_id =?";
			PreparedStatement check_stmt = conn.prepareStatement(check_sql);
			check_stmt.setInt(1, userid);
			return check_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }

        /**
     * 指定したユーザのすべてのshapeデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getShapes(int userid){
		String check_sql = "SELECT * FROM shapes WHERE user_id =?";
		PreparedStatement check_stmt;
		try {
			check_stmt = conn.prepareStatement(check_sql);
			check_stmt.setInt(1, userid);
			return check_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }

    /**
     * 指定したユーザのすべてのtripsデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getTrips(int userid){
		String check_sql = "SELECT * FROM trip WHERE user_id =?";
		PreparedStatement check_stmt;
		try{
			check_stmt = conn.prepareStatement(check_sql);
			check_stmt.setInt(1, userid);
			return check_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }

    /**
     * 指定したユーザのすべてのstopsデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getStops(int userid){
   		PreparedStatement check_stmt;
		try {
			check_stmt = conn.prepareStatement("SELECT * FROM stop WHERE user_id =?");
			check_stmt.setInt(1, userid);
			return check_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }


        /**
     * 指定したユーザのすべてのstop_timesデータを取得する
     * @param userid
     * @return
     */
    public static ResultSet getStop_times(int userid){
		PreparedStatement check_stop_times_stmt;
		try {
			check_stop_times_stmt = conn.prepareStatement("SELECT * FROM stop_times WHERE user_id =?");
			check_stop_times_stmt.setInt(1, userid);
			return check_stop_times_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
    }


    /**
     * ユーザーidとアナウンスデータから存在するアナウンスのidを返す
     * @param userid
     * @param data
     * @return　一致するアナウンスのid　なければ-1
     */
    public static long checkAnnounceExists(String userid,AnnounceData data){
    	long announceid=-1;
		// announce のチェックsql
		String check = "SELECT announce_id FROM announces WHERE user_id=? AND filename=?　AND stop_sequences=?";
		PreparedStatement checkStmt = null;
		try {
			checkStmt = conn.prepareStatement(check);
		}catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			checkStmt.setInt(1, Integer.parseInt(userid));
			checkStmt.setString(2, data.getFileName());
			checkStmt.setString(3, data.getStopSequences());
			ResultSet result = checkStmt.executeQuery();
			if (result != null && result.next()) {
				announceid = result.getLong("announce_id");
			}
		} catch (NumberFormatException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return announceid;
    }


    /**
     * アナウンス一気にinsert
     * @param userid
     * @param list
     */
    public static void insertAnnounces(String userid,AnnounceList list) {
		int tripid = list.getTrip_id();
		// announce のチェックsql
		String check = "SELECT announce_id FROM announces WHERE user_id=? AND filename=? AND stop_sequences=?";
		// announceのインサートsql
		String sql = "INSERT INTO announces (user_id,announce_lat,announce_lon,filename,stop_sequences) values(?,?,?,?,?)";
		// trip のチェックsql
		String checkTrip = "SELECT * FROM announce_trips WHERE announce_id=? AND trip_id=?";
		// trp のインサートsql
		String tripsql = "INSERT INTO announce_trips (user_id,announce_id,trip_id,announce_sequences) values(?,?,?,?)";

		PreparedStatement checkStmt = null;
		PreparedStatement checkTripStmt = null;
		PreparedStatement stmt = null;
		PreparedStatement tripStmt = null;
		try {
			checkStmt = conn.prepareStatement(check);
			stmt = conn.prepareStatement(sql, new String[] { "annnounce_id" });
			checkTripStmt = conn.prepareStatement(checkTrip);
			tripStmt = conn.prepareStatement(tripsql);

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		int announcenum =0;//announce_sequenceをもとめる
		for (AnnounceData data : list.getList()) {
			Long announceid = -1L;
			/**
			 * announceダブりチェック
			 */
			try {
				checkStmt.setInt(1, Integer.parseInt(userid));
				checkStmt.setString(2, data.getFileName());
				checkStmt.setString(3, data.getStopSequences());
				ResultSet result = checkStmt.executeQuery();
				if (result != null && result.next()) {
					announceid = result.getLong("announce_id");
				}
			} catch (NumberFormatException | SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

			/**
			 * announce insert
			 */
			if (announceid == -1) {
				try {
					System.out.println("at addannouncedetailes : statement is "+stmt);
					stmt.setInt(1, Integer.parseInt(userid));
					stmt.setDouble(2, data.getLatitude());
					stmt.setDouble(3, data.getLongitude());
					stmt.setString(4, data.getFileName());
					stmt.setInt(5, Integer.parseInt(data.getStopSequences()));
					stmt.executeUpdate();
					ResultSet res = stmt.getGeneratedKeys();
					if (res != null && res.next()) {
						announceid = res.getLong(1);
					}
				} catch (NumberFormatException | SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			/**
			 * announcetripダブりチェック
			 */
			try {
				checkTripStmt.setLong(1, announceid);
				checkTripStmt.setLong(2, tripid);
				ResultSet tripResult = checkTripStmt.executeQuery();
				if (tripResult == null || !tripResult.next()) {
					System.out.println("addannouncedetails debug trip insert");
					/**
					 * announcetrip insert
					 */
					tripStmt.setInt(1, Integer.parseInt(userid));
					tripStmt.setLong(2, announceid);
					tripStmt.setLong(3, tripid);
					tripStmt.setInt(4, announcenum*5);
					tripStmt.executeUpdate();
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			//announce_sequencesをインクリメント
			announcenum++;
		}
    }

    /**
     * announcesのデータを取得
     * @param userid
     * @return
     */
    public static ResultSet getAnnounceData(String userid) {
		String resultSql = "SELECT * FROM announces WHERE user_id =?";
		PreparedStatement resultStmt;
			ResultSet announceResult = null;
			try {
				resultStmt = conn.prepareStatement(resultSql);
				resultStmt.setInt(1, Integer.parseInt(userid));
				announceResult = resultStmt.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return announceResult;
    }

    /**
     * announce_tripsのデータを取得
     * @param userid
     * @return
     */
    public static ResultSet getAnnounceTripData(String userid) {
		String tripResultSql = "SELECT * FROM announce_trips WHERE user_id =?";
		ResultSet tripResult = null;
		try {
			PreparedStatement tripResultStmt = conn.prepareStatement(tripResultSql);
			tripResultStmt.setInt(1, Integer.parseInt(userid));
			tripResult = tripResultStmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tripResult;
    }

    /**
     *
     * @param vehicleid
     * @param state true 使用可能に false使用中に変更
     * @return
     */
    public static Boolean setVehicleIdEnabledState(int vehicleid,boolean state){
    	Timestamp time = new Timestamp(System.currentTimeMillis());
    	//datetimeのフォーマットはhh:mm:ss
    	//まずはデータベースに挿入するタイムスタンプの取得
    	int isengaged = -1;
    	if(state){
    		isengaged = 0;
    	}else{
    		isengaged = 1;
    	}
		PreparedStatement stmt = null;
		String sql = "UPDATE vehiclenumber set isengaged = ? WHERE id = ?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, isengaged);
			stmt.setInt(2, vehicleid);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
    }
}
