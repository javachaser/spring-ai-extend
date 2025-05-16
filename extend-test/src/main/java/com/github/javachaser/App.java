package com.github.javachaser;

import com.github.javachaser.service.dify.DifyClientChatModel;
import com.github.javachaser.service.dify.api.DifyAiChatApi;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        DifyAiChatApi difyAiChatApi = new DifyAiChatApi("app-L7v5UjVvKpeezCqlVHYbUJZj");

        DifyClientChatModel chatModel = new DifyClientChatModel(difyAiChatApi);

//        String resp = chatModel.call("你好呀,你能做些什么");

//        System.out.println(resp);
//        DifyAiChatApi.ChatCompletionRequest chatCompletionRequest = new DifyAiChatApi.ChatCompletionRequest(
//                "你好呀,你能做些什么",
//                Map.of(),
//                "streaming",
//                "test",
//                null,
//                null,
//                null
//        );
//        ResponseEntity<DifyAiChatApi.ChatResponse> response = difyAiChatApi.chatCompletionEntity(chatCompletionRequest);
//
//        System.out.println(response.getBody());
//        Flux<DifyAiChatApi.ChunkChatCompletionResponse> chatCompletionStream = difyAiChatApi.chatCompletionStream(chatCompletionRequest);
        Prompt prompt = new Prompt("你好呀,你能做些什么");
        Flux<ChatResponse> chatCompletionStream = chatModel.stream(prompt);
        chatCompletionStream.toStream()
                .forEach(response -> System.out.println("收到：" + ModelOptionsUtils.toJsonString(response)));
//        chatCompletionStream.doOnNext(response -> System.out.println("收到：" + response));
//        chatCompletionStream.subscribe(response -> System.out.println("收到：" + response));

        TimeUnit.SECONDS.sleep(10);
    }
}
