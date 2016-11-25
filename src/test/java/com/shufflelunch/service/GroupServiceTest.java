package com.shufflelunch.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.shufflelunch.ApplicationConfigurer;
import com.shufflelunch.config.TestConfig;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.User;

/**
 * @author seisuke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(
        initializers = ConfigFileApplicationContextInitializer.class,
        classes = {
                TestConfig.class,
                ApplicationConfigurer.class,
                MBeanExporter.class,
        })
@WebAppConfiguration
@TestExecutionListeners({
                                DependencyInjectionTestExecutionListener.class,
                                DirtiesContextTestExecutionListener.class,
                        })
public class GroupServiceTest {

    @InjectMocks
    GroupService groupService;

    @Mock
    FireBaseDao fireBaseDao;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void calcMemberNumber() {

        assertThat(groupService.calcMemberNumber(5)
                , is(Arrays.asList(5)));

        assertThat(groupService.calcMemberNumber(6)
                , is(Arrays.asList(3, 3)));

        assertThat(groupService.calcMemberNumber(8)
                , is(Arrays.asList(4, 4)));

        assertThat(groupService.calcMemberNumber(9)
                , is(Arrays.asList(3, 3, 3)));

        assertThat(groupService.calcMemberNumber(11)
                , is(Arrays.asList(4, 4, 3)));

        assertThat(groupService.calcMemberNumber(15)
                , is(Arrays.asList(4, 4, 4, 3)));

    }

    @Test
    public void grouping() {
        User user1 = new User("1", "name1");
        User user2 = new User("2", "name2");
        User user3 = new User("3", "name3");
        User user4 = new User("4", "name4");
        User user5 = new User("5", "name5");
        User user6 = new User("6", "name6");
        User user7 = new User("7", "name7");

        List<User> inputList = new ArrayList<User>() {
            {
                add(user1);
                add(user2);
                add(user3);
                add(user4);
                add(user5);
                add(user6);
                add(user7);
            }
        };

        List<User> subResult1 = new ArrayList<User>() {
            {
                add(user1);
                add(user2);
                add(user3);
                add(user4);
            }
        };

        List<User> subResult2 = new ArrayList<User>() {
            {
                add(user5);
                add(user6);
                add(user7);
            }
        };

        List<List<User>> result = groupService.grouping(inputList, false);
        assertThat(result, is(Arrays.asList(subResult1, subResult2)));

    }

}
