package com.shufflelunch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
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
import com.shufflelunch.handler.LanguageHandler;
import com.shufflelunch.model.Participant;
import com.shufflelunch.model.User;
import com.shufflelunch.service.ParticipantService;
import com.shufflelunch.service.ProfileService;
import com.shufflelunch.service.TranslationService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.profile.UserProfileResponse;

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
    UserService userService;

    @MockBean
    ProfileService profileService;

    @MockBean
    ParticipantService participantService;

    @Autowired
    TranslationService translationService;

    @Autowired
    LanguageHandler languageHandler;

    private MockMvc mockMvc;
    private static MockWebServer server;

    @BeforeClass
    public static void beforeClass() {
        server = new MockWebServer();
        System.setProperty("line.bot.apiEndPoint", server.url("/").toString());
    }

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                                 .build();
    }

    @Test
    public void joinCallbackTest() throws Exception {

        Optional<User> user = Optional.of(new User("sjkghfjhsg", "Brown", Locale.JAPANESE.getLanguage()));
        when(userService.getUser(any())).thenReturn(user);

        server.enqueue(new MockResponse().setBody("{}"));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-join.json");
        byte[] json = ByteStreams.toByteArray(resource);

        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        // Test request 2
        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request2.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request2.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        String body = request2.getBody().readUtf8();
        assertThat(body).contains("{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"template\",\"altText\":\"シャッフルランチに参加しますか？\",\"template\":{\"type\":\"confirm\","
                                  + "\"text\":\"シャッフルランチに参加しますか？\",\"actions\":[{\"type\":\"postback\",\"label\":\"Yes\",\"data\":\"join_yes\"},{\"type\":\"postback\",\"label\":\"No\","
                                  + "\"data\":\"join_no\"}]}}]}");
    }

    @Test
    public void confirmCallbackTest() throws Exception {

        Optional<User> user = Optional.of(new User("sjkghfjhsg", "Brown", Locale.JAPANESE.getLanguage()));
        when(userService.getUser(any())).thenReturn(user);
        when(participantService.getParticipant(any())).thenReturn(Optional.empty());

        server.enqueue(new MockResponse().setBody("{}"));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-confirm.json");
        byte[] json = ByteStreams.toByteArray(resource);

        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        // Test request 2
        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request2.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request2.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        String body = request2.getBody().readUtf8();
        assertThat(body)
                .contains("{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"Brownさんを参加者リストに加えました。\"}]}");

    }

    @Test
    public void confirmAlreadyJoinedTest() throws Exception {

        Optional<User> user = Optional.of(new User("sjkghfjhsg", "Brown", Locale.JAPANESE.getLanguage()));
        when(userService.getUser(any())).thenReturn(user);
        when(participantService.getParticipant(any())).thenReturn(Optional.of(new Participant(user.get())));

        server.enqueue(new MockResponse().setBody("{}"));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-confirm.json");
        byte[] json = ByteStreams.toByteArray(resource);

        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        RecordedRequest request = server.takeRequest(3, TimeUnit.SECONDS);
        String body = request.getBody().readUtf8();
        assertThat(body).contains(
                "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"あなたは既に今日のランチに参加しています。\"}]}");
    }

    @Test
    public void followCallbackTest() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));

        String userId = "U206d25c2ea6bd87c17655609a1c37cb8";
        Optional<User> user = Optional.empty();
        UserProfileResponse userProfile = new UserProfileResponse("Brown", userId, "https://abc.picuture.jpg", "status");
        when(userService.getUser(userId)).thenReturn(user);
        when(profileService.getProfile(userId)).thenReturn(Optional.of(userProfile));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        // Request
        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-follow.json");
        byte[] json = ByteStreams.toByteArray(resource);
        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        // Validate Response
        RecordedRequest recordedRequest = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(recordedRequest.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        String body = recordedRequest.getBody().readUtf8();
        assertThat(body)
                .contains(
                        "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"こんにちは Brownさん, ようこそシャッフルランチへ\"}]}");
    }

    @Test
    public void unfollowCallbackTest() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));

        String userId = "U206d25c2ea6bd87c17655609a1c37cb8";
        Optional<User> user = Optional.of(new User(userId, "Brown", Locale.JAPANESE.getLanguage()));
        when(userService.getUser(userId)).thenReturn(user);

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        // Request
        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-unfollow.json");
        byte[] json = ByteStreams.toByteArray(resource);
        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());
    }

    @Test
    public void helpCallbackTest() throws Exception {

        server.enqueue(new MockResponse().setBody("{}"));

        String signature = "ECezgIpQNUEp4OSHYd7xGSuFG7e66MLPkCkK1Y28XTU=";

        InputStream resource = getClass().getClassLoader().getResourceAsStream("callback-help.json");
        byte[] json = ByteStreams.toByteArray(resource);

        mockMvc.perform(MockMvcRequestBuilders.post("/callback")
                                              .header("X-Line-Signature", signature)
                                              .content(json))
               .andDo(print())
               .andExpect(status().isOk());

        // Test request 2
        RecordedRequest request2 = server.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request2.getPath()).isEqualTo("/v2/bot/message/reply");
        assertThat(request2.getHeader("Authorization")).isEqualTo("Bearer TOKEN");
        String body = request2.getBody().readUtf8();
        assertThat(body)
                .contains(
                        "{\"replyToken\":\"nHuyWiB7yP5Zw52FIkcQobQuGDXCTA\",\"messages\":[{\"type\":\"text\",\"text\":\"Available commands are :\"},{\"type\":\"text\",\"text\":\" - help : displays "
                        + "this help message\"},{\"type\":\"text\",\"text\":\" - join : allow you to register for next lunch\"},{\"type\":\"text\",\"text\":\" - leave : remove your registration for"
                        + " the next lunch\"},{\"type\":\"text\",\"text\":\" - group : lets you know your group and meeting point for lunch\"}]}");
    }

    @Test
    public void translationTest() {
        assertThat(translationService.getTranslation("test.welcome", Locale.JAPANESE.getLanguage())).isEqualTo("こんにちは！");
        assertThat(translationService.getTranslation("test.welcome", Locale.ENGLISH.getLanguage())).isEqualTo("Welcome!");

        assertThat(translationService.getTranslation("test.hello", Arrays.asList("Brown"), Locale.JAPANESE.getLanguage())).isEqualTo("こんにちはBrown！");
        assertThat(translationService.getTranslation("test.hello", Arrays.asList("Brown"), Locale.ENGLISH.getLanguage())).isEqualTo("Hello Brown!");
    }
}
