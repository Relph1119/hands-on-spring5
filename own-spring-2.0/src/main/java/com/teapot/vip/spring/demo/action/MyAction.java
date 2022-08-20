package com.teapot.vip.spring.demo.action;

import com.teapot.vip.spring.demo.service.IModifyService;
import com.teapot.vip.spring.demo.service.IQueryService;
import com.teapot.vip.spring.framework.annotation.TPAutowired;
import com.teapot.vip.spring.framework.annotation.TPController;
import com.teapot.vip.spring.framework.annotation.TPRequestMapping;
import com.teapot.vip.spring.framework.annotation.TPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@TPController
@TPRequestMapping("/web")
public class MyAction {

	@TPAutowired IQueryService queryService;
	@TPAutowired IModifyService modifyService;

	@TPRequestMapping("/query.json")
	public TPModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@TPRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@TPRequestMapping("/add*.json")
	public TPModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @TPRequestParam("name") String name,@TPRequestParam("addr") String addr){
		String result = null;
		try {
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
//			e.printStackTrace();
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail",e.getCause().getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			return new TPModelAndView("500",model);
		}

	}
	
	@TPRequestMapping("/remove.json")
	public TPModelAndView remove(HttpServletRequest request,HttpServletResponse response,
		   @TPRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@TPRequestMapping("/edit.json")
	public TPModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@TPRequestParam("id") Integer id,
			@TPRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private TPModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
