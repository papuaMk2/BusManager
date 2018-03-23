package Gtfs_Servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeLibrary;

import com.google.protobuf.Message;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedEntity.Builder;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.PassengersDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.ryosuke.gtfs_realtime.FeedMessageProvider;

import acount.Pdo;
import utils.MySingleton;

/**
 * Servlet implementation class TripUpdate
 */
@WebServlet("/TripUpdate")
public class AddTripUpdate extends AbstractAddServlet {
	private static final long serialVersionUID = 1L;
	private String FILENAME = "trip-update.dat";
	private Connection conn = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddTripUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException{
		System.out.println("init in servlet");
		super.init(config);
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
		super.doPost(request,response);
		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
		System.out.println("session:"+session);
		if(request.getParameter("debug") != null){
			response.getWriter().print(getMessage((FeedMessageProvider)_context.getAttribute((String)session.getAttribute("userid"))));
		}
		System.out.println(session.getAttribute("login"));
		if(session.getAttribute("login") == null ){
			response.getWriter().println("Time out");
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
		FeedMessageProvider provider = (FeedMessageProvider)_context.getAttribute((String)session.getAttribute("userid"));
		Message message = getMessage(provider);

		int userid = Integer.parseInt((String)session.getAttribute("userid"));
		String messagetype = request.getParameter("message");
		System.out.println("at AddTripUpdate: messagetype="+messagetype);
		String vehicleid = request.getParameter("vehicleid");


		//trip終了の場合
		if(messagetype !=null && messagetype.equals("finish")){
			String tripid = request.getParameter("tripid");
			if(tripid !=null){
				int delay = Integer.parseInt(request.getParameter("delay"));
				String stopid = request.getParameter("stopid");
				int stopsequence = Integer.parseInt(request.getParameter("stopsequence"));
				int ridenum = Integer.parseInt(request.getParameter("ridenum"));
				int getoffnum = Integer.parseInt(request.getParameter("getoffnum"));
				Timestamp date = new Timestamp(System.currentTimeMillis());
				insertTripLog(userid, tripid, date, stopid, stopsequence, delay, ridenum, getoffnum, messagetype);
				}
			deleteFeedMessage(vehicleid,String.valueOf(userid));
			writeMessageToFile(MySingleton.getInstance().getUserDataDirectory()+session.getAttribute("userfoldername"),provider);
			return;
		}

		int delay = Integer.parseInt(request.getParameter("delay"));
		String tripid = request.getParameter("tripid");
		String stopid = request.getParameter("stopid");
		int stopsequence = Integer.parseInt(request.getParameter("stopsequence"));
		int ridenum = Integer.parseInt(request.getParameter("ridenum"));
		int getoffnum = Integer.parseInt(request.getParameter("getoffnum"));
		Timestamp date = new Timestamp(System.currentTimeMillis());

		insertTripLog(userid, tripid, date, stopid, stopsequence, delay,ridenum, getoffnum,messagetype);
		buildFeedMessage(tripid,stopid,stopsequence,delay,ridenum,getoffnum,messagetype,session,vehicleid);
//		response.getWriter().print(message);
		writeMessageToFile(MySingleton.getInstance().getUserDataDirectory()+session.getAttribute("userfoldername"),provider);
	}

	/*
	 * FeedMessageの作成と登録
	 */
//	@Override
	private void buildFeedMessage(String tripid,String stopid,int stopsequence,int delay,int ridenum,int getoffnum,String message,HttpSession session, String vehicleid) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("start buildFeedMessage");
		FeedMessage.Builder tripupdates = GtfsRealtimeLibrary.createFeedMessageBuilder();
//		_gtfsrealtimeprovider.getVehiclePositions()
		int userid = Integer.parseInt((String)session.getAttribute("userid"));
//		String providername = session.getAttribute("userid")+":provider"; //_contextに登録するproviderの名前

		Builder newentity;
		// sessionからuseridを取得
		FeedMessageProvider provider = (FeedMessageProvider)_context.getAttribute((String)session.getAttribute("userid"));
		FeedMessage preTripUpdate =(FeedMessage)getMessage(provider);
		if(preTripUpdate.getEntityCount()==0){
			newentity = getFeedEntity(userid,tripid,stopid,stopsequence,delay,ridenum,getoffnum,message,null,vehicleid);
			tripupdates.addEntity(newentity);
//			return vehiclePositions.build();
			provider.setTripUpdates(tripupdates.build());
			return;
		}
		/*entityを追加したことを確認するフラッグ：追加したらtrue*/
		boolean flag = false;
		for (FeedEntity entity : preTripUpdate.getEntityList()) {
			if(!entity.hasId()){
				continue;
			}
			if(vehicleid.equals(entity.getId())){
				newentity = getFeedEntity(userid,tripid,stopid,stopsequence,delay,ridenum,getoffnum,message,entity,vehicleid);
				tripupdates.addEntity(newentity);
				flag=true;
			}else{
				tripupdates.addEntity(entity);
			}
	    }
		if(!flag){
			newentity = getFeedEntity(userid,tripid,stopid,stopsequence,delay,ridenum,getoffnum,message,null,vehicleid);
			tripupdates.addEntity(newentity);
		}
		provider.setTripUpdates(tripupdates.build());
//		return vehiclePositions.build();
	}



