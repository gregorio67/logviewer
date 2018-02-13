package dymn.log.mybatis;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MybatisSqlSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MybatisSqlSession.class);

	public static SqlSession beginTransaction() throws Exception {
		SqlSession sqlSession = MybatisSessionFactory.getSqlSession();
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
			LOGGER.error(ex.getLocalizedMessage());
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
			LOGGER.error(ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		finally {
			sqlSession.close();				
		}
	}
}
