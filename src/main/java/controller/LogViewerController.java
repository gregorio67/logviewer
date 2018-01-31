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

import cmn.util.common.util.NullUtil;
import cmn.util.common.util.PropertyUtil;
import cmn.util.spring.SpringBeanSupport;


@Controller
public class LogViewerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerController.class);
	
	
	@RequestMapping(value = "/log/viewer.do")
	public @ResponseBody Map<String, Object> logViewer(String logType) throws Exception {
		
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
		int logLength =  PropertyUtil.getInt("log.read.line");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Read log file :: {}", logFile);
		}

		/** Check log file exists **/
		File file = new File(logFile);
		if (!file.exists()) {
			resultMap.put("log", "Log file not found :: " + logFile);
			return resultMap;
		}

		BufferedReader in = null;
		StringBuilder logContent = new StringBuilder();
		int idx = 0;
		
		try {
			in = new BufferedReader (new InputStreamReader (new ReverseReader(file)));
			while(true) {
				String contents = in.readLine();
				logContent.append(contents).append("\n");
				if (idx++ >= logLength) {
					break;
				}
			}
		}
		catch(Exception logex) {
			LOGGER.error("Log file read error :: {}", logex.getMessage());
		}
		finally {
			in.close();
		}
		
		if (NullUtil.isNull(logContent) ) {
			resultMap.put("log", "No Contents");			
		}
		else {
			resultMap.put("log", logContent.toString());			
		}
		return resultMap;
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
}
