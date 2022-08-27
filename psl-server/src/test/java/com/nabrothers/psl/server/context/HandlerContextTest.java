package com.nabrothers.psl.server.context;

import com.nabrothers.psl.sdk.annotation.Handler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/core-appcontext-core.xml"})
@Handler(command = "测试")
@Component
public class HandlerContextTest {

    private HandlerContext context = HandlerContext.getInstance();

    @PostConstruct
    private void init() {
        context.load("com.nabrothers.psl.server");
    }

    @Test
    public void test0() {
        Assert.assertEquals("this is a test", context.handle("测试"));
    }

    @Test
    public void test1() {
        Assert.assertEquals(context.handle("测试 测试1"), "123");
        Assert.assertEquals(context.handle("测试 测试1 测试"), "456");
    }

    @Test
    public void test2() {
        Assert.assertEquals(context.handle("测试 测试2 PSL"), "PSL");
        Assert.assertEquals(context.handle("测试 测试2 测试"), "123");
        Assert.assertThrows(RuntimeException.class, () -> context.handle("测试 测试2"));
    }

    @Test
    public void test3() {
        Assert.assertEquals(context.handle("测试 测试3 PSL PSL PSL"), "1_PSL 2_PSL 3_PSL");
        Assert.assertEquals(context.handle("测试 测试3 PSL PSL PSL PSL"), "1_PSL 2_PSL 3_PSL 4_PSL");
        Assert.assertThrows(RuntimeException.class, () -> context.handle("测试 测试3 PSL PSL"));
    }

    @Handler()
    public String handler0() {
        return "this is a test";
    }

    @Handler(command = "测试1", info = "测试无参数")
    public String handler1_1() {
        return "123";
    }

    @Handler(command = "测试1 测试", info = "测试重复参数")
    public String handler1_2() {
        return "456";
    }

    @Handler(command = "测试2", info = "测试一个参数")
    public String handler2_1(String param1) {
        return param1;
    }

    @Handler(command = "测试2 测试", info = "测试重复参数")
    public String handler2_2() {
        return "123";
    }

    @Handler(command = "测试3", info = "测试多个参数")
    public String handler3_1(String param1, String param2, String param3) {
        return String.format("1_%s 2_%s 3_%s", param1, param2, param3);
    }

    @Handler(command = "测试3", info = "测试多个参数")
    public String handler3_2(String param1, String param2, String param3, String param4) {
        return String.format("1_%s 2_%s 3_%s 4_%s", param1, param2, param3, param4);
    }
}
