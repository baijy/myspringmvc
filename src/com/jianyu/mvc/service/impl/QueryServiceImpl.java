package com.jianyu.mvc.service.impl;

import com.jianyu.mvc.annotation.Service;
import com.jianyu.mvc.service.QueryService;

@Service("myQueryService")
public class QueryServiceImpl implements QueryService {

	@Override
	public String search(String name) {
		return "invoke search name = " + name;
	}

}
