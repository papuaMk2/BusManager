package acount;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class AddVehicleNumber
 */
@WebServlet("/AddVehicleNumber")
public class AddVehicleNumber extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection conn=null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddVehicleNumber() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() throws ServletException{
    	super.init();
    	String url = "jdbc:mysql://localhost/busApp";
        String user = "busApp";
        String password = "busAppPW";


        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(url+"?user="+user+"&password="+password+"&autoReconnect=true");
        }catch (ClassNotFoundException e){
            log("ClassNotFoundException:" + e.getMessage());
        }catch (SQLException e){
            log("SQLException:" + e.getMessage());
        }catch (Exception e){
            log("Exception:" + e.getMessage());
        }
    }

    @Override
    public void destroy(){
    	super.destroy();
    	try {
			conn.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if(session.getAttribute("login") == null ){
			response.getWriter().println("Time out");
			return;
		}
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		URLDecoder _decoder = new URLDecoder();
		PrintWriter out = response.getWriter();
		String vehiclenumber = _decoder.decode(new String(request.getParameter("vehicleNumber").getBytes("ISO-8859-1")), "UTF-8");
		if(vehiclenumber == null || vehiclenumber.length() == 0){
			response.getWriter().println("Error");
			return;
		}
//		String vehiclenumber = new String(request.getParameter("vehicleNumber").getBytes("8859_1"), "utf-8");
/*		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		*/
		String userid = (String)session.getAttribute("userid");
		String sql = "SELECT * FROM vehiclenumber WHERE userid = ? && vehicleid = ?";
		PreparedStatement pstmt;
		boolean vehicleIdAvailable = Pdo.checkVehicleIdAvailable(Integer.parseInt(userid),vehiclenumber);
		if(!vehicleIdAvailable){
			out.print("not available");
			return;
		}
		int retryCount = 3;
        do{
		try {
			if(vehicleIdAvailable && Pdo.getVehicleID(Integer.parseInt(userid), vehiclenumber) != -1){
				//update vehicle isengaged =1
				if(Pdo.setVehicleIdEnabledState(Pdo.getVehicleID(Integer.parseInt(userid), vehiclenumber), true)){
					out.print("OK");
					return;
				}else{
					out.print("error");
					return;
				}

			}

/*			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
	        pstmt.setString(2, vehiclenumber);
	        ResultSet rs = pstmt.executeQuery();
			if (!rs.next()){*/
				sql = "INSERT INTO vehiclenumber(userid, vehicleid) VALUES("+userid+", '"+vehiclenumber+"');";
				pstmt = conn.prepareStatement(sql);
//				pstmt.setString(1,userid);
//				pstmt.setString(2, vehiclenumber);
				Boolean rs = pstmt.execute(sql);
				if(!rs){
					out.print("OK");
					return;
				}else{
					out.print("exist");
				}
			/*}else{
				out.print("exist");
			}*/
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			out.print("exist");
			e1.printStackTrace();
			String sqlState = e1.getSQLState();
			if (sqlState.equals("08S01")){
	        	retryCount--;
	        	}else{
	        	retryCount = 0;
	        	}
		}
        }while(retryCount>0);

	}

}
