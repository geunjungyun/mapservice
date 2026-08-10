package com.gis2.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.data.file.MetaInfo;
import com.data.util.MapLog;
import com.gis2.servlet.Constant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapplan.Content;
import com.mapplan.Function;
import com.mapplan.Header;
import com.mapplan.Protocol;
import com.mapplan.User;
import com.mapplan.Users;
import com.util2.thread.CustomThreadPoolExecutor;
import com.util2.thread.TileThreadPoolExecutor;


import org.json.JSONObject;

public class AuthorityMng implements ServletContextListener {
	
	static public Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();	
	
	static ServletContext sc = null;
	
	public static Users users = new Users();
	
	public static boolean isAuthority = false;
	
	
	//연결 서버 리스트
	public static Vector<String> conServers = new Vector();
	
	public static ConcurrentMap<String, Boolean> liveServers = new ConcurrentHashMap<>();
	
	
	public static String local = "";
	
	public static String uri = "";
	
	public static CustomThreadPoolExecutor tpool;
	
	public static String dbUrl = "";
	public static String dbUser = "";
	public static String dbPassword = "";
	public static String serverGroup = "";
	
	public static DBAuth dbAuth = null;
	
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		sc = sce.getServletContext();
		
		tpool = new CustomThreadPoolExecutor(50, 50, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		
		this.init();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
	}
	
	public static Users getUsers() {
		return AuthorityMng.users;
	}
	
	
	public static Vector<String> getUserTileNames(String key){
		
		//Vector<String> tileNames = new Vector();
		
		HashMap<String, String> tileNames = new HashMap();
		
		List<User> userList = users.getUser();
		
		User selectUser = null;
		
		for(User user : userList) {
			if(key.equals(user.getKey())) {
				selectUser = user;
				break;
			}
		}
		
		/*
		if(selectUser != null) {
			List<Function> functs = selectUser.getFunction();
			for(Function func : functs) {
				if(func.getName().toLowerCase().equals(Constant.tile)) {
					
					List<Content> cts = func.getContent();
					//타일서비스 전체
					if(cts.size() == 0) {
						Enumeration<com.gis2.storage.Version> versions = TileServiceMng.versions.elements();
						while (versions.hasMoreElements()) {
							com.gis2.storage.Version ver = versions.nextElement();
							tileNames.add(ver.name);
						}
					}
					else {
						for(Content ct : cts) {
							tileNames.add(ct.getName());
						}
					}
				}
			}
		}
		*/
		
		if(selectUser != null) {
			List<Function> functs = selectUser.getFunction();
			for(Function func : functs) {
				
				String tileSet = func.getName().split("\\.")[0];
				
				if(tileSet.startsWith("emp") || tileSet.startsWith("emap")) {
					continue;
				}
				
				Enumeration<com.gis2.storage.Version> versions = TileServiceMng.versions.elements();
				while (versions.hasMoreElements()) {
					com.gis2.storage.Version ver = versions.nextElement();
					
					if(ver.name.startsWith(tileSet)) {
						tileNames.put(ver.name, ver.name);
					}
				}
				
				
				//타일서비스 전체
				/*
				if(cts.size() == 0) {
					Enumeration<com.gis2.storage.Version> versions = TileServiceMng.versions.elements();
					while (versions.hasMoreElements()) {
						com.gis2.storage.Version ver = versions.nextElement();
						tileNames.add(ver.name);
					}
				}
				else {
					for(Content ct : cts) {
						tileNames.add(ct.getName());
					}
				}
				*/
				
			}
		}
		
		Vector<String> names = new Vector();
		Set set = tileNames.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			String name = (String)it.next();
			names.add(name);
		}
		
