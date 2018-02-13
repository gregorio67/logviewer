package dymn.log.tailer;

import dymn.log.util.PropertiesUtil;

public class TailerMain 
{
	private static final int DELAT_MILLS = 250;
	
    public static void main( String[] args ) throws Exception{
    
//    	if (args[1] == null) {
//    		System.out.println("Usage : java -cp [classpath] dymn.log.tailer.TailerMain [filename]");
//    		System.exit(1);
//    	}
	    int delayMills = PropertiesUtil.getInt("og.read.delay.mills") != 0 ? PropertiesUtil.getInt("og.read.delay.mills") : DELAT_MILLS;
    	String logFileName = "d:/temp/portal_app.log";
    	
    	FileHandler fileHandler = new FileHandler(logFileName, delayMills, false, false);
    	fileHandler.start();
    }
}
