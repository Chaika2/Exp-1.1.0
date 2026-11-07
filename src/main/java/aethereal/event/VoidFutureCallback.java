package aethereal.event;
import aethereal.net.CloudConfigService;

import java.util.concurrent.CompletableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoidFutureCallback implements Callback<Void> {
    final CompletableFuture future;

    public VoidFutureCallback(CompletableFuture completableFuture) {
        this.future = completableFuture;
    }

    public void onResponse(Call<Void> call, Response<Void> response) {
        if (response.isSuccessful()) {
            this.future.complete(null);
        } else {
            this.future.completeExceptionally(CloudConfigService.httpError(response));
        }
    }

    public void onFailure(Call<Void> call, Throwable th) {
        this.future.completeExceptionally(th);
    }
}
