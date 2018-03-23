package announce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.MySingleton;

@WebServlet(name = "requestUpdateList", urlPatterns = { "/requestUpdateList" })
public class RequestUpdateList extends HttpServlet{

	/**
	 * 受け取ったリストとサーバーの更新リストを比較し、サーバー側の法が新しいリストを作成し返す
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
//		response.setContentType("text/html; charset=utf-8");
		System.out.println("announce logincheck:"+session.getAttribute("login"));
		if(session.getAttribute("login") == null ){
			response.getWriter().println("Time out");
			return;
		}
		response.setContentType("text/html; charset=utf-8");
	    ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());

	    String directory = MySingleton.getInstance().getUserDataDirectory()+"/"+session.getAttribute("userid");
	    request.setCharacterEncoding("utf-8");
	    Map<String,String[]> map = request.getParameterMap();

	    AnnounceUpdateList list = null;
	    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(directory+"/"+MySingleton.getAnnounceUpdateName()))) {
		    list = (AnnounceUpdateList)ois.readObject();
		    ois.close();
        } catch (IOException e) {
        	response.getWriter().println("error");
        	return;
        } catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	    if(list == null){
	    	list = new AnnounceUpdateList();
	    }
    	list = update(directory,list);
	    AnnounceUpdateList result = new AnnounceUpdateList();
	    for(Map.Entry<String, String[]> entry: map.entrySet()){
//	    	System.out.println("filename:"+entry.getKey()+" lastmodified:"+entry.getValue()[0]+" server:"+list.get(entry.getKey()));
	    	Long local = Long.parseLong(entry.getValue()[0]);
	    	Long server = list.get(entry.getKey());
	    	if(server == null){continue;}
	    	if(local < server){
	    		result.put(entry.getKey(), server);
	    	}
	    }
	    out.writeObject(result);

	    out.close();
	}

	private AnnounceUpdateList update(String directory,AnnounceUpdateList list){
		if(list.size()==0)return null;
		for(Map.Entry<String, Long> entry: list.entrySet()){
			File file = new File(directory+"/announcedata/"+entry.getKey());
			if(entry.getValue()<file.lastModified()){
				list.put(entry.getKey(), file.lastModified());
//				System.out.println("filename:"+entry.getKey()+" lastmodified:"+list.get(entry.getKey()));
			}
		}

		return list;
	}
}
