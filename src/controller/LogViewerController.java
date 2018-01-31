import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cmn.util.spring.PropertyUtil;


@Controller
public class LogViewerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerController.class);
	
	
	@RequestMapping(value = "/log/viewer.do")
	public @ResponseBody Map<String, Object> logViewer() throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String logFile = PropertyUtil.getString("app.log.location");
		int logLength =  PropertyUtil.getInt("app.log.length");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("LOG FILE :: {}", logFile);
		}
		
		BufferedReader in = null;
		StringBuilder logContent = new StringBuilder();
		int idx = 0;

		try {
			in = new BufferedReader (new InputStreamReader (new ReverseReader(new File(logFile))));
			while(true) {
				String contents = in.readLine();
				logContent.append(contents).append("\n");
				if (idx++ >= logLength) {
					break;
				}
			}
		}
		catch(Exception logex) {
			
		}
		finally {
			in.close();
		}
		
		resultMap.put("log", logContent.toString());
		return resultMap;
	}
}
