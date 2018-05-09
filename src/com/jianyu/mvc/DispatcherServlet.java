package com.jianyu.mvc;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jianyu.mvc.annotation.Autowired;
import com.jianyu.mvc.annotation.Controller;
import com.jianyu.mvc.annotation.RequestMapping;
import com.jianyu.mvc.annotation.RequestParam;
import com.jianyu.mvc.annotation.Service;
import com.jianyu.mvc.util.Play;

/**
 * 入口Servlet
 * 
 * @author BaiJianyu
 *
 */
public class DispatcherServlet extends HttpServlet {
	private Map<String, Object> instanceMapping = new HashMap<String, Object>(); // 方法名和类方法的映射

	private List<String> classNames = new ArrayList<>(); // 带有特定注解的类

	private Map<String, HandlerModel> handlerMapping = new HashMap<>(); // url到方法的映射

	@Override
	public void init(ServletConfig config) {
		System.out.println("DispatcherServlet初始化开始");
		scanPackage(config.getInitParameter("scanPackage"));
		doInstance();
		doAutowired();
		doHandlerMapping();

		for (Entry<String, HandlerModel> entry : handlerMapping.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().controller.getClass().getName());
			System.out.println(entry.getValue().method.getName());
			System.out.println(entry.getValue().paramMap.values());
			System.out.println("----");
		}

