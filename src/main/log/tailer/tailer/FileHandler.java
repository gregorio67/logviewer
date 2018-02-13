package dymn.log.tailer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import dymn.log.mybatis.BaseDao;
import dymn.log.mybatis.MybatisSqlSession;
import dymn.log.util.PropertiesUtil;

public class FileHandler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
	
	@Inject
	private BaseDao baseDao;
	
	private static final Splitter IF_SPEC_SPLITTER = Splitter.on( '|' );

	private final String filePath;

	private final long delayMs;

	private final boolean readWhole;

	private final boolean reopen;

	private final Map<File, Tailer> tailers = new HashMap<File, Tailer>();

//	private static final String logEntryPattern = "(^\\d+[(\\.\\|\\:\\d+)]*)(\\s+)((\\[\\w+[(\\-\\w+)]*)\\])(\\s+)(\\w+)(\\s+)(\\w+[(\\.|\\(|\\)\\w+)]*)";
//	private static final String logEntryPattern = "(^\\d+[(\\.\\|\\:\\d+)]*)(\\s+)((\\[\\w+[(\\-\\|\\/|.\\w+)]*)\\])(\\s+)(\\w+)(\\s+)(\\w+[(\\.|\\(|\\)\\w+)]*)";
//
//	private static final Pattern pattern = Pattern.compile(logEntryPattern);

	private FileAlterationMonitor monitor;

	private final ArrayBlockingQueue<String> readQueue = new ArrayBlockingQueue<String>(100);

	private boolean running = false;

	public FileHandler(final String filePath, final long delayMs, final boolean readWhole, final boolean reopen) {
		this.filePath = filePath;
		this.delayMs = delayMs;
		this.readWhole = readWhole;
		this.reopen = reopen;
	}

	private void setUpMonitor() {
		FileAlterationListenerAdaptor fileAlterationListener = new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(final File file) {
				LOGGER.info("File created: {}", file.getAbsolutePath());

				startTailer(file, true);
			}

			@Override
			public void onFileChange(final File file) {
				startTailer(file, false);
			}

			@Override
			public void onFileDelete(final File file) {
				LOGGER.info("File deleted: {}", file.getAbsolutePath());

				stopTailer(file);
			}
		};

		String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
		String directory = filePath.substring(0, filePath.lastIndexOf('/'));

		FileAlterationObserver observer = new FileAlterationObserver(new File(directory),
				new WildcardFileFilter(filename));
		observer.addListener(fileAlterationListener);

		monitor = new FileAlterationMonitor(delayMs);
		monitor.addObserver(observer);

		try {
			monitor.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void start() {
		if (running) {
			return;
		}

		LOGGER.info("File handler starting with pattern {}", filePath);

		running = true;

		setUpMonitor();

		String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
		File directory = new File(filePath.substring(0, filePath.lastIndexOf('/')));

		Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter(filename),
				FalseFileFilter.INSTANCE);

		for (File file : files) {
			startTailer(file, false);
		}

		Thread thread = new Thread(this);
		thread.setName(getClass().getName());
		thread.start();

		LOGGER.info("File handler started");
	}

	public void startTailer(final File file, final boolean newFile) {
		synchronized (tailers) {
			if (tailers.containsKey(file)) {
				return;
			}

			LOGGER.info("Starting tailer: {}", file.getAbsolutePath());

			Tailer tailer = null;
			tailer = new Tailer(file, new TailerListener(file), delayMs, !newFile && !readWhole, reopen);
			tailers.put(file, tailer);

			Thread thread = new Thread(tailer);
			thread.setName("Tailer-" + file.getName());
			thread.start();
		}
	}

	private void stopTailer(final File file) {
		synchronized (tailers) {
			try {
				tailers.get(file).stop();
				tailers.remove(file);

				LOGGER.info("Tailer stopped: {}", file.getAbsolutePath());
			} catch (NullPointerException e) {
				// ignore
			}
		}
	}

	public synchronized void stop() {
		if (!running) {
			return;
		}

		LOGGER.info("File handler stopping");

		try {
			monitor.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		synchronized (tailers) {
			for (File file : tailers.keySet()) {
				tailers.get(file).stop();
				LOGGER.info("Tailer stopped: {}", file.getAbsolutePath());
			}
			tailers.clear();
		}

		running = false;

		LOGGER.info("File handler stopped");
	}

	public void run() {
		String logEntryPattern = null;
		Pattern logPattern = null;
		List<String> patternIdx = null;
		try {
			logEntryPattern = PropertiesUtil.getString("log.enrty.pattern");
			logPattern = Pattern.compile(logEntryPattern);
			patternIdx = IF_SPEC_SPLITTER.splitToList(PropertiesUtil.getString("log.entry.index"));
		}
		catch(Exception ex) {
			throw new RuntimeException("Pattern compile error, Check your pattern : " + logEntryPattern);
		}
		StringBuilder sb = new StringBuilder();
	    Map<String, Object> map = new HashMap<String, Object>();
		boolean isSaved = false;
		while (running) {
			try {
				String line = readQueue.poll(100, TimeUnit.MILLISECONDS);
				if (line != null) {
					try {
						LOGGER.debug(line);
					    Matcher matcher = logPattern.matcher(line);
					    if (matcher.find()) {
							if (isSaved) {
								map.put("detailLog", sb.toString());
								insertLog(map);
								
								/** Current Read contents parsing **/
								int len = sb.length();
								sb.delete(0, len);
								map.clear();
								map.put("logTime", matcher.group(Integer.parseInt(patternIdx.get(0))));
								map.put("wasThread", matcher.group(Integer.parseInt(patternIdx.get(1))));
								map.put("logLevel", matcher.group(Integer.parseInt(patternIdx.get(2))));
								map.put("callMethod", matcher.group(Integer.parseInt(patternIdx.get(3))));						
								sb.append(line);
								if (LOGGER.isDebugEnabled()) {
									for (int i = 0; i <= matcher.groupCount(); i++) {
										LOGGER.debug(matcher.group(i));
										
									}
								}
								SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
								map.put("logDate", sdf.format(new Date()));
								isSaved = false;
							}
							/** First Start **/
							else {
								map.put("logTime", matcher.group(Integer.parseInt(patternIdx.get(0))));
								map.put("wasThread", matcher.group(Integer.parseInt(patternIdx.get(1))));
								map.put("logLevel", matcher.group(Integer.parseInt(patternIdx.get(2))));
								map.put("callMethod", matcher.group(Integer.parseInt(patternIdx.get(3))));						
								sb.append(line);
								if (LOGGER.isDebugEnabled()) {
									for (int i = 0; i <= matcher.groupCount(); i++) {
										LOGGER.debug(matcher.group(i));
										
									}
								}
								SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
								map.put("logDate", sdf.format(new Date()));
								isSaved = true;
							}
					    }
					    else {
					    	sb.append(line);
					    }
					} catch (Exception e) {
						LOGGER.error("Line processing error", e);
					}
				}
				else {
					/** Next line is null and map is not null, in this case not saved log context, insert */
					if (map.get("logTime") != null) {
						map.put("detailLog", sb.toString());
						insertLog(map);
						int len = sb.length();
						sb.delete(0, len);
						isSaved = false;
						map.clear();						
					}
				}

			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private class TailerListener implements org.apache.commons.io.input.TailerListener {

		private final File file;

		private TailerListener(final File file) {
			this.file = file;
		}

		public void init(final Tailer tailer) {
			LOGGER.info("Tailing file {}", file.getAbsolutePath());
		}

		public void fileNotFound() {
			LOGGER.error("Tailer error: file not found: {}", file.getAbsolutePath());

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}

		public void fileRotated() {
			LOGGER.info("Tailer: file was rotated: {}", file.getAbsolutePath());

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}

		public void handle(final String line) {
			try {
				readQueue.put(line);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		public void handle(final Exception ex) {
			LOGGER.error("Tailer error", ex);

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}
	}

	
	/**
	 * 
	 *<pre>
	 *
	 *</pre>
	 * @param map
	 * @throws Exception
	 */
	private void insertLog(Map<String, Object> map) {
		BaseDao baseDao = new BaseDao();
		SqlSession sqlSession = null;
		try {
			sqlSession = MybatisSqlSession.beginTransaction();
			if (sqlSession == null) {
				throw new RuntimeException ("SqlSession is null");
			}
			
			baseDao.insert(sqlSession, "log.tailer.insLog", map);
		} catch (Exception e) {
			try {
				MybatisSqlSession.abortTransaction(sqlSession);				
			}
			catch(Exception pe) {
				pe.printStackTrace();
			}
			e.printStackTrace();
		}
		finally {
			try {
				MybatisSqlSession.endTransaction(sqlSession);				
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
