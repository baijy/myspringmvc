package com.jianyu.mvc.service.impl;

import com.jianyu.mvc.annotation.Service;
import com.jianyu.mvc.service.ModifyService;

@Service
public class ModifyServiceImpl implements ModifyService{

	@Override
	public String add(String name, String addr) {
		return "invoke add name = " + name + " addr = " + addr;
	}

	@Override
	public String remove(Integer id) {
		return "remove id = " + id;
	}

}
