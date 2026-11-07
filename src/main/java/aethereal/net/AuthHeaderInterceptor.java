package aethereal.net;
import aethereal.model.AccountProfile;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class AuthHeaderInterceptor implements Interceptor {
    public final AccountProfile accountProfile;

    public AuthHeaderInterceptor(AccountProfile class013Var) {
        this.accountProfile = class013Var;
    }

    @NotNull
    public Response intercept(Interceptor.Chain chain) throws java.io.IOException {
        Request.Builder builderHeader = chain.request().newBuilder().header("X-Uid", this.accountProfile.uid()).header("X-Hwid", this.accountProfile.hwid()).header("X-Login", this.accountProfile.login()).header("Authorization", this.accountProfile.signature());
        if (this.accountProfile.avatarUrl() != null) {
            builderHeader.header("X-Avatar-Url", this.accountProfile.avatarUrl());
        }
        return chain.proceed(builderHeader.build());
    }
}
