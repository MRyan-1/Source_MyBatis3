package com.mryan.test;

import com.mryan.mapper.IOrderMapper;
import com.mryan.mapper.IUserMapper;
import com.mryan.pojo.Order;
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
import java.util.List;

public class MybatisTest {

    private IOrderMapper orderMapper;
    private IUserMapper userMapper;
    private SqlSession sqlSession;
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void before() throws IOException {
        //1. 加载sqlMapConfig.xml配置文件，将其转换成输入流存入内存中
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        //2. 解析配置文件，封装Configuration对象  创建DefaultSqlSessionFactory对象
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        // 3. 生产了DefaultSqlSession实例对象  设置了事务不自动提交  完成了executor对象的创建
        sqlSession = sqlSessionFactory.openSession();
        //JDK动态代理
        orderMapper = sqlSession.getMapper(IOrderMapper.class);
        userMapper = sqlSession.getMapper(IUserMapper.class);
    }

    @After
    public void after() {
        // 5.释放资源
        sqlSession.close();
    }


    /*
        测试一对一查询
     */
    @Test
    public void TEST_QUERY_ONE_TO_ONE() throws IOException {
        List<Order> orderAndUser = orderMapper.findOrderAndUser();
        for (Order order : orderAndUser) {
            System.out.println(order);
        }
    }

    /**
     * 传统方式查询
     *
     * @throws IOException
     */
    @Test
    public void TEST_QUERY_BY_TRADITIONAL() throws IOException {
        // 4.(1)根据statementid来从Configuration中map集合中获取到了指定的MappedStatement对象
        //(2)将查询任务委派了executor执行器
        User user1 = sqlSession.selectOne("com.mryan.mapper.IUserMapper.findById", 1);
        System.out.println(user1);
        User user2 = sqlSession.selectOne("com.mryan.mapper.IUserMapper.findById", 1);
        System.out.println(user2);
    }


    /*
        测试一对多查询
     */
    @Test
    public void TEST_QUERY_ONE_TO_MANY() throws IOException {
        List<User> users = userMapper.findAll();
        for (User user : users) {
            System.out.println(user.getUsername());
            System.out.println(user.getOrderList());
            System.out.println("============");
        }
    }

    /*
    测试 延迟加载查询
   */
    @Test
    public void TEST_QUERY_LAZY() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = factory.openSession();
        User user = sqlSession.selectOne("com.mryan.mapper.IUserMapper.findById", 1);
        //延迟加载生效 下方输出语句 不涉及到orders表 于是不会打印orders相关日志
        System.out.println("user：" + user.getUsername());
        //涉及orders表 才会执行相关SQL语句 加载orders执行日志 （延迟加载 什么时候用什么时候查）
        System.out.println("orders：" + user.getOrderList());
    }


}



