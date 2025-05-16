package com.github.javachaser.service.dify.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * dify 参数类
 *
 * @author 85949
 * @date 2025/4/30 15:46
 * @description
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class DifyAiChatOptions extends AbstractChatOptions {

    private @JsonProperty("query") String query;

    private @JsonProperty("inputs") Map<String, Object> inputs;

    private @JsonProperty("response_mode") String responseMode;

    private @JsonProperty("user") String user;

    private @JsonProperty("conversation_id") String conversationId;

    private @JsonProperty("files") List<FileDomain> files;

    @Nullable
    private @JsonProperty("auto_generate_name") Boolean autoGenerateName;


    @Override
    @SuppressWarnings("unchecked")
    public DifyAiChatOptions copy() {
        return fromOptions(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DifyAiChatOptions fromOptions(DifyAiChatOptions fromOptions) {
        return DifyAiChatOptions.builder()
                .query(fromOptions.getQuery())
                .inputs(fromOptions.getInputs())
                .responseMode(fromOptions.getResponseMode())
                .user(fromOptions.getUser())
                .conversationId(fromOptions.getConversationId())
                .files(fromOptions.getFiles())
                .autoGenerateName(fromOptions.getAutoGenerateName())
                .build();
    }


    public static class Builder {

        protected DifyAiChatOptions options;

        public Builder() {
            this.options = new DifyAiChatOptions();
        }

        public Builder query(String query) {
            this.options.query = query;
            return this;
        }

        public Builder inputs(Map<String, Object> inputs) {
            this.options.inputs = inputs;
            return this;
        }

        public Builder responseMode(String responseMode) {
            this.options.responseMode = responseMode;
            return this;
        }

        public Builder user(String user) {
            this.options.user = user;
            return this;
        }

        public Builder conversationId(String conversationId) {
            this.options.conversationId = conversationId;
            return this;
        }

        public Builder files(List<FileDomain> files) {
            this.options.files = files;
            return this;
        }

        public Builder autoGenerateName(Boolean autoGenerateName) {
            this.options.autoGenerateName = autoGenerateName;
            return this;
        }

        public DifyAiChatOptions build() {
            return this.options;
        }
    }

}
