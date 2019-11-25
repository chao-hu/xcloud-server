package com.xxx.xcloud.client.gitlab.model;

import com.google.gson.annotations.SerializedName;

/**
 * gitlab Token信息
 *
 * @author mengaijun
 * @date: 2018年12月21日 上午11:25:43
 */
public class GitlabToken {
    /**
     * 使用的token字段, 可以根据此token获取用户的gitlab项目信息
     */
    @SerializedName(value = "access_token")
    private String accessToken;

    @SerializedName(value = "token_type")
    private String tokenType;

    @SerializedName(value = "refresh_token")
    private String refreshToken;

    @SerializedName(value = "created_at")
    private String createdAt;

    @SerializedName(value = "scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "GitlabToken [accessToken=" + accessToken + ", tokenType=" + tokenType + ", refreshToken=" + refreshToken
                + ", createdAt=" + createdAt + ", scope=" + scope + "]";
    }
}
