package com.jianyu.mvc.service.impl;

import com.jianyu.mvc.annotation.Service;
import com.jianyu.mvc.service.IQueryService;

@Service("myQueryService")
public class QueryServiceImpl implements IQueryService {

	@Override
	public String search(String name) {
		return "invoke search name = " + name;
	}

}
