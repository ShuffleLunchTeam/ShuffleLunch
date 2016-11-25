package com.shufflelunch.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;
import com.shufflelunch.Application;
import com.shufflelunch.handler.DebugHandler;
import com.shufflelunch.handler.FollowLunchHandler;
import com.shufflelunch.handler.GroupHandler;
import com.shufflelunch.handler.HelpHandler;
import com.shufflelunch.handler.JoinLunchHandler;
import com.shufflelunch.handler.LanguageHandler;
import com.shufflelunch.model.User;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Slf4j
@LineMessageHandler
public class SchuffleLunchController {

    @Autowired
    private LineMessagingService lineMessagingService;

    @Autowired
    private JoinLunchHandler joinLunchHandler;

    @Autowired
    private FollowLunchHandler followLunchHandler;

    @Autowired
    private HelpHandler helpHandler;

    @Autowired
    private DebugHandler debugHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private LanguageHandler languageHander;

    @Autowired
    private GroupHandler groupHandler;

    @EventMapping
    public void handleFollowEvent(FollowEvent event) throws IOException {
        String replyToken = event.getReplyToken();
        Optional<Message> maybeMessage = followLunchHandler.handleFollow(event);
        maybeMessage.ifPresent(message ->
                                       reply(replyToken, message)
        );
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        String userId = event.getSource().getUserId();
        if (StringUtils.isEmpty(userId)) {
            log.error("UnfollowEvent: userId is empty: {}", event);
            return;
        }

        Optional<User> userMaybe = userService.getUser(userId);
        if (userMaybe.isPresent()) {
            userService.deleteUser(userMaybe.get());
            log.info("UnfollowEvent: User unfollowed this bot: {}", event);
        } else {
            log.warn("UnfollowEvent: Not registered user tries to unfollow this bot: {}", event);
        }
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws IOException {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        handleSticker(event.getReplyToken(), event.getMessage());
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        LocationMessageContent locationMessage = event.getMessage();
        reply(event.getReplyToken(), new LocationMessage(
                locationMessage.getTitle(),
                locationMessage.getAddress(),
                locationMessage.getLatitude(),
                locationMessage.getLongitude()
        ));
    }

    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        // You need to install ImageMagick
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent jpg = saveContent("jpg", responseBody);
                    DownloadedContent previewImg = createTempFile("jpg");
                    system(
                            "convert",
                            "-resize", "240x",
                            jpg.path.toString(),
                            previewImg.path.toString());
                    reply(((MessageEvent) event).getReplyToken(),
                          new ImageMessage(jpg.getUri(), jpg.getUri()));
                });
    }

    @EventMapping
    public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent mp4 = saveContent("mp4", responseBody);
                    reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
                });
    }

    @EventMapping
    public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
        // You need to install ffmpeg and ImageMagick.
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent mp4 = saveContent("mp4", responseBody);
                    DownloadedContent previewImg = createTempFile("jpg");
                    system("convert",
                           mp4.path + "[0]",
                           previewImg.path.toString());
                    reply(((MessageEvent) event).getReplyToken(),
                          new VideoMessage(mp4.getUri(), previewImg.uri));
                });
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Joined " + event.getSource());
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws IOException {
        String replyToken = event.getReplyToken();
        switch (event.getPostbackContent().getData()) {
            case "join_yes": {
                reply(replyToken, joinLunchHandler.handleJoinConfirmation(event));
                break;
            }
            case "join_no": {
                reply(replyToken, joinLunchHandler.handleNotJoinConfirmation(event));
                break;
            }
            case "lang_ja": {
                reply(replyToken, languageHander.handleLanguageChange(event));
                break;
            }
            case "lang_en": {
                reply(replyToken, languageHander.handleLanguageChange(event));
                break;
            }
            case "lunch_member": {
                this.replyText(replyToken, event.getPostbackContent().getData());
                break;
            }
        }
    }

    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleLeaveEvent(LeaveEvent event) {
        String userId = event.getSource().getUserId();
        log.info("User:{} just left", userId);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .execute();
            log.info("Sent messages: {}", apiResponse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleHeavyContent(String replyToken, String messageId,
                                    Consumer<ResponseBody> messageConsumer) throws IOException {
        Response<ResponseBody> response = lineMessagingService.getMessageContent(messageId)
                                                              .execute();
        if (response.isSuccessful()) {
            try (ResponseBody body = response.body()) {
                messageConsumer.accept(body);
            }
        } else {
            reply(replyToken, new TextMessage("Cannot get image: " + response.message()));
        }

    }

    private void handleSticker(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(
                content.getPackageId(), content.getStickerId())
        );
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws IOException {
        String text = content.getText();

        log.info("Got text message from {}: {}", replyToken, text);
        switch (text) {

            //////////////////
            // ShuffleLunch //
            //////////////////
            case "join": {
                this.reply(replyToken, joinLunchHandler.handleJoinRequest(event));
                break;
            }
            case "leave":
                this.reply(replyToken, joinLunchHandler.handleUnSubscribe(event));
                break;

            case "help":
                reply(replyToken, helpHandler.handleHelp());
                break;

            case "language":
                reply(replyToken, languageHander.handleLanguage());
                break;

            case "debug":
                reply(replyToken, debugHandler.handleDebug());
                break;

            case "group":
                reply(replyToken, groupHandler.handleGroupRequest(event));
                break;

            case "shuffle":
                reply(replyToken, groupHandler.handleShuffleGroup());
                break;

            default:
                log.info("Unhandled message {}: {}", replyToken, text);
                break;
        }
    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(path).build()
                                          .toUriString();
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static DownloadedContent saveContent(String ext, ResponseBody responseBody) {
        log.info("Got content-type: {}", responseBody.contentType());
        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.byteStream(), outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = Application.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
}
