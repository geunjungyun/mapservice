package com.gis2.storage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.mapplan.Function;
import com.mapplan.User;
import com.mapplan.Users;

public class DBAuth {
	
    String url = "jdbc:postgresql://119.196.18.2:5555/test"; // 데이터베이스 URL
    String user = "test"; // 사용자 이름
    String password = "test1234"; // 비밀번호
    String serverGroup = "";
    
    public DBAuth(String url, String user, String password, String serverGroup) {
    	this.url = url;
    	this.user = user;
    	this.password = password;
    	this.serverGroup = serverGroup;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBAuth db = new DBAuth("jdbc:postgresql://119.196.18.2:5555/test", "test", "test1234","0");
		db.getDBAuth();
	}
	
	public Users getDBAuth() {
        String queryTable1 = "SELECT api_key,start_dttm,end_dttm FROM meb_api_key where server_id like '"+serverGroup+"' and use_yn like 'Y'"; // 키 등록
        String queryTable2 = "SELECT * FROM meb_api_ipdns where use_yn like 'Y'"; // 도메인 등록 테이블
        //String queryTable3 = "SELECT api_key, cd_id, day_limit FROM meb_api_usr_svc where (use_yn like 'Y') and (cd_id like 'tile' or cd_id like 'analysis' )"; // 컨텐츠 회수 제한
        String queryTable3 = "SELECT api_key, cd_id, day_limit FROM meb_api_usr_svc where (use_yn like 'Y')"; // 컨텐츠 회수 제한
        
        Users users = new Users();
        
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("PostgreSQL 데이터베이스에 연결 성공!");
            
            // 첫 번째 테이블 읽기
            try (Statement statement1 = connection.createStatement();
                 ResultSet resultSet1 = statement1.executeQuery(queryTable1)) {

                System.out.println("\n[meb_api_key 데이터]");
                while (resultSet1.next()) {
                    //인증키
                    String api_key = resultSet1.getString("api_key").trim();
                    //요효기간
                    Timestamp start = resultSet1.getTimestamp("start_dttm");
                    Timestamp end = resultSet1.getTimestamp("end_dttm");
                    System.out.println(api_key+"|"+start+"|"+end);
                    
                    User user = new User();
                    user.setKey(api_key);
                    if(start != null) {
                    	user.setStartTime(start.toString());
                    }
                    if(end != null) {
                    	user.setEndTime(end.toString());
                    }
                    users.getUser().add(user);
                }
            }

            // 두 번째 테이블 읽기
            try (Statement statement2 = connection.createStatement();
                 ResultSet resultSet2 = statement2.executeQuery(queryTable2)) {

                System.out.println("\n[meb_api_ipdns 데이터]");
                while (resultSet2.next()) {
                    String api_key = resultSet2.getString("api_key").trim();
                    String ip_dns = resultSet2.getString("ip_dns").trim();
                    System.out.println(api_key+"|"+ip_dns);
                    
                    List<User> userList = users.getUser();
                    for(User user : userList) {
                    	if(user.getKey().equals(api_key)) {
                    		user.getDomain().add(ip_dns);
                    	}
                    }
                    
                }
            }
            
            
            // 두 번째 테이블 읽기
            try (Statement statement3 = connection.createStatement();
                 ResultSet resultSet3 = statement3.executeQuery(queryTable3)) {

                System.out.println("\n[meb_api_usr_svc 데이터]");
                while (resultSet3.next()) {
                    String api_key = resultSet3.getString("api_key").trim();
                    String cd_id = resultSet3.getString("cd_id").trim();
                    int day_limit = resultSet3.getInt("day_limit");
                    System.out.println(api_key+"|"+cd_id+"|"+day_limit);
                    
                    List<User> userList = users.getUser();
                    for(User user : userList) {
                    	if(user.getKey().equals(api_key)) {

                    		if(cd_id.endsWith(".tile") || cd_id.endsWith(".layer")) {
                    			Function ft = new Function();
                    			ft.setName(cd_id);
                    			ft.setDayCnt(day_limit);
                    			user.getFunction().add(ft);
                    		}
                    		
                    		/*
                    		if(cd_id.equals("tile")) {
                    			
                    			Function ft = new Function();
                    			ft.setName("TILE");
                    			ft.setDayCnt(day_limit);
                    			user.getFunction().add(ft);
                    		}
                    		else if(cd_id.equals("analysis")) {
                    			Function ft = new Function();
                    			ft.setName("ANALYSIS");
                    			ft.setDayCnt(day_limit);
                    			user.getFunction().add(ft);
                    		}
                    		*/
                    	}
                    }
                    
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //System.out.println(users.toString());
        
        if(users.getUser() == null || users.getUser().size() == 0) {
        	users = null;
        }
        
        return users;
	}
	
	public void addInfo() {
//        String queryTable1 = "SELECT * FROM meb_api_key"; // 첫 번째 테이블 쿼리
//        String queryTable2 = "SELECT * FROM meb_api_ipdns"; // 두 번째 테이블 쿼리
//        String queryTable3 = "SELECT * FROM meb_api_sum"; // 두 번째 테이블 쿼리
        
        // SQL 쿼리
        String insertTable1 = "INSERT INTO meb_api_key (api_key_id, api_key, use_yn) VALUES (?, ?, ?)";
        String insertTable2 = "INSERT INTO meb_api_ipdns (api_key_id, api_key, ip_dns) VALUES (?, ?, ?)";
        String insertTable3 = "INSERT INTO meb_api_sum (api_key_id, day_cnt) VALUES (?, ?)";

        // 데이터베이스 연결
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("PostgreSQL 데이터베이스에 연결 성공!");

            // 첫 번째 테이블에 데이터 삽입
            try (PreparedStatement pstmt1 = connection.prepareStatement(insertTable1)) {
                pstmt1.setInt(1, 1); // 첫 번째 값: id
                pstmt1.setString(2, "Alice"); // 두 번째 값: name
                int rowsInserted1 = pstmt1.executeUpdate();
                System.out.println("Table 1에 추가된 행 수: " + rowsInserted1);
            }

            // 두 번째 테이블에 데이터 삽입
            try (PreparedStatement pstmt2 = connection.prepareStatement(insertTable2)) {
                pstmt2.setInt(1, 1); // 첫 번째 값: id
                pstmt2.setString(2, "Sample Description"); // 두 번째 값: description
                int rowsInserted2 = pstmt2.executeUpdate();
                System.out.println("Table 2에 추가된 행 수: " + rowsInserted2);
            }

            // 세 번째 테이블에 데이터 삽입
            try (PreparedStatement pstmt3 = connection.prepareStatement(insertTable3)) {
            	pstmt3.setInt(1, 1); // 첫 번째 값: id
            	pstmt3.setString(2, "Sample Description"); // 두 번째 값: description
                int rowsInserted3 = pstmt3.executeUpdate();
                System.out.println("Table 2에 추가된 행 수: " + rowsInserted3);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }        
	}
	

}
