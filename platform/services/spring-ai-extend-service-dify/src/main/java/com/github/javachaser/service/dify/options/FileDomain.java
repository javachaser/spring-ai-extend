package com.github.javachaser.service.dify.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 85949
 * @date 2025/5/6 16:43
 * @description
 */
@Getter
@Setter
public class FileDomain {

    private @JsonProperty("type") String type;

    /**
     * 传递方式
     * remote_url 图片地址
     * local_file 上传文件
     */
    private @JsonProperty("transfer_method") String transferMethod;

    /**
     * 图片地址。（仅当传递方式为 remote_url 时）。
     */
    private @JsonProperty("url") String url;

    /**
     * 上传文件 ID。（仅当传递方式为 local_file 时）
     */
    private @JsonProperty("upload_file_id") String uploadFileId;

}
