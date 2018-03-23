package com.ryosuke.gtfs_realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeProviderImpl;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class FeedMessageProvider extends GtfsRealtimeProviderImpl{
	private  FeedMessageProvider _this;
	private static String VEHICLE_POSITIONS_PATH = "/vehicle-positions.dat";
	private static String TRIP_UPDATE_PATH = "/trip-update.dat";
	private static boolean initializeflag = false; /*trueだとfeedfile読み込み済み*/

//	private FeedMessageProvider(){}

//	public  FeedMessageProvider getInstance(){return _this;}
	/**
	 * userdirectorypath
	 * @param directory
	 * @throws IOException
	 */
	public synchronized void initialize(String directory) throws IOException{
		System.out.println("initialize in provider");
		if(initializeflag == false){
			File vehiclepositionfilepath = new File(directory+VEHICLE_POSITIONS_PATH);
			if(vehiclepositionfilepath.exists()){
				FileInputStream _vehiclepositionspath;
				_vehiclepositionspath = new FileInputStream(vehiclepositionfilepath);
				setVehiclePositions(FeedMessage.parseFrom(_vehiclepositionspath));
			}
			File tripupdatefilepath = new File(directory+TRIP_UPDATE_PATH);
			if(tripupdatefilepath.exists()){
				FileInputStream _tripupdatepath;
				_tripupdatepath = new FileInputStream(tripupdatefilepath);
				setTripUpdates(FeedMessage.parseFrom(_tripupdatepath));
			}
			initializeflag = true;
		}
	}


}
