package Gtfs_Servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.protobuf.Message;
import com.ryosuke.gtfs_realtime.FeedMessageProvider;

import utils.MySingleton;

/**
 * Servlet implementation class AbstractAddServlet
 */
abstract class AbstractAddServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected static final String CONTENT_TYPE = "application/x-google-protobuf";
    protected ServletContext _context;
//    protected FeedMessageProvider _gtfsrealtimeprovider;
    protected String providername = "feedprovider";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AbstractAddServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		String latitude = request.getParameter("Latitude");
//		_gtfsrealtimeprovider = FeedMessageProvider.getInstance();

/*		System.out.println("doget in abstractservlet");
		Message message = getMessage();
		response.getWriter().print(message);
		response.getWriter().println("waki");*/
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if(_context.getAttribute((String)session.getAttribute("userid")) == null){
			FeedMessageProvider _gtfsrealtimeprovider = new FeedMessageProvider();
			try {
				String path = "/"+session.getAttribute("userfoldername");
				String directory = MySingleton.getInstance().getUserDataDirectory()+session.getAttribute("userfoldername");
				_gtfsrealtimeprovider.initialize(directory);
				System.out.println("catch in init ");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

			_context.setAttribute((String)session.getAttribute("userid"),_gtfsrealtimeprovider);
		}
		response.getWriter().append("Served at servlet: waki").append(request.getContextPath());
	}

	@Override
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		_context = getServletConfig().getServletContext();
/*		try {
			_gtfsrealtimeprovider.initialize(_context);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if(_context.getAttribute(providername)==null){
			_context.setAttribute(providername,_gtfsrealtimeprovider);}*/
	}

	protected abstract Message getMessage(FeedMessageProvider provider);

	protected abstract String getFileName();
//	protected abstract FeedMessage buildFeedMessage();

	/*
	 * _pathはフィードファイルの絶対パス
	 * このセッションを利用しているユーザーが利用しているfeedmessageprovider
	 */
	protected synchronized void writeMessageToFile(String directory, FeedMessageProvider provider) throws IOException {
	    Message message = getMessage(provider);
	    File newdirectory = new File(directory);
	    newdirectory.mkdir();
	    OutputStream out = new BufferedOutputStream(new FileOutputStream(directory+"/"+getFileName()));
	    message.writeTo(out);
	    out.close();
	  }
}
