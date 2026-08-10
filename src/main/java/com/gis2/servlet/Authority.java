package com.gis2.servlet;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import com.data.file.MetaInfo;
import com.data.util.MapLog;
import com.gis2.storage.AuthorityMng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapplan.Function;
import com.mapplan.Header;
import com.mapplan.Protocol;
import com.mapplan.User;
import com.mapplan.Users;

public class Authority extends HttpServlet {
	
	Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 *  Users 객체의 인증 정보를 복사한 객체를 전달
	 * @param users
	 * @return
	 */
	public Users getUsers(Users users) {
		
		Users nus = new Users();
		
		List<User> nusList = nus.getUser();
		
		List<User> usList =  users.getUser();
		
		for(User usr : usList) {
			User nusr = new User();
			nusList.add(nusr);
			
			nusr.setKey(usr.getKey());
			nusr.setStartTime(usr.getStartTime());
			nusr.setEndTime(usr.getEndTime());
			
			if(usr.getDomain().size() > 0) {
				nusr.getDomain().addAll(usr.getDomain());
			}
			
			if(usr.getFunction().size() > 0) {
				List<Function> funcList = usr.getFunction();
				
				for(Function func : funcList) {
					Function nfunc = new Function();
					
					nfunc.setName(func.getName());
					nfunc.setDayCnt(func.getDayCnt());
					nfunc.setCnt(func.getCnt());
					
					nusr.getFunction().add(nfunc);
				}
				
			}
			
		}
		
		
		return nus;
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String ip = MapService.getClientIP(request);
		
		if( ip.startsWith("192.168.") || ip.startsWith("localhost") ) {
			
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        return;
		}
		
		String json = "";
		
		String req = request.getParameter("req");
		
		Protocol pt = new Protocol();
		
		Header header = new Header();
		header.setResult("0000");
		header.setResultDesc("정상");
		header.setReq(req);
		pt.setHeader(header);
		
        // 요청 본문(JSON 데이터) 읽기
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }
        // 출력으로 데이터 확인
        
        Users us = AuthorityMng.gson.fromJson(jsonString.toString(), Users.class);
        
        if(us.getUser().size() > 0) {
    		header.setResult("0000");
    		header.setResultDesc("정상");
            AuthorityMng.users = us;
            AuthorityMng.writeUsersFile();
            MapLog.getSCLog().debug("post로 인증정보 전달 받음");
        }
        else {
        	MapLog.getSCLog().debug("post로 인증정보 전달 오류");
    		header.setResult("1000");
    		header.setResultDesc("오류");
        }
        
        json = gson.toJson(pt);
        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
	}	
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String ip = MapService.getClientIP(request);
		
		if( ip.startsWith("192.168.") || ip.startsWith("localhost") ) {
			
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        return;
		}
		
		String req = request.getParameter("req");
		
		Protocol pt = new Protocol();
		
		Header header = new Header();
		header.setResult("0000");
		header.setResultDesc("정상");
		header.setReq(req);
		
		pt.setHeader(header);
		
		String json = "";
		
		if(!req.equals("ack")) {
			MapLog.getSCLog().debug("req = " + req+", isAuth="+AuthorityMng.isAuthority);
		}
		
		if(!AuthorityMng.isAuthority) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "현재 서버는 인증을 하지 않는다");
			return;
		}
		
		
		if(req.equals("users")) {
			
			
			String key = request.getParameter("key");
			if(key == null) {
				
				Users users = this.getUsers(AuthorityMng.users);
				
				//pt.setBody(AuthorityMng.users);
				pt.setBody(users);
				json = gson.toJson(pt);
			}
			else {
				String[] keys = key.split("\\|");
				Users users = new Users();
				for(User user : AuthorityMng.users.getUser()) {
					
					for(String keyCode : keys) {
						if(user.getKey().equals(keyCode)) {
							users.getUser().add(user);
						}
					}
				}
				users = this.getUsers(users);
				pt.setBody(users);
				json = gson.toJson(pt);
			}
			
			
		}
		else if(req.equals("user")) {
			
			//System.out.println("url1 = " + request.getRequestURI()+request.getQueryString());
			
			String key = request.getParameter(Constant.key);
			
			String funName = request.getParameter(Constant.functionName);
			
			String tileName = request.getParameter(Constant.tileName);
			
			String layerName = request.getParameter(Constant.layerName);
			
			int cnt = Integer.parseInt(request.getParameter(Constant.count));
			
			int ok = AuthorityMng.addCnt(key, funName, tileName, layerName, cnt);
			
			if(ok == -1) {
				//오류
			}
			json = gson.toJson(pt);
		}
		else if(req.equals("update")){
			
			//Users dbUsers = AuthorityMng.dbAuth.getDBAuth();
			
			boolean isUpdate = AuthorityMng.updateDBAuth();
			
			if(isUpdate == true) {
				AuthorityMng.deployUsers();
			}
			else {
				
				header.setResult("1000");
				header.setResultDesc("오류");
				
				MapLog.getSCLog().debug("인증 데이타베이스 업데이트 오류");
			}
			json = gson.toJson(pt);
		}
		else {
			json = gson.toJson(pt);
		}
		
		String callBack = request.getParameter("callback");
		if (callBack == null || callBack.indexOf("http") > -1) {
			response.setContentType("application/json; charset=utf-8");
			byte[] bodyByte = json.getBytes("utf-8");
			BufferedOutputStream boss = new BufferedOutputStream(response.getOutputStream());
			boss.write(bodyByte);
			boss.flush();

			boss.close();
		} else {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "utf-8"));
			bw.write(callBack + "(" + json + ")");
			bw.flush();
			bw.close();
		}
		
	}
}
