package Gtfs_Servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Gtfs_Servlet.RouteData.BusStation;
import utils.ZipCompressUtils;


/**
 * Servlet implementation class AddRouteData
 */
@WebServlet("/AddRouteData")
public class AddRouteData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ServletContext _context = null;
	private String PROVIDERNAME = "provider"; //_contextにインスタンスを登録するときに利用する名前　＊例:userid:PROVIDENAME

	private String ZIP_FILE_PATH = null;   //圧縮したファイルの絶対パス
	private String DIRECTORY_PATH = null;  //圧縮するフォルダの絶対パス
	private String ROUTEINSTANCE = "routeinstance";  //sessionに関連付けられているルートインスタンスの名前

	/**
	 * 例：圧縮後ファイルへの絶対パス
	 * 　　CONTEXT_DIRECTORY+"/"+session.getAttribute("username")+"/"+ZIPFILE_NAME　
	 */
	private String ZIPFILE_NAME = "google_transit.zip";  //圧縮後のファイル名
	private String DIRECTORY_NAME = "google_transit_feed";  //圧縮するフォルダのフォルダ名
	private String CONTEXT_DIRECTORY = null;
	private String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";

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
		ZIP_FILE_PATH = _context.getRealPath("/"+ZIPFILE_NAME);
		DIRECTORY_PATH = _context.getRealPath("/sample-feed");
//		_path = new File(_context.getRealPath("/"+ZIPFILE_NAME));
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
//		response.setContentType("text/html; charset=utf-8");
		System.out.println("session:"+session);
		System.out.println("logincheck:"+session.getAttribute("login"));
        PrintWriter out = response.getWriter();
		if(session.getAttribute("login") == null ){
			out.println(-1);
			return;
			}
		System.out.println("message:"+request.getParameter("message"));
		Enumeration names = request.getParameterNames();
		System.out.println("parameter name");
	    while (names.hasMoreElements()){
	      String name = (String)names.nextElement();
	      System.out.println(name);
	    }
        Map<String,String[]> params = request.getParameterMap();
//        System.out.println(userdata.get("message"));

        String message = null;
        message = request.getParameter("message");
        System.out.println("message :"+message);
        if(message == null){
        	message = params.get("message")[0];
        }
