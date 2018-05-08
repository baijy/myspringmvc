package com.jianyu.mvc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jianyu.mvc.annotation.Autowired;
import com.jianyu.mvc.annotation.Controller;
import com.jianyu.mvc.annotation.Service;

/**
 * 入口Servlet
 * @author BaiJianyu
 *
 */
public class DispatcherServlet extends HttpServlet {
	private Map<String,Object> instanceMapping = new HashMap<String,Object>(); // 方法名和类方法的映射
	
	private List<String> classNames = new ArrayList<>(); // 带有特定注解的类
	
	@Override
	public void init(ServletConfig config){
		// System.out.println("我是初始化方法");
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		doPost(req,resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("请求到doPost方法了...");
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
	
	/**
	 * 扫描
	 * @param pkgName
	 */
	private void scanPackage(String pkgName){
		URL url = getClass().getClassLoader().getResource("/"+ pkgName.replace("\\.", "/"));
		
		File dir = new File(url.getFile());
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				scanPackage(pkgName + "."+ file.getName());
			}else{
				if( !file.getName().endsWith(".class") ){
					continue;
				}
			}
			
			String className = pkgName + "." + file.getName().replace(".class", "");
			
			try {
				Class<?> clazz = Class.forName(className);
				if(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)){
					classNames.add(className);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	// 自动注入方法
	private void doAutowired(){
		if(instanceMapping.isEmpty()){
			return;
		}
		
		for(Map.Entry<String, Object> entry : instanceMapping.entrySet()){
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field : fields ){
				if(!field.isAnnotationPresent(Autowired.class)){
					continue;
				}
				String beanName;
				Autowired autowired = field.getAnnotation(Autowired.class);  
				if("".equals(autowired.value())){
					
					
				}
				
				
			}
		}
			
			
			
	}
}
