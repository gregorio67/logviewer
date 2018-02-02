
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import ncd.spring.common.util.NullUtil;
import ncd.spring.common.util.PropertyUtil;
import ncd.spring.common.util.SpringBeanSupport;

@Controller
public class LogViewerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerController.class);
	
	/** ObjectMapper **/
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	@RequestMapping(value = "/log/viewer.do")
	public ModelAndView logViewer(String logType, String logSize, String callback) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		/** Log file location **/
//		String logType = params.get("logType") != null ? String.valueOf(params.get("logType")) : "";
		String logLocation = null;
		
		if (NullUtil.isNull(logType)) {
			logLocation = "log.app.location";
		}
		else {
			logLocation = "log." + logType + ".location";
		}
		
		String logFile = PropertyUtil.getString(logLocation);
		int logLength =  0;
		if (!NullUtil.isNull(logSize)) {
			logLength = Integer.parseInt(logSize);
		}
		else {
			logLength =  PropertyUtil.getInt("log.read.line");
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Read log file :: {}", logFile);
		}

		ModelAndView mav = new ModelAndView("ajaxMultiDataView");
		String result = null;
		/** Check log file exists **/
		File file = new File(logFile);
		if (!file.exists()) {
			resultMap.put("log", "Log file not found :: " + logFile);
			StringBuilder sb = new StringBuilder();
			result = sb.append(callback).append("(").append(map2Json(resultMap)).append(")").toString();
			return mav.addObject(result);
		}
		
		if (!file.canRead()) {
			resultMap.put("log", "Log file can't read :: " + logFile);
			StringBuilder sb = new StringBuilder();
			result = sb.append(callback).append("(").append(map2Json(resultMap)).append(")").toString();
			return mav.addObject(result);
			
		}

		BufferedReader in = null;
		StringBuilder logContent = new StringBuilder();
		int idx = 0;
		
		try {
			in = new BufferedReader (new InputStreamReader (new ReverseReader(file)));
			
			boolean isLoop = true;
			while(isLoop) {
				String contents = in.readLine();
				logContent.append(contents).append("\n");
				if (idx++ >= logLength) {
					isLoop = false;
				}
			}
		}
		catch(Exception logex) {
			logex.printStackTrace();
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
		
		if (logContent.length() <= 0) {
			resultMap.put("log", "No Contents");			
			StringBuilder sb = new StringBuilder();
			result = sb.append(callback).append("(").append(map2Json(resultMap)).append(")").toString();
			return mav.addObject(result);

		}
		else {
			resultMap.put("log", logContent.toString());			
			StringBuilder sb = new StringBuilder();
			result = sb.append(callback).append("(").append(map2Json(resultMap)).append(")").toString();

		}
		LOGGER.info("result :: {}", result);
		mav.addObject(result);
		return mav;
	}
	
	@RequestMapping(value = "/log/systemInfo.do")
	public @ResponseBody Map<String, Object> getSystemInfo(HttpServletRequest request) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Map<String, Object> map =  (Map<String, Object>) SpringBeanSupport.getBean(request, "systemInfo");
		
		Iterator<String> itr = map.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			String value = (String)map.get(key);
			
			resultMap.put(key, value);
		}
		
		return resultMap;
	}
	
	private <K,V> String map2Json(Map<K, V> dataMap) throws Exception {
		String jsonData = null;

		try {
			jsonData = objectMapper.writeValueAsString(dataMap);
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
			throw new Exception(e.getMessage(), e);
		}

		return jsonData;
	}

}


