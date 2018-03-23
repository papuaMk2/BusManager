package Gtfs_Servlet;

/**
 * Created by ryosuke on 2015/12/29.
 * ルート情報を格納するクラス
 */


import java.util.ArrayList;

public class RouteData{
    private String _routeshortname=null;
    private String _routelongname=null;
    private ArrayList<Integer> _weekcalender = new ArrayList<Integer>();
    private String _startdate = null;
    private String _enddate = null;
    private ArrayList<BusStation> _stops = new ArrayList<BusStation>();//添え字がstopsequence
    private ArrayList<Shape> _shapes = new ArrayList<Shape>();

    public void setName(String routeshortname, String routelongname){
        _routeshortname = routeshortname;
        _routelongname = routelongname;
    }

    public String getShortName(){return _routeshortname;}
    public String getLongName(){return _routelongname;}

    public void setRoutePeriod(String startdate,String enddate){
        _startdate = startdate;
        _enddate = enddate;
    }

    public Boolean hasName(){
        if(_routelongname == null || _routeshortname == null){
            return false;
        }
        return true;
    }

    public String getStartDate(){return _startdate;}
    public String getEndDate(){return _enddate;}

    public void setWeekCalender(ArrayList<Integer> weekcalender){
        _weekcalender = weekcalender;
    }

    public ArrayList<Integer> getWeekCalender(){return _weekcalender;}

    public void addRoute(String stopname,String stoplat,String stoplon,String arrivaltime,String departuretime){
        _stops.add(new BusStation(stopname,stoplat,stoplon,arrivaltime,departuretime));
    }

    public void addShape(double lat,double lon){
        _shapes.add(new Shape(lat, lon));
    }

    public ArrayList<BusStation> stops(){return _stops;}

    public ArrayList<Shape> getShapes(){return _shapes;}

    public class BusStation{
        private String _stopname;
        private String _stoplat;
        private String _stoplon;
        private String _arrivaltime;
        private String _departuretime;

        BusStation(String stopname,String stoplat,String stoplon,String arrivaltime,String departuretime){
            _stopname = stopname;
            _stoplat = stoplat;
            _stoplon = stoplon;
            _arrivaltime = arrivaltime;
            _departuretime = departuretime;
        }
        public String getName(){return _stopname;}
        public String getLat(){return _stoplat;}
        public String getLon(){return _stoplon;}
        public String getArrivalTime(){return _arrivaltime;}
        public String getDepartureTime(){return _departuretime;}

    }

    public class Shape{
        private double shape_pt_lat;
        private double shape_pt_lon;

        public Shape(double shape_pt_lat, double shape_pt_lon) {
            this.shape_pt_lat = shape_pt_lat;
            this.shape_pt_lon = shape_pt_lon;
        }

        public double getShape_pt_lat() {
            return shape_pt_lat;
        }

        public double getShape_pt_lon() {
            return shape_pt_lon;
        }

    }


}