		return names;
	}
	
	public static void initCallCnt() {
		List<User> userList = users.getUser();
		for(User us : userList) {
			List<Function> functs = us.getFunction();
			for(Function fun : functs) {
				fun.setCnt(0);
				List<Content> contList = fun.getContent();
				for(Content cont : contList) {
					cont.setCnt(0);
				}
			}
		}
	}
	
	
	public static boolean isDomain(List<String> regDomains, String domain) {
		
		boolean isOk = false;
		
		for(String regDo : regDomains) {
			if(regDo.indexOf("*") > -1) {
				int idx = regDo.lastIndexOf("*");
				
				String sudo = regDo.substring(0, idx-1);
				
				if(domain.startsWith(sudo)) {
					isOk = true;
					break;
				}
				
			}
			else if(regDo.equals(domain)){
				isOk = true;
				break;
			}
		}
		
		
		return isOk;
	}
	
	/**
	 * 
	 * @param key
	 * @param req
	 * @param tileName tileSet | tileName
	 * @param layer tileSet | layerName
	 * @return 1 권한 오류, 2 응답 개수 초과, 0 정상
	 */
	public static boolean isOK(String key, String req, String tileName, String layer, String domain, Header header) {

		boolean ok = true;
		
		if(req.equals(Constant.update) || req.equals(Constant.updatetile)) {
			return true;
		}
		
		List<User> userList = users.getUser();
		
		User selectUser = null;
		
		for(User user : userList) {
			if(key.equals(user.getKey())) {
				selectUser = user;
				
				//if(user.getDomain().size() > 0  && !user.getDomain().contains(domain)) {
				if(user.getDomain().size() > 0  && ! isDomain(user.getDomain(), domain) ) {
					header.setResult(Constant.authNotDomain+"");
					header.setResultDesc(Constant.authNotDomainDesc + domain);
					return false;
				}
				
				LocalDateTime startL = null;
				LocalDateTime endL = null;
				
				if(user.getStartTime() != null) {
					startL = Timestamp.valueOf(user.getStartTime()).toLocalDateTime();
				}
				
				if(user.getEndTime() != null) {
					endL  = Timestamp.valueOf(user.getEndTime()).toLocalDateTime();
				}
				
				LocalDateTime now = LocalDateTime.now();
				
				if(startL != null || endL != null) {
					if(startL == null) {
						startL = Timestamp.valueOf("2024-12-20 10:00:00").toLocalDateTime();
					}
					
					if(endL == null) {
						endL = Timestamp.valueOf("2034-12-20 10:00:00").toLocalDateTime();
					}
					
					if(now.isAfter(startL) && now.isBefore(endL)) {
						
					}
					else {
						header.setResult(Constant.authNotKey+"");
						header.setResultDesc(Constant.authNotKeyDesc +  key);
						return false;
					}
				}
				
			}
		}
		
		Function selectFunc = null;
		
		if(selectUser == null) {
			header.setResult(Constant.authNotKey+"");
			header.setResultDesc(Constant.authNotKeyDesc +  key);
			return false;
		}
		else {
			
			List<Function> fucs = selectUser.getFunction();
			
			for(Function fuc : fucs) {
				//if(  req.equals(fuc.getName())  ) {
				//req 가 존재하는 기능 이름에 존재하는지 판단
				
				//String funcName = getFunc(req);
				
				String funcName = getFunc(tileName, layer);
				
				if(funcName!= null &&  fuc.getName().equals(funcName) ) {
					selectFunc = fuc;
					break;
				}
			}
		}
		
		if(selectFunc == null) {
			header.setResult(Constant.authNotFunc+"");
			header.setResultDesc(Constant.authNotFuncDesc + req);
			return false;
		}
		else {
			
			if( (tileName == null && layer == null) ||  selectFunc.getContent().size() == 0 ) {
				if(selectFunc.getCnt() >= selectFunc.getDayCnt()) {
					header.setResult(Constant.authNotCnt+"");
					header.setResultDesc(Constant.authNotCntDesc + selectFunc.getDayCnt());
					return false;
				}
				else {
					return true;
				}
			}
			else if(tileName != null || layer != null){
				
				String contentName =  (tileName == null ? layer.toLowerCase() : tileName.toLowerCase());
				
				Content selectCt = null;
				
				List<Content> ctL = selectFunc.getContent();
				for(Content ct : ctL) {
					if(contentName.startsWith(ct.getName()) ) {
						selectCt = ct;
						break;
					}
				}
				
				if(selectCt == null) {
					header.setResult(Constant.authNotFunc+"");
					header.setResultDesc(Constant.authNotFuncDesc);
					return false;
				}
				
				if(selectCt.getCnt() >= selectCt.getDayCnt()) {
					header.setResult(Constant.authNotCnt+"");
					header.setResultDesc(Constant.authNotCntDesc + selectFunc.getDayCnt());
					return false;
				}
				else {
					return true;
				}
				
			}
			
		}
		
		return ok;
	}
	
	
	public static boolean isAllFunc(String req) {
		Vector<String> list = Constant.getOpenApiNames();
		for(String func : list) {
			if(func.equals(req)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getTileFunc(String req) {
		
		Vector<String> list = Constant.getOpenApiTile();
		
		for(String func : list) {
			if(func.equals(req)) {
				return Constant.TILE;
			}
		}
		return null;
	}
	
	public static String getAnalysisFunc(String req) {
		
		Vector<String> list = Constant.getOpenApiAnalysis();
		
		for(String func : list) {
			if(func.equals(req)) {
				return Constant.ANALYSIS;
			}
		}
		return null;
	}
	
	public static String getFunc1(String req) {
		String name = null;
		name = getTileFunc(req);
		
		if(name == null) {
			name = getAnalysisFunc(req);
		}
		
		return name;
	}
	
	public static String getFunc(String tile, String layer) {
		
		String name = null;
		
		if(tile != null && tile.length() > 0) {
			name = tile.split("\\|")[0]+".tile";
		}
		else if(layer != null && layer.length() > 0) {
			name = layer.split("\\|")[0]+".layer";
		}
		
		return name;
	}

	
	
	public static Object lock = new Object();
	
	public static int addCnt(String key, String req, String tileName, String layer, int count) {
		
		//System.out.println("addCnt  key="+key+",req="+req+",tileName="+tileName+",layerName="+layer+",count="+count);

		int cntResult = -1;
		
		List<User> userList = users.getUser();
		
		User selectUser = null;
		
		for(User user : userList) {
			if(key.equals(user.getKey())) {
				selectUser = user;
				break;
			}
		}
		
		Function selectFunc = null;
		
		if(selectUser == null) {
			return -1;
		}
		else {
			List<Function> fucs = selectUser.getFunction();
			for(Function fuc : fucs) {
				//String funcName = getFunc(req);
				String funcName = getFunc(tileName, layer);
				if(funcName!= null &&  fuc.getName().equals(funcName) ) {
					selectFunc = fuc;
					break;
				}
			}
		}
		
		if(selectFunc == null) {
			return -1;
		}
		else {
			
			if( (tileName == null && layer == null) ||  selectFunc.getContent().size() == 0 ) {
				
				synchronized(lock) {
					int cnt = selectFunc.getCnt();
					selectFunc.setCnt(count > 0 ? count : ++cnt);
				}
				
				cntResult = selectFunc.getCnt();
			}
			else if(tileName != null || layer != null){
				
				String contentName =  (tileName == null ? layer.toLowerCase() : tileName.toLowerCase());
				
				Content selectCt = null;
				
				List<Content> ctL = selectFunc.getContent();
				for(Content ct : ctL) {
					//if(ct.getName().equals(contentName) ) {
					if(contentName.startsWith(ct.getName()) ) {
						selectCt = ct;
						break;
					}
				}
				
				if(selectCt == null) {
					return -1;
				}
				synchronized(lock) {
					int cnt = selectCt.getCnt();
					selectCt.setCnt(count > 0 ? count : ++cnt);
				}
				cntResult = selectCt.getCnt();
			}
			
		}
		
		return cntResult;
	}
	
	// 최초 연결 서버 
	// 
	// 최초 사용자 요청 개수는 어떻게 연동 할것 인지
	public void init() {
		Properties enginProperty = new Properties();
		System.out.println("AuthorityMng contextInitialized Loading");
		try {
			// Get Engine config path
			String getContextPath = sc.getContextPath();

			String meta_info_path = sc.getRealPath("WEB-INF");
			String cur_dir = System.getProperty("user.dir");
			
			System.out.println("jetty.home " + System.getProperty("jetty.home"));
			
			File path = new File(
					cur_dir + File.separator + "map_service" + File.separator + "service_config.properties");

			System.out.println("Properties read start");

			if (path.exists()) {
				FileInputStream fiss = new FileInputStream(path);
				enginProperty.load(fiss);
				meta_info_path = cur_dir + File.separator + "map_service";

				// PropertyMng.getInstance(path.getAbsolutePath());
			} else {
				FileInputStream fiss = new FileInputStream(
						meta_info_path + File.separator + "service_config.properties");
				enginProperty.load(fiss);

				// PropertyMng.getInstance(meta_info_path+File.separator +
				// "service_config.properties");
			}
			
			String isAuthority = enginProperty.getProperty("isAuthority", "");
			
			if(isAuthority == null || isAuthority.equals("false")) {
				AuthorityMng.isAuthority = false;
				return;
			}
			else {
				AuthorityMng.isAuthority = true;
			}
			
			local = enginProperty.getProperty("local", "");
			MapLog.getSCLog().debug("local=" + local);
			
			uri = enginProperty.getProperty("uri", "");
			MapLog.getSCLog().debug("uri=" + uri);
			String serverS = enginProperty.getProperty("servers", "");
			MapLog.getSCLog().debug("servers=" + serverS);
			
			if(serverS != null) {
				String[] serverList = serverS.split("\\|");
				for(String server : serverList) {
					
					if(!server.equals(local)) {
						conServers.add(server);	
					}
				}
			}
			
			dbUrl = enginProperty.getProperty("dbUrl", "");
			dbUser = enginProperty.getProperty("dbUser", "");
			dbPassword = enginProperty.getProperty("dbPassword", "");
			serverGroup = enginProperty.getProperty("serverGroup", "");
			MapLog.getSCLog().debug("dbUrl=" + dbUrl);
			MapLog.getSCLog().debug("dbUser=" + dbUser);
			MapLog.getSCLog().debug("dbPassword=" + dbPassword);
			MapLog.getSCLog().debug("serverGroup=" + serverGroup);
			
			
			dbAuth = new DBAuth(dbUrl, dbUser, dbPassword, serverGroup);
			
			liveCheck();
			
			// 기존에 구동 중인 서버로 부터 인증 정보를 받는다.
			Users serverUsers = this.getServerAuth();
			
			// 기존에 구동 중인 서버로 부터 인증 정보를 못받을 경우 DB와 로컬 파일을 읽는다.
			if(serverUsers == null) {
				
				Users dbUsers = dbAuth.getDBAuth();
				MapLog.getSCLog().debug("데이타 베이스로 부터 받은 권한");
				MapLog.getSCLog().debug(gson.toJson(users));
				
				
				Users localUsers = this.readUsersFile();
				MapLog.getSCLog().debug("로컬 파일로 부터 받은 권한");
				MapLog.getSCLog().debug(gson.toJson(localUsers));
				
				
				if(dbUsers != null) {
					MapLog.getSCLog().debug("데이타 베이스로 부터 권한 정보 저장");
					this.users = dbUsers;
					if(localUsers != null && localUsers.getUser().size() > 0) {
						updateUsers(this.users, localUsers);
					}
				}
				else if(localUsers != null ){
					MapLog.getSCLog().debug("로컬 파일로 부터 권한 정보 저장");
					this.users = localUsers;
				}
			}
			else {
				this.users = serverUsers;
				MapLog.getSCLog().debug("서버로 부터 전달 받은 권한 정보 저장");
			}
			
			this.writeUsersFile();
			
			// 메일 0시에 요청 개수 초기화 설정
			this.regInitAuth();
			
			MapLog.getSCLog().debug("인증 정보 설정 완료");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public int sendHttp(String urlS, byte[] input) {
		
		int responseCode = -1;
        try {
            // URL 객체 생성
            URL urlObj = new URL(urlS);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

            // 요청 설정
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                //byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 확인
            responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // 응답 읽기
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Response Body: " + response.toString());
                }
            } else {
                System.out.println("Error: Response Code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCode;
	}
	
	
	public static void deployUsers() {
		
		String json = gson.toJson(users);
		
		for(String server : conServers) {
			if(liveServers.get(server)) {
				
				String urlS = "http://"+server+uri+"?req=update";
				
		        try {
		            // URL 객체 생성
		            URL urlObj = new URL(urlS);
		            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

		            // 요청 설정
		            connection.setRequestMethod("POST");
		            connection.setRequestProperty("Content-Type", "application/json; utf-8");
		            connection.setRequestProperty("Accept", "application/json");
		            connection.setDoOutput(true);

		            // 요청 본문 전송
		            try (OutputStream os = connection.getOutputStream()) {
		                byte[] input = json.getBytes("utf-8");
		                os.write(input, 0, input.length);
		            }

		            // 응답 확인
		            int responseCode = connection.getResponseCode();
		            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
		                // 응답 읽기
		                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
		                    StringBuilder response = new StringBuilder();
		                    String responseLine;
		                    while ((responseLine = br.readLine()) != null) {
		                        response.append(responseLine.trim());
		                    }
		                    MapLog.getSCLog().debug("인증 배포 전달  : responseCode="+ responseCode );
		                }
		            } else {
		            	MapLog.getSCLog().debug("인증 배포 전달 오류 : "+ urlS +", responseCode = " + responseCode );
		            }
		        } catch (Exception e) {
		        	MapLog.getSCLog().debug("인증 배포 전달 오류 : "+urlS, e);
		        }
				
			}
		}
		
	}
	
	
	public static boolean updateDBAuth() {
		Users dbUsers = dbAuth.getDBAuth();
		if(dbUsers == null || dbUsers.getUser().size() == 0 ) {
			return false;
		}
		
		updateUsers(dbUsers, users);
		
		users = dbUsers;
		
		writeUsersFile();
		
		return true;
	}
	
	
	/**
	 * users2의 Function의 cnt 정보를 users1에 업데이트한다.
	 * @param users1
	 * @param users2
	 */
	public static void updateUsers(Users users1, Users users2) {
		for(User user1 : users1.getUser()) {
			
			User user2 = null;
			
			for(User user22 : users2.getUser()) {
				if(user1.getKey().equals(user22.getKey())) {
					user2 = user22;
				}
			}
			
			if(user2 == null) {
				continue;
			}
			
			for(Function ft1 : user1.getFunction()) {
				
				Function ft2 = null;
				
				for(Function ft22 : user2.getFunction()) {
					
					if(ft1.getName().equals(ft22.getName())) {
						
						ft2 = ft22;
					}
				}
				
				if(ft2 != null) {
					
					ft1.setCnt(ft2.getCnt());
				}
				
			}
		}
	}
	
	
	public void liveCheck() {
		
		for(String server: conServers) {
			
			String urlS = "http://"+server+uri+"?req=ack";

	            try {
	                URL url = new URL(urlS);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setConnectTimeout(3000); // 타임아웃 설정 (5초)
	                connection.setReadTimeout(3000);

	                int statusCode = connection.getResponseCode();
	                if (statusCode == 200) {
	                	liveServers.put(server, true);
	                	//MapLog.getSCLog().debug(server+",liveCheck OK");
	                } else {
	                	liveServers.put(server, false);
	                	MapLog.getSCLog().debug(urlS+",liveCheck Fail, statusCode="+statusCode);
	                }
	            } catch (Exception e) {
	            	liveServers.put(server, false);
	            	MapLog.getSCLog().debug(urlS+",liveCheck "+e.toString());
	            }
	            
		}        
		
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
		for(String server: conServers) {
			
			String urlS = "http://"+server+uri+"?req=ack";
			
	        scheduler.scheduleAtFixedRate(() -> {
	            try {
	                URL url = new URL(urlS);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setConnectTimeout(3000); // 타임아웃 설정 (5초)
	                connection.setReadTimeout(3000);

	                int statusCode = connection.getResponseCode();
	                if (statusCode == 200) {
	                	liveServers.put(server, true);
	                	//MapLog.getSCLog().debug(urlS+",liveCheck OK");
	                } else {
	                	liveServers.put(server, false);
	                	MapLog.getSCLog().debug(urlS+",liveCheck Fail, statusCode="+statusCode);
	                }
	            } catch (Exception e) {
	            	liveServers.put(server, false);
	            	MapLog.getSCLog().debug(urlS+",liveCheck "+e.toString());
	            }
	            
	        }, 20, 20, TimeUnit.SECONDS); // 처음 실행 지연 시간 0초, 주기 20초		
		}        
        
        // 10초 간격으로 작업 실행
	}
	
	
	/**
	 * 메일 0시에 요청 개수 초기화
	 */
	public void regInitAuth() {
        Timer timer = new Timer();

        // 매일 자정 시간 계산
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 현재 시간이 자정을 지난 경우, 다음날 자정으로 설정
        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Date firstTime = calendar.getTime();

        // 자정에 실행할 작업
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	AuthorityMng.initCallCnt();
            	MapLog.getSCLog().debug("AuthorityMng.initCallCnt()");
            }
        };

        // 24시간(86400000 밀리초) 간격으로 작업 스케줄링
        timer.scheduleAtFixedRate(task, firstTime, 24 * 60 * 60 * 1000);
	}
	
	
	public Users readUsersFile() {
		String cur_dir = System.getProperty("user.dir");
		String fileS = cur_dir + File.separator + "map_service" + File.separator + "authority.json";
		File path = new File(fileS);
		Users us = null;
		try {
			String value = FileUtils.readFileToString(path, "utf-8");
			if(value != null) {
				us = this.gson.fromJson(value, Users.class);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return us;
	}
	
	public static void writeUsersFile() {
		String cur_dir = System.getProperty("user.dir");
		
		String fileS = cur_dir + File.separator + "map_service" + File.separator + "authority.json";
		
		String json = gson.toJson(users);

		try {
			FileUtils.write(new File(fileS), json, "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void writeUsersFile(String path) {
//		String cur_dir = System.getProperty("user.dir");
//		
//		String fileS = cur_dir + File.separator + "map_service" + File.separator + "authority.json";
		
		String json = gson.toJson(users);

		try {
			FileUtils.write(new File(path), json, "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public Users getServerAuth() {
		
		//String localIp = TileServiceMng.getLocalServerIp();
		
		Users getUser = null;
		
		boolean ok = false;

		for(String server: conServers) {
			
			String urlS = "http://"+server+uri+"?req=users";
			try {
				
				if(server.equals(local)) {
					continue;
				}
				
				if(this.liveServers.get(server) == false) {
					continue;
				}
				
				//URL url = new URL("http://"+server+"/map/Authority?req=users");
				
				URL url = new URL(urlS);
				
				String value = IOUtils.toString(url, "utf-8");
				if(value != null) {
				
					JSONObject protocolj = new JSONObject(value);
					
					JSONObject body = (JSONObject) protocolj.get("body");
					
					getUser = this.gson.fromJson(body.toString(), Users.class);
					
					MapLog.getSCLog().debug("전달 받은 인증 정보 = " + urlS+"," + value);
					
					ok = true;
					break;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MapLog.getSCLog().debug(urlS+", 서버 인증 업데이트 연결 오류"+","+e.toString());
				//System.out.println("not connect server : " + server);
				
			}
		}
		
		//return ok;
		
		return getUser;
	}
	
	public User getUser1() {
		
		User user1 = new User();

		List<String> domains = user1.getDomain();
		
		domains.add("localhost");
		domains.add("192.168.100.1");
		
		user1.setKey("0101");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setCnt(0);
		f1.setName(Constant.TILE);
		func1.add(f1);
		
		
		Function f4 = new Function();
		f4.setDayCnt(2);
		f4.setCnt(0);
		f4.setName(Constant.ANALYSIS);
		func1.add(f4);


		return user1;
	}
	
	public User getUser2() {
		User user1 = new User();
		
		user1.setKey("0202");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setDayCnt(1000);
		f1.setCnt(0);
		f1.setName(Constant.TILE);
		func1.add(f1);
		
		Function f4 = new Function();
		f4.setDayCnt(2);
		f4.setCnt(0);
		f4.setName(Constant.ANALYSIS);
		func1.add(f4);
		
		return user1;
	}
	
	public User getUser3() {
		User user1 = new User();
		
		user1.setKey("0303");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setDayCnt(0);
		f1.setCnt(0);
		f1.setName(Constant.TILE);
		func1.add(f1);
		
		return user1;
	}
	
	public User getUser4() {
		User user1 = new User();
		
		user1.setKey("abcd1234efgh5678ijkl91011mnop1213qrst1415uvwx");
		user1.setUpdateTime(System.currentTimeMillis());
		List<String> domains = user1.getDomain();
		
		domains.add("localhost");
		domains.add("192.168.100.1");
		
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setDayCnt(Integer.MAX_VALUE);
		f1.setCnt(0);
		f1.setName(Constant.TILE);
		func1.add(f1);
		
		Function f4 = new Function();
		f4.setDayCnt(Integer.MAX_VALUE);
		f4.setCnt(0);
		f4.setName(Constant.ANALYSIS);
		func1.add(f4);

		
		return user1;
	}	
	
	/*
	public User getUser1() {
		
		User user1 = new User();
		
		user1.setKey("0101");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setCnt(0);
		f1.setName("tile");
		func1.add(f1);
		
		Content c1 = new Content();
		c1.setDayCnt(1000);
		c1.setCnt(0);
		c1.setName("emap|emp01");
		f1.getContent().add(c1);
		
		Content c2 = new Content();
		c2.setDayCnt(1000);
		c2.setCnt(0);
		c2.setName("emap|emp02");
		f1.getContent().add(c2);

		
		Function f2 = new Function();
		f2.setDayCnt(2);
		f2.setCnt(0);
		f2.setName("search");
		func1.add(f2);
		
		
		Content c3 = new Content();
		c3.setDayCnt(2);
		c3.setCnt(0);
		c3.setName("emap|fa");
		f2.getContent().add(c3);
		
		Content c4 = new Content();
		c4.setDayCnt(2);
		c4.setCnt(0);
		c4.setName("emap|fb");
		f2.getContent().add(c4);
		
		
		Function f3 = new Function();
		f3.setDayCnt(2);
		f3.setCnt(0);
		f3.setName("code");
		func1.add(f3);
		
		Function f4 = new Function();
		f4.setDayCnt(2);
		f4.setCnt(0);
		f4.setName("analysis");
		func1.add(f4);
		
		Function f5 = new Function();
		f5.setDayCnt(2);
		f5.setCnt(0);
		f5.setName("pnu_analysis");
		func1.add(f5);

		
		Function f6 = new Function();
		f6.setDayCnt(1000);
		f6.setCnt(0);
		f6.setName("tiles");
		func1.add(f6);


		return user1;
	}
	
	public User getUser2() {
		User user1 = new User();
		
		user1.setKey("0202");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setDayCnt(1000);
		f1.setCnt(0);
		f1.setName("tile");
		func1.add(f1);
		
		Function f2 = new Function();
		f2.setDayCnt(2);
		f2.setCnt(0);
		f2.setName("search");
		func1.add(f2);
		
		Function f3 = new Function();
		f3.setDayCnt(2);
		f3.setCnt(0);
		f3.setName("code");
		func1.add(f3);
		
		Function f4 = new Function();
		f4.setDayCnt(2);
		f4.setCnt(0);
		f4.setName("analysis");
		func1.add(f4);
		
		Function f5 = new Function();
		f5.setDayCnt(2);
		f5.setCnt(0);
		f5.setName("pnu_analysis");
		func1.add(f5);
		
		Function f6 = new Function();
		f6.setDayCnt(1000);
		f6.setCnt(0);
		f6.setName("tiles");
		func1.add(f6);


		return user1;
	}
	
	public User getUser3() {
		User user1 = new User();
		
		user1.setKey("0303");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Function f1 = new Function();
		f1.setDayCnt(0);
		f1.setCnt(0);
		f1.setName("tile");
		func1.add(f1);
		
		Content c1 = new Content();
		c1.setDayCnt(1000);
		c1.setCnt(0);
		c1.setName("emap");
		f1.getContent().add(c1);		
		
		Function f2 = new Function();
		f2.setDayCnt(2);
		f2.setCnt(0);
		f2.setName("search");
		func1.add(f2);
		
		Function f6 = new Function();
		f6.setDayCnt(1000);
		f6.setCnt(0);
		f6.setName("tiles");
		func1.add(f6);
		
		
		return user1;
	}
	
	public User getUser4() {
		User user1 = new User();
		
		user1.setKey("abcd1234efgh5678ijkl91011mnop1213qrst1415uvwx");
		user1.setUpdateTime(System.currentTimeMillis());
		List<Function> func1 =  user1.getFunction();
		
		Vector<String> apiNames = Constant.getOpenApiNames();
		
		for(String apiName : apiNames) {
			Function f1 = new Function();
			f1.setDayCnt(Integer.MAX_VALUE);
			f1.setCnt(0);
			f1.setName(apiName);
			func1.add(f1);
		}
		return user1;
	}
	*/
	
	public static void main(String[] args) {
		AuthorityMng am = new AuthorityMng();
		long now = System.currentTimeMillis();
		am.users.setUpdateTime(System.currentTimeMillis());
		List<User> userList = am.users.getUser();
		
		userList.add(am.getUser1());
		userList.add(am.getUser2());
		userList.add(am.getUser3());
		userList.add(am.getUser4());
		
		//json = gson.toJson(tilesReq);
		String json = am.gson.toJson(am.users);

		try {
			FileUtils.write(new File("D:\\workspace\\MapService\\map_service\\authority.json"), json, "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

