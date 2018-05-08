package com.jianyu.mvc;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 入口Servlet
 * @author BaiJianyu
 *
 */
public class DispatcherServlet extends HttpServlet {
	
	@Override
	public void init(ServletConfig config){
		System.out.println("我是初始化方法");
	}
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		doPost(req,resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		out(resp,"请求到doPost方法了");
	}
	
	private void out(HttpServletResponse response,String str){
		try {
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().print(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
