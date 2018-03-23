package Gtfs_Servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import Gtfs_Servlet.RouteData.BusStation;
import acount.Pdo;
import utils.MySingleton;


/**
 * Servlet implementation class AddRouteData
 */
@WebServlet("/AddRouteData")
public class AddRouteData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ServletContext _context = null;
	private String PROVIDERNAME = "provider"; //_contextにインスタンスを登録するときに利用する名前　＊例:userid:PROVIDENAME

//	private String ZIP_FILE_PATH = null;   //圧縮したファイルの絶対パス
//	private String DIRECTORY_PATH = null;  //圧縮するフォルダの絶対パス
	private String ROUTEINSTANCE = "routeinstance";  //sessionに関連付けられているルートインスタンスの名前

	private Logger _log = LoggerFactory.getLogger(AddRouteData.class);
	private Connection conn = null;
	/**
	 * 例：圧縮後ファイルへの絶対パス
	 * 　　CONTEXT_DIRECTORY+"/"+session.getAttribute("username")+"/"+ZIPFILE_NAME　
	 */
	private String ZIPFILE_NAME = "google_transit.zip";  //圧縮後のファイル名
	private String DIRECTORY_NAME = "google_transit_feed";  //圧縮するフォルダのフォルダ名
	private String CONTEXT_DIRECTORY = null;
	private String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";

	private static int DISTANCE = 20;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddRouteData() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException{
		System.out.println("init in servlet");
		super.init(config);
		_context = getServletConfig().getServletContext();
//		ZIP_FILE_PATH = _context.getRealPath("/"+ZIPFILE_NAME);
//		DIRECTORY_PATH = _context.getRealPath("/sample-feed");
//		_path = new File(_context.getRealPath("/"+ZIPFILE_NAME));
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("addroute start");
		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
//		response.setContentType("text/html; charset=utf-8");
		System.out.println("session:"+session);
        PrintWriter out = response.getWriter();
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
		if(session.getAttribute("login") == null ){
			out.println("Time out");
			return;
			}


//		try {
//			conn = Pdo.getConnection();
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
		try {

			conn = Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
        String userid = (String)session.getAttribute("userid");
//        AnnounceProvider provider;
        URLCodec codec = new URLCodec("UTF-8");
        try {
    		String line;
    		while ((line = reader.readLine()) != null) {
    			sb.append(line).append('\n');
    		}
    	} finally {
    		reader.close();
    	}



        Gson gson = new Gson();

        String requestData = sb.toString();


        //urlデコード
//        try {
//			requestData = codec.decode(new String(requestData.getBytes("ISO-8859-1")), "UTF-8");
//		} catch (DecoderException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
        URLDecoder _decoder = new URLDecoder();
		requestData = _decoder.decode(requestData, "UTF-8");

       RouteData route = gson.fromJson(requestData,RouteData.class);
       _log.info("addrouredata start");
       out.println(addRouteData(route, session));
	}


	private synchronized Long addRouteData(RouteData route,HttpSession session) throws IOException{


		String userdatadirectory = MySingleton.getInstance().getUserDataDirectory()+session.getAttribute("userfoldername");
//		String providername = session.getAttribute("userid")+":"+PROVIDERNAME; //_contextに登録するproviderの名前
		RouteDataProvider provider = new RouteDataProvider(userdatadirectory);
		int userid = Integer.parseInt((String)session.getAttribute("userid"));

//		if((provider = (RouteDataProvider)_context.getAttribute(providername))==null){
//			_context.setAttribute(providername, provider = new RouteDataProvider(userdatadirectory));
//		}

		_log.info("start route registration");
		//バス停の数が２以上でなければルートでないため登録しない
		ArrayList<BusStation> _route = route.stops();
		if(_route.size() <= 1){return -1L;}
		_log.info("start route insert");
		/**
		 * Routesフィード作成
		 */

		RoutesFeed routefeed = provider.getRoutes();
		Long routeid = -1L;
		if(route.getEndDate()!=null && route.getStartDate()!=null){
			String check_exists = "SELECT route_id FROM routes WHERE user_id=? AND route_long_name=?";
			try {
				PreparedStatement checkExist = conn.prepareStatement(check_exists);
				checkExist.setInt(1, userid);
				checkExist.setString(2, route.getLongName());
				ResultSet result = checkExist.executeQuery();
				if(result != null && result.next()) {
				    routeid = result.getLong("route_id");
				}
			} catch (SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			if(routeid == -1){
			String sql = "INSERT INTO routes (user_id, route_short_name, route_long_name, route_type) values(?,?,?,?)";
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(sql,new String[]{"route_id"});
				stmt.setInt(1, userid);
				stmt.setString(2, route.getShortName());
				stmt.setString(3, route.getLongName());
				stmt.setInt(4, 3);
				stmt.executeUpdate();
				ResultSet res = stmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    routeid = res.getLong(1);
				}


			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			}
			String check_sql = "SELECT * FROM routes WHERE user_id =?";
			PreparedStatement check_stmt;
			try {
				check_stmt = conn.prepareStatement(check_sql);
				check_stmt.setInt(1, userid);
				ResultSet result = check_stmt.executeQuery();
				provider.setRoutes(result);
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}


		}

		/**
		 * calendeフィード作成
		 */
		Long serviceid = -1L;
		if(route.getWeekCalender().size() > 1 && route.getEndDate()!=null && route.getStartDate()!=null){
			ArrayList<Integer> list = route.getWeekCalender();
			String sql = "INSERT INTO calender (monday,tuesday, wednesday, thursday, friday,saturday, sunday, startdate, enddate, user_id) "
					+"values(?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(sql,new String[]{"service_id"});
				for(int i=0;i<7;i++){
					stmt.setInt(i+1, list.get(i));
				}
				stmt.setString(8, route.getStartDate());
				stmt.setString(9, route.getEndDate());
				stmt.setInt(10, userid);
				stmt.executeUpdate();
				ResultSet res = stmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    serviceid = res.getLong(1);
				}

				String check_sql = "SELECT * FROM calender WHERE user_id =?";
				PreparedStatement check_stmt = conn.prepareStatement(check_sql);
				check_stmt.setInt(1, userid);
				ResultSet result = check_stmt.executeQuery();
				provider.setCalender(result);
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

		/**
		 * shapeフィード作成
		 */
		Long shapeid = -1L;
			ArrayList<Gtfs_Servlet.RouteData.Shape> list = route.getShapes();
			if(list.size() >0){
			try {
				/**
				 * shapeidの取得
				 */
				String getId = "INSERT INTO shapeid (shapeid) VALUES (NULL)";
				PreparedStatement getIdStmt = conn.prepareStatement(getId,new String[]{"shapeid"});
				getIdStmt.executeUpdate();
				ResultSet res = getIdStmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    shapeid = res.getLong(1);
				}
			} catch (SQLException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			if(shapeid >= 0){
			String sql = "INSERT INTO shapes (shape_id,user_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence) "
					+"values(?,?,?,?,?)";
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(sql);
				for(int i =0;i<list.size();i++){
					Gtfs_Servlet.RouteData.Shape shape = list.get(i);
				stmt.setLong(1, shapeid);
				stmt.setInt(2, userid);
				stmt.setDouble(3, shape.getShape_pt_lat());
				stmt.setDouble(4, shape.getShape_pt_lon());
				stmt.setInt(5, i*5);
				stmt.executeUpdate();
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

			}
			}
				String check_sql = "SELECT * FROM shapes WHERE user_id =?";
				PreparedStatement check_stmt;
				try {
					check_stmt = conn.prepareStatement(check_sql);
					check_stmt.setInt(1, userid);
					ResultSet result = check_stmt.executeQuery();
					provider.setShapes(result);
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			check_sql = null;
			check_stmt = null;

		Long tripid = -1L;

		/**
		 * tripsフィード作成
		 */
		if(routeid > 0 && serviceid > 0){
			String sql = "INSERT INTO trip (user_id,route_id,service_id,shape_id) values(?,?,?,?)";
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(sql,new String[]{"trip_id"});
				stmt.setInt(1, userid);
				stmt.setLong(2, routeid);
				stmt.setLong(3, serviceid);
				if(shapeid != -1){
					stmt.setLong(4, shapeid);
				}else{
					stmt.setNull(4, java.sql.Types.BIGINT);
				}
				stmt.executeUpdate();
				ResultSet res = stmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    tripid = res.getLong(1);
				}

				check_sql = "SELECT * FROM trip WHERE user_id =?";
				check_stmt = conn.prepareStatement(check_sql);
				check_stmt.setInt(1, userid);
				ResultSet result = check_stmt.executeQuery();
				provider.setTrips(result);
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

		check_sql = null;
		check_stmt = null;

		/**
		 * Stopsフィードの作成
		 */
		String stop_sql = "INSERT INTO stop (user_id,stop_name,stop_lat,stop_lon) values(?,?,?,?)";
		PreparedStatement stop_stmt;
		String stop_times_sql = "INSERT INTO stop_times (trip_id,user_id,arrival_time,departure_time,stop_id,stop_sequences) "
				+"values(?,?,?,?,?,?)";

		try {
			stop_stmt = conn.prepareStatement(stop_sql,new String[]{"stop_id"});
			PreparedStatement stop_times_stmt = conn.prepareStatement(stop_times_sql);

			for(int i=0;i<_route.size();i++){
				BusStation stop = _route.get(i);
				/*stop登録*/
				Long stopid = -1L;
				String check_exists = "SELECT stop_id,stop_lat,stop_lon FROM stop WHERE user_id=? AND stop_name=?";
				PreparedStatement checkExist = conn.prepareStatement(check_exists);
				checkExist.setInt(1, userid);
				checkExist.setString(2, stop.getName());
				ResultSet result = checkExist.executeQuery();
				while(result != null && result.next()) {
					double lat = result.getDouble("stop_lat");
					double lon = result.getDouble("stop_lon");
					if(getDistance(lat, lon, Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLon()), 'K') <= DISTANCE){
						//名前が同じかつ距離が行っていいかの場合は同じバス停として扱う
					    stopid = result.getLong("stop_id");
					    break;
					}
				}
				if(stopid == -1){
				stop_stmt.setInt(1, userid);
				stop_stmt.setString(2, stop.getName());
				stop_stmt.setDouble(3, Double.parseDouble(stop.getLat()));
				stop_stmt.setDouble(4, Double.parseDouble(stop.getLon()));
				stop_stmt.executeUpdate();
				ResultSet res = stop_stmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    stopid = res.getLong(1);
				}
				}
//				if(stopid == -1){break;}
				/*stop_times登録*/
				String check_stop_times_exists = "SELECT * FROM stop_times WHERE trip_id=? AND stop_sequences=?";
				PreparedStatement checkStopTimesExist = conn.prepareStatement(check_stop_times_exists);
				checkStopTimesExist.setLong(1, tripid);
				checkStopTimesExist.setInt(2, i);
				ResultSet stop_times_result = checkStopTimesExist.executeQuery();
				if(stop_times_result == null || !stop_times_result.next()) {
					stop_times_stmt.setLong(1, tripid);
					stop_times_stmt.setInt(2, userid);
					stop_times_stmt.setString(3, stop.getArrivalTime());
					stop_times_stmt.setString(4, stop.getDepartureTime());
					stop_times_stmt.setLong(5, stopid);
					stop_times_stmt.setInt(6, i);
					stop_times_stmt.executeUpdate();
				}
			}
			check_stmt = conn.prepareStatement("SELECT * FROM stop WHERE user_id =?");
			check_stmt.setInt(1, userid);
			ResultSet result = check_stmt.executeQuery();
			provider.setStops(result);

			PreparedStatement check_stop_times_stmt = conn.prepareStatement("SELECT * FROM stop_times WHERE user_id =?");
			check_stop_times_stmt.setInt(1, userid);
			ResultSet stop_times_result = check_stop_times_stmt.executeQuery();
			provider.setStop_Times(stop_times_result);

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		writeDataToFile(userdatadirectory, provider);
		return tripid;
	}

	  /*
     * 2点間の距離を取得
     * 第五引数に設定するキー（unit）で単位別で取得できる
     */
    private double getDistance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +  Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        double miles = dist * 60 * 1.1515;
        switch (unit) {
            case 'K': //メートル
                return (miles * 1.609344 * 1000);
            case 'N': // ノット
                return (miles * 0.8684);
            case 'M': // マイル
            default:
                return miles;
        }
    }

    private double rad2deg(double radian) {
        return radian * (180f / Math.PI);
    }

    public double deg2rad(double degrees) {
        return degrees * (Math.PI / 180f);
    }

	/**
	 * ファイルへの書き込みとフォルダの圧縮を行う
	 * @param directory ユーザーフォルダへの絶対パス
	 * @param route ルートインスタンス
	 * @throws IOException
	 */
	protected synchronized void writeDataToFile(String directory,RouteDataProvider provider) throws IOException {
	    String directorypath = directory+"/"+DIRECTORY_NAME; //圧縮するフォルダの絶対パス
	    String zipfilepath = directory+"/"+ZIPFILE_NAME;  //圧縮後のファイルの絶対パス
	    File newdirectory = new File(directorypath);
	    newdirectory.mkdirs();

	    provider.writeDataToFile();

	    provider.createZip(zipfilepath, directorypath);
//	    ZipCompressUtils.compressFileList(zipfilepath, );
//	    ZipCompressUtils.compressDirectory(zipfilepath, directorypath);
	  }

}
