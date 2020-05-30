package com.websystique.springmvc.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akash.entity.Articles;
import com.akash.entity.Register;
import com.akash.entity.Responce;
import com.websystique.springmvc.domain.Message;

@RestController
public class HelloWorldRestController {
	@PostMapping("/register")
	Responce register(@RequestBody Register newRegister) {
		String message = "";
		Responce r = new Responce();
		System.out.println(newRegister);
		Connection con = getConnection();
		boolean flag = true;
		try {
			PreparedStatement stmt = con.prepareStatement("insert into user_details values(?,?,?,?,?)");
			stmt.setString(1, newRegister.getUsername());// 1 specifies the first parameter in the query
			stmt.setString(2, newRegister.getPassword());
			stmt.setString(3, newRegister.getEmail());
			stmt.setString(4, newRegister.getAddress());
			stmt.setString(5, getAccessToken());
			int i = stmt.executeUpdate();
			System.out.println(i + " records inserted");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			flag = false;
			r.setStatusCode("500");
			message = e.getMessage();
			e.printStackTrace();
		}

		if (flag) {
			r.setStatusCode("201");
			message = "new user created";
		}
		JSONObject body = new JSONObject();
		body.put("message", message);
		r.setBody(body.toString());
		return r;
	}

	@PostMapping("/login")
	Responce login(HttpServletRequest req, HttpServletResponse res, @RequestBody Register login) {
		ServletContext context = req.getServletContext();
		String message = "";
		String accessToken = "";
		Responce r = new Responce();
		System.out.println(login);
		Connection con = getConnection();
		JSONObject body = new JSONObject();
		boolean flag = true;
		if (!login.getUsername().equals(null) && !login.getUsername().equals("")) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(
						"select accessToken, password from user_details where username='" + login.getUsername() + "'");
				while (rs.next()) {
					if (rs.getString(2).equals(login.getPassword())) {
						accessToken = rs.getString(1);
						body.put("accessToken", accessToken);
						if (context.getAttribute("accesstoken") == null) {
							Set<String> s = new HashSet<>();
							s.add(accessToken);
							context.setAttribute("accesstoken", s);
						} else {
							Set<String> s = (Set<String>) context.getAttribute("accesstoken");
							s.add(accessToken);
							context.setAttribute("accesstoken", s);
						}
					}
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				flag = false;
				r.setStatusCode("500");
				message = e.getMessage();
				e.printStackTrace();
			}
		}

		if (flag) {
			r.setStatusCode("200");
			message = "success";
		}
		body.put("message", message);
		r.setBody(body.toString());
		return r;
	}

	@PostMapping("/articles")
	Responce articles(HttpServletRequest req, HttpServletResponse res, @RequestBody Articles articles) {
		ServletContext context = req.getServletContext();
		Set s = (Set<String>) context.getAttribute("accesstoken");
		String message = "";
		Responce r = new Responce();
		System.out.println(articles);
		Connection con = getConnection();
		boolean flag = true;
		PreparedStatement stmt = null;
		try {
			if (s.contains(articles.getAccess_token())) {
				stmt = con.prepareStatement("insert into article values(?,?,?)");
				stmt.setString(1, articles.getTitle());// 1 specifies the first parameter in the query
				stmt.setString(2, articles.getBody());
				stmt.setString(3, articles.getAuthor());
				int i = stmt.executeUpdate();
				System.out.println(i + " records inserted");
			} else {
				flag = false;
				r.setStatusCode("500");
				message = "access token not present";
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			flag = false;
			r.setStatusCode("500");
			message = e.getMessage();
			e.printStackTrace();
		}

		if (flag) {
			r.setStatusCode("201");
			message = "new article created";
		}
		JSONObject body = new JSONObject();
		body.put("message", message);
		r.setBody(body.toString());
		return r;
	}

	@GetMapping("/articles")
	Responce getarticles() {
		String message = "";
		Responce r = new Responce();
		Connection con = getConnection();
		boolean flag = true;
		List<JSONObject> list = new ArrayList<>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from article");
			while (rs.next()) {
				JSONObject temp = new JSONObject();
				temp.put("title", rs.getString(1));
				temp.put("body", rs.getString(2));
				temp.put("author", rs.getString(3));
				list.add(temp);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			flag = false;
			r.setStatusCode("500");
			message = e.getMessage();
			e.printStackTrace();
		}

		if (flag) {
			r.setStatusCode("200");
			message = list.toString();
		}
		JSONObject body = new JSONObject();
		body.put("data", message);
		r.setBody(body.toString());
		return r;
	}

	private static String getAccessToken() {
		Calendar cal = Calendar.getInstance();
		// TODO Auto-generated method stub
		String token = UUID.randomUUID().toString() + cal.getTimeInMillis();
		System.out.println(token);
		return token;
	}

	private static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/web_customer_tracker", "springstudent",
					"springstudent");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
}
