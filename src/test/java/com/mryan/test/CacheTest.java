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
     *
     * @throws IOException
     */
    @Test
    public void TEST_QUERY_BY_FIRST_CACHE() throws IOException {
        //代理模式获取代理类
        IUserMapper userMapper = sqlSession.getMapper(IUserMapper.class);
        //第⼀次查询，发出sql语句，并将查询出来的结果放进缓存中
        User user1 = userMapper.findById(1);
        System.out.println("第一次查询：" + user1);
        //第⼆次查询，由于是同⼀个sqlSession,会在缓存中查询结果
        //如果有，则直接从缓存中取出来，不查库
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
     * 测试二级缓存
     */
    @Test
    public void SecondLevelCache() {
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