//        String pass = params.get("password")[0];
		if(message!= null){
			RouteData route;
			System.out.println("message:"+message);
			/**
			 * messageの内容により分岐
			 * start:ルートの登録開始
			 * finish:ルート登録終了
			 * stopdata:バス停情報
			 * routename:路線の名前情報
			 * routeperiod:路線が運行される期間
			 * weekcalender:運行される曜日
			 */
			switch(message){
				case "start": //新規ルートインスタンスの生成とsessionへの登録
					System.out.println("message start");
						session.setAttribute(ROUTEINSTANCE,new RouteData());
					break;

				case "finish"://fileへの書き込みとsessionからのインスタンスの破棄
					System.out.println("message finish");
					if((route = (RouteData)session.getAttribute(ROUTEINSTANCE))!=null){
						if(route.hasName()){
							out.print(addRouteData(route,session));
							session.removeAttribute(ROUTEINSTANCE);
						}
					}
					break;

				case "stopdata":
					System.out.println("message stopdata");
					if((route = (RouteData)session.getAttribute(ROUTEINSTANCE))!=null){
						String stopname = new String(request.getParameter("name").getBytes("8859_1"),"utf-8");
						System.out.println("routename:"+stopname);
						String stoplat = request.getParameter("lat");
						String stoplon = request.getParameter("lon");
						String arrivaltime = request.getParameter("arrivaltime");
						String departuretime = request.getParameter("departuretime");
						if(stopname.length() > 0){
							route.addRoute(stopname,stoplat,stoplon,arrivaltime,departuretime);
						}
					}
					break;

				case "routename":
					System.out.println("message routename");
					if((route = (RouteData)session.getAttribute(ROUTEINSTANCE))!=null){
//						String shortname = new String(request.getParameter("shortname").getBytes("8859_1"), "utf-8");
//						String longname = new String(request.getParameter("longname").getBytes("8859_1"),"utf-8");
						String shortname = new String(params.get("shortname")[0].getBytes("8859_1"), "utf-8");
						String longname = new String(params.get("longname")[0].getBytes("8859_1"),"utf-8");
						//String shortname = URLDecoder.decode(request.getParameter("shortname"),"utf-8");
						//String longname = URLDecoder.decode(request.getParameter("longname"),"utf-8");;
						if(shortname.length() > 0 && longname.length() > 0)route.setName(shortname, longname);
					}
					break;

				case "routeperiod":
					System.out.println("message period");
					if((route = (RouteData)session.getAttribute(ROUTEINSTANCE))!=null){
//						String startdate = request.getParameter("startdate");
//						String enddate = request.getParameter("enddate");
						String startdate = params.get("startdate")[0];
						String enddate = params.get("enddate")[0];
						if(startdate.equals("NO_DATA") || enddate.equals("NO_DATA")){
							startdate = "0000:00:00";
							enddate = "0000:00:00";
						}
						route.setRoutePeriod(startdate, enddate);
					}
					break;

				case "weekcalender":
					System.out.println("message calender");
					if((route = (RouteData)session.getAttribute(ROUTEINSTANCE))!=null){
						ArrayList<Integer> weekcalender = new ArrayList<Integer>();
//						weekcalender.add(Integer.parseInt(request.getParameter("monday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("tuesday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("wednesday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("thursday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("friday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("saturday")));
//						weekcalender.add(Integer.parseInt(request.getParameter("sunday")));
						weekcalender.add(Integer.parseInt(params.get("monday")[0]));
						weekcalender.add(Integer.parseInt(params.get("tuesday")[0]));
						weekcalender.add(Integer.parseInt(params.get("wednesday")[0]));
						weekcalender.add(Integer.parseInt(params.get("thursday")[0]));
						weekcalender.add(Integer.parseInt(params.get("friday")[0]));
						weekcalender.add(Integer.parseInt(params.get("saturday")[0]));
						weekcalender.add(Integer.parseInt(params.get("sunday")[0]));
						route.setWeekCalender(weekcalender);
					}
					break;

				default:
					System.out.println("message error");
			}
		}
	}


	private synchronized int addRouteData(RouteData route,HttpSession session) throws IOException{
		RouteDataProvider provider;

		String userdatadirectory = USERDATA_DIRECTORY+session.getAttribute("userfoldername");
		String providername = session.getAttribute("userid")+":"+PROVIDERNAME; //_contextに登録するproviderの名前

		int tripid = -1;
		if((provider = (RouteDataProvider)_context.getAttribute(providername))==null){
			_context.setAttribute(providername, provider = new RouteDataProvider(userdatadirectory));
		}
		//バス停の数が２以上であければルートでないため登録しない
		ArrayList<BusStation> _route = route.route();
		if(_route.size() > 1){

		/**
		 * Routesフィード作成
		 */
		RoutesFeed routefeed = provider.getRoutes();
		int routeid = -1;
		if(route.getEndDate()!=null && route.getStartDate()!=null)routeid = routefeed.addRoute(route.getShortName(), route.getLongName());

		/**
		 * calendeフィード作成
		 */
		CalenderFeed calenderfeed = provider.getCalender();
		int serviceid = -1;
		if(route.getWeekCalender().size() > 1 && route.getEndDate()!=null && route.getStartDate()!=null){
			serviceid = calenderfeed.addCalender(route.getWeekCalender(), route.getStartDate(), route.getEndDate());
		}

		/**
		 * tripsフィード作成
		 */
		TripsFeed tripfeed = provider.getTrips();
		System.out.println("addtripfeed routeid:"+routeid+" serviceid:"+serviceid);
		if(routeid > 0 && serviceid > 0)tripid = tripfeed.addTrip(routeid, serviceid);

		/**
		 * Stopsフィードの作成
		 */
		StopsFeed stopsfeed = provider.getStops();
		Stop_TimesFeed stop_timesfeed = provider.getStop_Times();
			for(int i=0;i<_route.size();i++){
				BusStation stop = _route.get(i);
				Integer stopid;
				if((stopid = stopsfeed.hasName(stop.getName()))==null){
					stopid = stopsfeed.addStop(stop.getName(),stop.getLat(),stop.getLon());
				}
				stop_timesfeed.addStop_Time(tripid, stop.getArrivalTime(), stop.getDepartureTime(), stopid,i);
			}
		}
		writeDataToFile(userdatadirectory, provider);
		return tripid;
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

	    provider.writeDataToFile(directory);
	    ZipCompressUtils.compressDirectory(zipfilepath, directorypath);
	  }

}
