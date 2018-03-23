package announce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.MySingleton;
import utils.ZipCompressUtils;

/**
 * filenameユーザーフォルダからの相対パス
 *
 * @author ryosuke
 *
 */
@WebServlet(name = "FileDownload", urlPatterns = { "/FileDownload" })
public class FileDownload extends HttpServlet {
	private ServletContext _context;
	private String ZIP_FILE_NAME = "announce.zip";
	private static String NORMALMODE = "normalmode";
	static String ZIPONLYMODE = "ziponlymode";

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("init in servlet");
		super.init(config);
		_context = getServletConfig().getServletContext();
		// ZIP_FILE_PATH = _context.getRealPath("/"+ZIPFILE_NAME);
		// DIRECTORY_PATH = _context.getRealPath("/sample-feed");
		// _path = new File(_context.getRealPath("/"+ZIPFILE_NAME));
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
		// response.setContentType("text/html; charset=utf-8");
		System.out.println("filedownload logincheck:" + session.getAttribute("login"));
		if (session.getAttribute("login") == null){
			response.getWriter().println("Time out");
			return;
		}
		response.setContentType("text/html; charset=utf-8");
		//ファイルがリストかひとつのファイルか調べる
		String downloadMode = NORMALMODE;
		String filename = request.getParameter("filename");
		String[] files = null;
		ArrayList<String> zipSource = new ArrayList<String>();
		String directory = MySingleton.getInstance().getUserDataDirectory()
				+ session.getAttribute("userfoldername")/* +"/announce_feed/announcedata" */;

		//圧縮するファイルリストの作成
		if(filename != null){
			zipSource.add(directory+"/"+filename);
		}else{
			if((files = request.getParameterValues("files[]")) != null){
//				zipSource = new ArrayList<String>(Arrays.asList(files));
				for (String file : files) {
					zipSource.add(directory+"/"+file);
				}
			}else{
				System.out.println("get filelist failed");
			}

		}

//		if(downloadMode == null){response.getWriter().println("file name not found");}
		AnnounceProvider provider;
		String PROVIDERNAME = MySingleton.getAnnounceProviderName();
		int userid = Integer.parseInt((String) session.getAttribute("userid"));
		// announceproviderの取得
		if ((provider = (AnnounceProvider) _context.getAttribute(userid + ":" + PROVIDERNAME)) == null) {
			_context.setAttribute(userid + ":" + PROVIDERNAME, provider = new AnnounceProvider(directory));
		}


//		if(downloadMode == FILEMODE){
		for(String file : zipSource){
			File targetFile = new File(file);
			// if(!isZip(directory+"/"+filename) ||
			// filename.equals("google_transit.zip")){
			if (!targetFile.exists()) {
				response.getWriter().println("Not found");
				return;
			}
			//zipファイルの確認 zipファイルのみだったらzipオンリーモード ほかにもあるならzipはむし
			if(isZip(file)){
				if(zipSource.size()==1){
					downloadMode = ZIPONLYMODE;
					break;
				}
				response.getWriter().println("Error:Target files includes zip file ");
				return;
			}

			if (targetFile.isDirectory()) {
				response.getWriter().println("Error:Target file is Directory");
				return;
			}


		}
		// }

		for (Iterator iterator = zipSource.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
		}
		String zipFileName = null;//ダウンロードされるファイル
		String filePath = null;//ダウンロードされるファイルの絶対パス
		if(downloadMode.equals(ZIPONLYMODE)){
			zipFileName = filename;
			filePath = directory + "/" + zipFileName;
		}else{
			zipFileName = "downloadfile.zip";
			filePath = directory + "/" + zipFileName;// zipfile出力先
			new File(new File(filePath).getParent()).mkdirs();
			ZipCompressUtils.compressFileList(filePath, zipSource);
		}

		// File preFile = new File(filePath);
		// if(preFile.exists()){preFile.delete();}

//		Boolean deleteFlag = false;
		/*
		 * zipfile作成 filePath zipfileの出力先
		 */

		//filepathはレスポンスで返すファイルのパス zipfilenameはレスポンスで返すファイルの名前
		InputStream in = null;
		OutputStream out = null;
		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "filename=\"" + zipFileName + "\"");
			in = new FileInputStream(filePath);
			out = response.getOutputStream();
			int len = 0;
			while ((len = in.read()) != -1) {
				out.write(len);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
					if (downloadMode == NORMALMODE) {
						 File file = new File(filePath);
//						 if(file.exists())file.delete();
					}
				} catch (IOException e) {
				}
			}
		}
	}

	private String convertToZip(String path) {
		String filename = path;
		if (filename.lastIndexOf("/") != -1) {
			filename = path.substring(path.lastIndexOf("/") + 1);
		}
		if (filename.lastIndexOf(".") != -1) {
			filename = filename.substring(0, filename.lastIndexOf("."));
			filename += ".zip";
		}
		return filename;
	}

	private Boolean isZip(String filename) {
		if (filename.lastIndexOf(".") != -1) {
			filename = filename.substring(filename.lastIndexOf("."));
		}
		Boolean result = false;
		if (filename.equals(".zip")) {
			result = true;
		}
		return result;
	}

}