	/**
	 *
	 * @param tripid
	 * @param stopid
	 * @param stopsequence
	 * @param arrivaltime
	 * @param delay
	 * @param ridenum
	 * @param getoffnum
	 * @return
	 */
	private Builder getFeedEntity(int userid,String tripid,String stopid,int stopsequence,int delay,int ridenum,int getoffnum,String message,FeedEntity preentity, String vehicleid){
		System.out.println("start create Feedentity");
		TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
	    tripDescriptor.setTripId(tripid);

	    TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
	    tripUpdate.setTrip(tripDescriptor);

	    StopTimeEvent.Builder delaybuilder = StopTimeEvent.newBuilder();
	    delaybuilder.setDelay(delay);

//	    ArrayList<Stop_Time> stoptimelist = routeprovider.getStop_Times().getStop_Time(Integer.parseInt(tripid));
		PreparedStatement check_stop_times_stmt;
		ResultSet result=null;
		try {
			check_stop_times_stmt = conn.prepareStatement("SELECT * FROM stop_times WHERE user_id =? AND trip_id=?");
			check_stop_times_stmt.setInt(1, userid);
			check_stop_times_stmt.setLong(2, Long.parseLong(tripid));
			result = check_stop_times_stmt.executeQuery();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


//	    System.out.println("stopsequence size:"+stoptimelist.size());
	    List<StopTimeUpdate> prestoptimeupdatelist= null;
	    if(preentity != null){
	    	TripUpdate tripupdate = preentity.getTripUpdate();
	    	prestoptimeupdatelist = tripupdate.getStopTimeUpdateList();
	    }

	    try {
			while(result.next()){
//	    	Stop_Time stoptime = stoptimelist.get(i);

				if(result.getInt("stop_sequences") < stopsequence){
					//今回のデータより前のStopsequenceのデータを引き継ぐフェイズ
					if(prestoptimeupdatelist !=null){
						StopTimeUpdate prestoptimeupdate = null;
						for(StopTimeUpdate stoptimeupdate :prestoptimeupdatelist){
							if(stoptimeupdate.getStopSequence() == result.getInt("stop_sequences"))prestoptimeupdate = stoptimeupdate;
						}
						if(prestoptimeupdate == null) continue;
						tripUpdate.addStopTimeUpdate(prestoptimeupdate);
					}
					continue;
				}
				if(stopsequence == result.getInt("stop_sequences")){

					//今回のデータぶっこむフェイズ
					System.out.println("create entity");
				    StopTimeUpdate.Builder stoptimeupdate = StopTimeUpdate.newBuilder();
				    stoptimeupdate.setStopId(stopid);
				    stoptimeupdate.setStopSequence(stopsequence);
				    System.out.println("message:"+message);
				    if(message.equals("arrival")){
				    	stoptimeupdate.setArrival(delaybuilder);
				    }
				    if(message.equals("departure")){
				    	StopTimeUpdate prestoptimeupdate = null;
						for(StopTimeUpdate stoptime :prestoptimeupdatelist){
							if(stoptimeupdate.getStopSequence() == result.getInt("stop_sequences")){
								prestoptimeupdate = stoptime;
								break;
							}
						}
						if(prestoptimeupdate == null) continue;
						StopTimeEvent arrival = prestoptimeupdate.getArrival();
						stoptimeupdate.setArrival(arrival);
				    	stoptimeupdate.setDeparture(delaybuilder);

				    }
				    PassengersDescriptor.Builder passengersdescriptor = PassengersDescriptor.newBuilder();
				    passengersdescriptor.setNumberOfPeopleGetOff(getoffnum);
				    passengersdescriptor.setNumberOfPeopleRide(ridenum);
				    passengersdescriptor.setStopsequence(String.valueOf(stopsequence));
				    passengersdescriptor.setTripId(tripid);
				    stoptimeupdate.setPassengers(passengersdescriptor);
				    tripUpdate.addStopTimeUpdate(stoptimeupdate);
				    continue;
				}
				//まだ来ててないデータを埋めるフェイズ
			     StopTimeUpdate.Builder stoptimeupdate = StopTimeUpdate.newBuilder();
			     stoptimeupdate.setStopId(String.valueOf(result.getLong("stop_id")));
			     stoptimeupdate.setStopSequence(result.getInt("stop_sequences"));
//			     stoptimeupdate.setArrival(arrival);
//			     stoptimeupdate.setArrival(arrival);
			     tripUpdate.addStopTimeUpdate(stoptimeupdate);
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	    FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
	    tripUpdateEntity.setId(vehicleid);
	    tripUpdateEntity.setTripUpdate(tripUpdate);

	    return tripUpdateEntity;

	}


	protected Boolean insertTripLog(int userid,String tripid,Timestamp date,String stopid,int stopsequence,int delay,int gotonnum,int gotoffnum,String message){
		if(userid < 0 || tripid == null || tripid.length() <=0 || date == null || stopsequence < 0){
			return false;
		}
		System.out.println("insert TripLog");

			Long tripupdateid = -1L;
			PreparedStatement stmt;
			if(message.equals("arrival")){
				String sql = "INSERT INTO tripupdate_log (userid, tripid, date, stop_sequence, stopid, arrival_delay,departure_delay,goton,gotoff) values(?,?,?,?,?,?,?,?,?)";
			tripupdateid=-1L;
			try {
				stmt = conn.prepareStatement(sql,new String[]{"route_id"});
				stmt.setLong(1, userid);
				stmt.setLong(2, Integer.parseInt(tripid));
				stmt.setTimestamp(3, date);
				stmt.setInt(4, stopsequence);
				stmt.setLong(5, Integer.parseInt(stopid));
				stmt.setInt(6, delay);
				stmt.setInt(7, 0);
				stmt.setInt(8, gotonnum);
				stmt.setInt(9, gotoffnum);
				stmt.executeUpdate();
				ResultSet res = stmt.getGeneratedKeys();
				if(res != null && res.next()) {
				    tripupdateid = res.getLong(1);
				}
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				return false;
			}
			if(tripupdateid == -1L){return false;}
			}else{
				Calendar c = Calendar.getInstance();
		        //フォーマットパターンを指定して表示する
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
		        String starttime = sdf.format(c.getTime())+"00:00:00";
		        String endtime = sdf.format(c.getTime())+"23:59:59";
		        System.out.println("at AddTripUpdate: day="+sdf.format(c.getTime()));
				String check_exists = "SELECT tripupdateid FROM tripupdate_log WHERE userid =? and tripid = ? and stop_sequence = ? and date <? and date >? order by tripupdateid DESC limit 1";
				try {
					PreparedStatement checkExist = conn.prepareStatement(check_exists);
					checkExist.setLong(1, userid);
					checkExist.setLong(2, Integer.parseInt(tripid));
					checkExist.setInt(3, stopsequence);
					checkExist.setString(4, endtime);
					checkExist.setString(5, starttime);
					ResultSet result = checkExist.executeQuery();
					if(result != null && result.next()) {
					    tripupdateid = result.getLong("tripupdateid");
					}
				} catch (SQLException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
				if(tripupdateid==-1L){System.out.println("at AddTripUpdate: ondeparture not found arrivaldata"); return false;}
				String update ="UPDATE tripupdate_log SET date = ?,departure_delay = ? WHERE tripupdateid = ?";
				try {
					stmt = conn.prepareStatement(update);
					stmt.setTimestamp(1, date);
					stmt.setInt(2, delay);
					stmt.setLong(3, tripupdateid);

					stmt.executeUpdate();
//					ResultSet res = stmt.getGeneratedKeys();
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					return false;
				}

			}
			return true;
	}

	/**
	 * useridさんのvehicleidを持つえんてぃてぃ削除
	 * @param vehicleid
	 * @param userid
	 */
	private void deleteFeedMessage(String vehicleid,String userid){
		FeedMessage.Builder tripupdate = GtfsRealtimeLibrary.createFeedMessageBuilder();
		FeedMessageProvider provider = (FeedMessageProvider)_context.getAttribute(userid);
		FeedMessage preVehiclePositions =(FeedMessage)getMessage(provider);
		for (FeedEntity entity : preVehiclePositions.getEntityList()) {
			if(!entity.hasId()){
				continue;
			}
			if(vehicleid.equals(entity.getId())){
			}else{
			tripupdate.addEntity(entity);
			}
		}
		provider.setTripUpdates(tripupdate.build());
	}

	@Override
	protected Message getMessage(FeedMessageProvider provider) {
		// TODO 自動生成されたメソッド・スタブ
		return provider.getTripUpdates();
	}

	@Override
	protected String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILENAME;
	}

}
