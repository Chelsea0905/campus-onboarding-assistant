package com.yupi.yuaiagent.campus;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DeepSeekAnswerGeneratorTest {
    @Test
    void sendsGroundedPromptAndReadsAnswer() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("deepseek-flash"))
                .andExpect(jsonPath("$.messages[1].content").value(org.hamcrest.Matchers.containsString("资料片段：\n证据 [1]")))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"请带通知书 [1]\"}}]}", MediaType.APPLICATION_JSON));

        var generator = new DeepSeekAnswerGenerator(builder, "test-key", "https://api.deepseek.com", "deepseek-flash");
        assertEquals("请带通知书 [1]", generator.generate("带什么？", "证据 [1]"));
        server.verify();
    }

    @Test
    void refusesMissingKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeepSeekAnswerGenerator(RestClient.builder(), " ", "https://api.deepseek.com", "deepseek-flash"));
    }
}
