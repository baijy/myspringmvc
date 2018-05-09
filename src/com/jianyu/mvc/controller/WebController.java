package com.jianyu.mvc.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jianyu.mvc.annotation.Autowired;
import com.jianyu.mvc.annotation.Controller;
import com.jianyu.mvc.annotation.RequestMapping;
import com.jianyu.mvc.annotation.RequestParam;
import com.jianyu.mvc.service.ModifyService;
import com.jianyu.mvc.service.QueryService;

@Controller
@RequestMapping("/web")
public class WebController {
	@Autowired("myQueryService")
	private QueryService queryService;
	@Autowired
	private ModifyService modifyService;

	@RequestMapping("/search")
	public void search(@RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response) {
		String result = queryService.search(name);
		out(response, result);
	}

	/**
	 * 增加一条记录
	 * 
	 * @param name
	 * @param addr
	 * @param request
	 * @param response
	 */
	@RequestMapping("/add")
	public void add(@RequestParam("name") String name, @RequestParam("addr") String addr, HttpServletRequest request,
			HttpServletResponse response) {
		System.out.println();
		
		String result = modifyService.add(name, addr);
		out(response, result);
	}

	@RequestMapping("/update")
	public void update(String name, boolean flag, HttpServletRequest request, HttpServletResponse response) {
		out(response, "我是name：" + name + "flag为：" + flag);
	}

	/**
	 * 删除一条记录
	 * 
	 * @param id
	 * @param request
	 * @param response
	 */
	@RequestMapping("/remove")
	public void remove(@RequestParam("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
		String result = modifyService.remove(id);
		out(response, result);
	}

	/**
	 * 返回内容到前端的方法
	 * 
	 * @param response
	 * @param str
	 */
	private void out(HttpServletResponse response, String str) {
		try {
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().print(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
