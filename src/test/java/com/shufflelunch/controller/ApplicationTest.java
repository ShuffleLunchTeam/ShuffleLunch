package com.shufflelunch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.io.ByteStreams;
import com.shufflelunch.model.User;
import com.shufflelunch.service.UserService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

// integration test
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "line.bot.handler.enabled=true")
public class ApplicationTest {

    static {
        System.setProperty("line.bot.channelSecret", "SECRET");
        System.setProperty("line.bot.channelToken", "TOKEN");
    }

    @Autowired
    private WebApplicationContext wac;

    @MockBean
    UserService sellerService;

    private MockMvc mockMvc;
    private static MockWebServer server;

    @BeforeClass
    public static void beforeClass() {
        server = new MockWebServer();
        System.setProperty("line.bot.apiEndPoint", server.url("/").toString());
    }

    @Before
    public void before() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                                      .build();
    }

    @Test
    public void missingSignatureTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .content("{}"))
               .andDo(print())
               .andExpect(status().isBadRequest())
               .andExpect(content().string(containsString("Missing 'X-Line-Signature' header")));
    }

    @Test
    public void validCallbackTest() throws Exception {

        Optional<User> user = Optional.of(new User("sjkghfjhsg", "Brown"));
        when(sellerService.getUser(any())).thenReturn(user);

        server.enqueue(new MockResponse().setBody("{}"));
        server.enqueue(new MockResponse().setBody("{}"));
        server.enqueue(new MockResponse().setBody("{}"));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-request.json");
        byte[] json = ByteStreams.toByteArray(resource);

        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        // Test request 1
        RecordedRequest request1 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request1.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request1.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        assertThat(request1.getBody().readUtf8())
                .isEqualTo(
                        "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\"," +
                        "\"text\":\"Hello, world\"}]}");

        // Test request 2
        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request2.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request2.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        assertThat(request2.getBody().readUtf8())
                .isEqualTo(
                        "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"You subscribed for today's lunch\"}]}")
        ;

        // Test request 3
        RecordedRequest request3 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request3.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request3.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        assertThat(request3.getBody().readUtf8())
                .isEqualTo(
                        "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"Hello Brown, welcome to Shuffle Lunch!\\nDo you want want to join today?\\n\"}]}");
    }
}