		System.out.println("DispatcherServlet初始化完毕");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		out(resp, "请求到doPost方法了... ");
		doInvoke(req, resp);
	}

	private void doInvoke(HttpServletRequest req, HttpServletResponse resp) {
		boolean matched=false;
		try {
			matched = pattern(req, resp);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		if (!matched) {
			out(resp, "404 not found");
		}

	}

	// url能否映射到
	private boolean pattern(HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (handlerMapping.isEmpty()) {
			return false;
		}
		// 用户请求地址
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		// 用户写了多个"///"，只保留一个
		requestUri = requestUri.replace(contextPath, "").replaceAll("/+", "/");

		// 遍历HandlerMapping，寻找url匹配的
		for (Map.Entry<String, HandlerModel> entry : handlerMapping.entrySet()) {
			if (entry.getKey().equals(requestUri)) {
				HandlerModel handlerModel = entry.getValue();

				Map<String, Integer> paramIndexMap = handlerModel.paramMap;
				// 存放参数值
				Object[] paramValues = new Object[paramIndexMap.size()];
				// 参数类型
				Class<?>[] types = handlerModel.method.getParameterTypes();

				for (Map.Entry<String, Integer> param : paramIndexMap.entrySet()) {
					String key = param.getKey();
					if (key.equals(HttpServletRequest.class.getName())) {
						paramValues[param.getValue()] = request;
					} else if (key.equals(HttpServletResponse.class.getName())) {
						paramValues[param.getValue()] = response;
					} else {
						String parameter = request.getParameter(key);
						if (parameter != null) {
							paramValues[param.getValue()] = convert(parameter.trim(), types[param.getValue()]);
						}
					}
				}
				
				// TODO 如果缺少参数，paramValues会出现null，调用时报参数错误
				handlerModel.method.invoke(handlerModel.controller,paramValues);
				return true;
			}

			
		}

		return false;
	}
	
	// 将用户传来的参数转换为方法需要的参数类型
	private Object convert(String parameter, Class<?> targetType) {
		if (targetType == String.class) {
			return parameter;
		} else if (targetType == Integer.class || targetType == int.class) {
			return Integer.valueOf(parameter);
		} else if (targetType == Long.class || targetType == long.class) {
			return Long.valueOf(parameter);
		} else if (targetType == Boolean.class || targetType == boolean.class) {
			if (parameter.toLowerCase().equals("true") || parameter.equals("1")) {
				return true;
			} else if (parameter.toLowerCase().equals("false") || parameter.equals("0")) {
				return false;
			}
			throw new RuntimeException("不支持的参数");
		} else {
			// TODO 还有很多其他的类型，char、double之类的依次类推，也可以做List<>, Array, Map之类的转化
			return null;
		}
	}

	private void out(HttpServletResponse response, String str) {
		try {
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().print(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 扫描包下面所有的类
	 * 
	 * @param pkgName
	 */
	private void scanPackage(String pkgName) {
		// 获取指定的包的实际路径url，将com.jianyu.mvc变成目录结构com/jianyu/mvc
		// String path = "/"+ pkgName.replace("\\.", "/"); // linux
		String path = "/" + pkgName.replace(".", "/"); // windows
		URL url = getClass().getClassLoader().getResource(path);
		// 转化成file对象
		File dir = new File(url.getFile());
		// 递归查询所有的class文件
		for (File file : dir.listFiles()) {
			String fileName = file.getName();
			// 如果是目录，就递归目录的下一层，如com.jianyu.mvc.controller
			if (file.isDirectory()) {
				scanPackage(pkgName + "." + fileName);
			} else {
				// 只有是class文件，并且是需要被spring托管的才做后续处理，否则跳过
				if (!fileName.endsWith(".class")) {
					continue;
				}
			}

			// 子目录居然进入了两次，直接跳过了是否目录，没弄明白，再加一层检查
			if (!fileName.endsWith(".class")) {
				continue;
			}

			// 举例，className = com.jianyu.mvc.controller.WebController
			String className = pkgName + "." + fileName.replace(".class", "");

			// 判断是否被 @Controller或者 @Service注解了，如果没注解，那么我们就不管它
			// 譬如annotation包和DispatcherServlet类我们就不处理
			try {
				Class<?> clazz = Class.forName(className);
				if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
					classNames.add(className);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 实例化
	private void doInstance() {
		if (classNames.size() == 0) {
			return;
		}

		// 遍历所有的被托管的类，并且实例化
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				// 如果是Controller
				if (clazz.isAnnotationPresent(Controller.class)) {
					// 举例：webController -> new WebController
					instanceMapping.put(lowerFirstChar(clazz.getSimpleName()), clazz.newInstance());
				} else if (clazz.isAnnotationPresent(Service.class)) {
					// 获取注解上的值，作为实例的key
					// 举例：QueryServiceImpl上的@Service("myQueryService")
					Service service = clazz.getAnnotation(Service.class);
					String value = service.value();
					if (!"".equals(value.trim())) {
						instanceMapping.put(value.trim(), clazz.newInstance());
					} else {
						// **个人思考：SIMS中是放在SpringService类中，启动时集中初始化的
						// 没值时就用接口的名字首字母小写作为key
						Class[] inters = clazz.getInterfaces();
						for (Class c : inters) {
							// 举例： ModifyService->new ModifyServiceImpl()
							String implName = lowerFirstChar(c.getSimpleName());
							instanceMapping.put(implName, clazz.newInstance());
							// 假设ServiceImpl只实现了一个接口
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 给被AutoWired注解的属性注入值
	private void doAutowired() {
		if (instanceMapping.isEmpty()) {
			return;
		}

		// 遍历所有被托管的对象
		for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
			// 查找所有被Autowired注解的属性（类）
			// getFields()获得某个类的所有的公共（public）的字段，包括父类;
			// getDeclaredFields()获得某个类的所有申明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
			// 在一个类里面注入其他的类，其他的类就是这个类的Field（属性成员）
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// 属性没加@Autowired的不需要注值
				if (!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}
				String beanName;
				// 获取AutoWired上面写的值，譬如@Autowired("abc")
				Autowired autowired = field.getAnnotation(Autowired.class);
				if ("".equals(autowired.value())) {
					// 例 searchService。注意，此处是获取属性的类名的首字母小写，与属性名无关，可以定义@Autowired
					// SearchService abc都可以。
					beanName = lowerFirstChar(field.getType().getSimpleName());
				} else {
					beanName = autowired.value();
				}

				field.setAccessible(true);
				if (instanceMapping.get(beanName) != null) {
					try {
						field.set(entry.getValue(), instanceMapping.get(beanName));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// 建立url到方法的映射
	private void doHandlerMapping() {
		if (instanceMapping.isEmpty()) {
			return;
		}

		for (Entry<String, Object> entry : instanceMapping.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			// 只有Controller有RequestMapping
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}

			// 取到Controller上的RequestMapping值，类的路径，例如 /web
			String url = "/";
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
				url += requestMapping.value();
			}

			// 获取方法上的RequestMapping，例如 /add
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}

				RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
				// 后面没有/的补一个
				String realUrl = url + "/" + methodMapping.value();

				// 替换掉多余的"/"，多于1个都都替换成1个
				realUrl = realUrl.replaceAll("/+", "/");

				// 获取@RequestParam的注解，一个参数可能有多个值
				Annotation[][] annotations = method.getParameterAnnotations();
				// 保存参数的位置
				Map<String, Integer> paramMap = new HashMap<String, Integer>();

				// 如Controller的add方法，将得到如下数组["name", "addr", "request",
				// "response"]
				String[] paramNames = Play.getMethodParameterNamesByAsm4(clazz, method);
				// 获取所有参数的类型，提取Request和Response的索引
				Class<?>[] paramTypes = method.getParameterTypes();

				for (int i = 0; i < annotations.length; i++) {
					Annotation[] anns = annotations[i];
					// 没有注解
					if (anns.length == 0) {
						// 如果没有注解，则是如String abc，Request request这种，没写注解的
						Class<?> type = paramTypes[i];
						if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
							// 保存参数的索引index
							paramMap.put(type.getName(), i);
						}else {  
                            //参数没写@RequestParam注解，只写了String name，那么通过java是无法获取到name这个属性名的  
                            //通过上面asm获取的paramNames来映射  
                            paramMap.put(paramNames[i], i);  
                        }  
						continue;
					}

					// 有注解
					for (Annotation ans : anns) {
						// 找到被RequestParam注解的参数，并取value值
						if (ans.annotationType() == RequestParam.class) {
							// 也就是@RequestParam("name")上的"name"
							String paramName = ((RequestParam) ans).value();
							// 如果@RequestParam("name")这里面
							if (!"".equals(paramName.trim())) {
								paramMap.put(paramName, i);
							}
						}
					}
				}

				HandlerModel model = new HandlerModel(method, entry.getValue(), paramMap);
				handlerMapping.put(realUrl, model);
			}
		}

	}

	private class HandlerModel {
		Method method;
		Object controller;
		Map<String, Integer> paramMap;

		public HandlerModel(Method method, Object controller, Map<String, Integer> paramMap) {
			this.method = method;
			this.controller = controller;
			this.paramMap = paramMap;
		}
	}

	// 首字母小写
	private String lowerFirstChar(String className) {
		char[] chars = className.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
}
