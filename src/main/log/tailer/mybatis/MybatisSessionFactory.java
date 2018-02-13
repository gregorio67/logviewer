package dymn.log.mybatis;

import java.io.InputStream;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MybatisSessionFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MybatisSessionFactory.class);
	
	/** Sql Session Factory **/
	private static SqlSessionFactory sqlSessionFactory;
	
	private static Object sync = new Object();
	private static final String DEFAULT_MYBATIS_CONFIG = "sqlmap/config/mybatis-config.xml";
	
	
	/**
	 * 
	 *<pre>
	 * Create SqlSessionFactory with mybatis configuration file
	 *</pre>
	 * @return SqlSessionFactory
	 * @throws Exception
	 */
	public static SqlSessionFactory getInstance() throws Exception {
		return getInstance(DEFAULT_MYBATIS_CONFIG);
	}

	/**
	 * 
	 *<pre>
	 * Create SqlSessionFactory with mybatis configuration file
	 *</pre>
	 * @param resource String 
	 * @return SqlSessionFactory
	 * @throws Exception
	 */
	public static SqlSessionFactory getInstance(String resource) throws Exception {
		if (sqlSessionFactory == null) {
			synchronized(sync) {
				InputStream inputStream = Resources.getResourceAsStream(resource);
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
				LOGGER.info("SqlSessionFactory is create with {} file", resource);
				return sqlSessionFactory;
			}
		}
		return sqlSessionFactory;
		
	}
	
	public static SqlSession getSqlSession() throws Exception {
		if (sqlSessionFactory == null) {
			getInstance();
		}
		SqlSession sqlSession = sqlSessionFactory.openSession();
		return sqlSession;
	}
	
	/**
	 * 
	 *<pre>
	 * Start 
	 *</pre>
	 * @return
	 * @throws Exception
	 */
	public static SqlSession beginTransaction() throws Exception {
		if (sqlSessionFactory == null) {
			getInstance();
		}
		SqlSession sqlSession = sqlSessionFactory.openSession();
		return sqlSession;
	}
	
	/**
	 * 
	 *<pre>
	 * Transaction is completely end
	 *</pre>
	 * @param sqlSession SqlSession
	 * @throws Exception
	 */
	public static void endTransaction(SqlSession sqlSession) throws Exception {
		try {
			sqlSession.commit();						
		}
		catch(PersistenceException ex) {
			ex.printStackTrace();
		}
		finally {
			sqlSession.close();				
		}
	}
	
	/**
	 * 
	 *<pre>
	 * Transaction is aborted
	 *</pre>
	 * @param sqlSession SqlSession
	 * @throws Exception
	 */
	public static void abortTransaction(SqlSession sqlSession) throws Exception {
		try {
			sqlSession.rollback();						
		}
		catch(PersistenceException ex) {
			ex.printStackTrace();
		}
		finally {
			sqlSession.close();				
		}
	}
}
