package io.choerodon.iam.api.vo.devops;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class UserAttrVO {
    @ApiModelProperty("choerodon用户id")
    @Encrypt
    private Long iamUserId;
    @ApiModelProperty("gitlab用户id")
    private Long gitlabUserId;

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }
}
