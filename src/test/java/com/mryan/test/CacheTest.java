package com.mryan.test;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2021/8/22 10:21 下午
 * @Version 1.0
 */

import com.mryan.mapper.IUserMapper;
import com.mryan.pojo.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * 缓存相关测试
 */
public class CacheTest {

    private IUserMapper userMapper;
    private SqlSession sqlSession;
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void before() throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        sqlSession = sqlSessionFactory.openSession();
        userMapper = sqlSession.getMapper(IUserMapper.class);
    }

    @After
    public void after() {
        sqlSession.close();
    }


    /**
     * 测试一级缓存
     */
    @Test
    public void TEST_QUERY_BY_FIRST_CACHE() {
        //代理模式获取代理类
        IUserMapper userMapper = sqlSession.getMapper(IUserMapper.class);
        //第⼀次sql语句查询 将查询结果放入缓存中
        User user1 = userMapper.findById(1);
        System.out.println("第一次查询：" + user1);
        //第⼆次sql语句查询，由于是同⼀个sqlSession,会去查询缓存 如果缓存中没有则查库 缓存中有则直接取缓存
        User user2 = userMapper.findById(1);
        System.out.println("第二次查询：" + user2);
        System.out.println(user1 == user2);
    }


    /**
     * 测试一级缓存commit()是否重置缓存
     */
    @Test
    public void TEST_QUERY_COMMIT_BY_FIRST_CACHE() {
        //代理模式获取代理类
        IUserMapper userMapper = sqlSession.getMapper(IUserMapper.class);
        //第⼀次sql语句查询 将查询结果放入缓存中
        User user1 = userMapper.findById(1);
        System.out.println("第一次查询：" + user1);
        //更新操作 并提交sqlSession
        user1.setUsername("MRyan666");
        userMapper.updateById(user1);
        sqlSession.commit();
        User user2 = userMapper.findById(1);
        System.out.println("第二次查询：" + user2);
        System.out.println(user1 == user2);
    }


    /**
     * 一级缓存测试
     */
    @Test
    public void TEST_FIRST_LEVEL_CACHE() {
        // 第一次查询id为1的用户
        User user1 = userMapper.findById(1);
        //更新用户
        User user = new User();
        user.setId(1);
        user.setUsername("MRyan");
        userMapper.updateById(user);
        sqlSession.commit();
        sqlSession.clearCache();
        // 第二次查询id为1的用户
        User user2 = userMapper.findById(1);
        System.out.println("第一次查询：" + user1);
        System.out.println("第二次查询：" + user2);
        System.out.println(user1 == user2);
    }


    /**
     * 测试二级缓存和sqlSession无关
     */
    @Test
    public void TEST_LEVEL_CACHE_NOT_RELEVANT() {
        //根据 sqlSessionFactory 产生 session
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        IUserMapper userMapper1 = sqlSession1.getMapper(IUserMapper.class);
        IUserMapper userMapper2 = sqlSession2.getMapper(IUserMapper.class); //第一次查询，发出sql语句，并将查询的结果放入缓存中
        User u1 = userMapper1.findById(1);
        System.out.println(u1);
        sqlSession1.close();
        //第一次查询完后关闭 sqlSession
        //第二次查询，即使sqlSession1已经关闭了，这次查询依然不发出sql语句
        User u2 = userMapper2.findById(1);
        System.out.println(u2);
        sqlSession2.close();
    }

    /**
     * 测试二级缓存 执行commit()操作，二级缓存数据清空
     */
    @Test
    public void TEST_LEVEL_CACHE_COMMIT() {
        //根据 sqlSessionFactory 产生 session
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        SqlSession sqlSession3 = sqlSessionFactory.openSession();
        String statement = "com.mryan.pojo.UserMapper.selectUserByUserld";
        IUserMapper userMapper1 = sqlSession1.getMapper(IUserMapper.class);
        IUserMapper userMapper2 = sqlSession2.getMapper(IUserMapper.class);
        IUserMapper userMapper3 = sqlSession2.getMapper(IUserMapper.class);
        //第一次查询，发出sql语句，并将查询的结果放入缓存中
        User u1 = userMapper1.findById(1);
        System.out.println(u1);
        sqlSession1.close();
        //第一次查询完后关闭sqlSession
        //执行更新操作，commit()
        u1.setUsername("aaa");
        userMapper3.updateById(u1);
        sqlSession3.commit();
        //第二次查询，由于上次更新操作，缓存数据已经清空(防止数据脏读)，这里必须再次发出sql语
        User u2 = userMapper2.findById(1);
        System.out.println(u2);
        sqlSession2.close();
    }

    /**
     * 测试二级缓存
     */
    @Test
    public void TEXT_SECOND_LEVEL_CACHE() {
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        SqlSession sqlSession3 = sqlSessionFactory.openSession();

        IUserMapper mapper1 = sqlSession1.getMapper(IUserMapper.class);
        IUserMapper mapper2 = sqlSession2.getMapper(IUserMapper.class);
        IUserMapper mapper3 = sqlSession3.getMapper(IUserMapper.class);

        User user1 = mapper1.findById(1);
        sqlSession1.close(); //清空一级缓存

        User user = new User();
        user.setId(1);
        user.setUsername("MRyan");
        mapper3.updateById(user);
        sqlSession3.commit();

        User user2 = mapper2.findById(1);

        System.out.println("第一次查询：" + user1);
        System.out.println("第二次查询：" + user2);
        System.out.println(user1 == user2);
    }


}

