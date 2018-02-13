package dymn.log.mybatis;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaseDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

	@Inject
	public BaseDao() {
		
	}
	/**
	 * 
	 *<pre>
	 *
	 *</pre>
	 * @param sqlId
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public <T, V> T select(SqlSession sqlSession, String sqlId, V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}
		return sqlSession.selectOne(sqlId, param);			
	}

	/**
	 * 
	 *<pre>
	 *
	 *</pre>
	 * @param sqlId
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T, V> T selectList(SqlSession sqlSession, String sqlId,V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}

		return (T)sqlSession.selectList(sqlId, param);
	}

	/**
	 * 
	 *<pre>
	 * Insert row
	 *</pre>
	 * @param sqlId String
	 * @param param V
	 * @return int
	 * @throws Exception
	 */
	public <V> int insert(SqlSession sqlSession, String sqlId,V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}
		
		return sqlSession.insert(sqlId, param);
	}

	/**
	 * 
	 *<pre>
	 * Insert many rows
	 *</pre>
	 * @param sqlSession
	 * @param sqlId
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public <V> int insertList(SqlSession sqlSession, String sqlId,V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}
		
		int cnt = 0;
		if (param instanceof List) {
			List<?> tempList = (List<?>) param;
			for(Object object : tempList) {
				sqlSession.insert(sqlId, object);
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * 
	 *<pre>
	 * update one row
	 *</pre>
	 * @param sqlId String
	 * @param param V
	 * @return int
	 * @throws Exception
	 */
	public <V> int update(SqlSession sqlSession, String sqlId, V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}

		return sqlSession.update(sqlId, param);
	}
	
	/**
	 * 
	 *<pre>
	 * delete one row
	 *</pre>
	 * @param sqlId String
	 * @param param V
	 * @return int
	 * @throws Exception
	 */
	public <V >int delete(SqlSession sqlSession, String sqlId, V param) throws Exception {
		if (sqlSession == null) {
			throw new RuntimeException("sqlSession is null");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} is executed", sqlId);
		}

		return sqlSession.delete(sqlId, param);
	}

}
