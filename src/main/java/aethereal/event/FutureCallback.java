package aethereal.event;
import aethereal.net.CloudConfigService;

import java.util.concurrent.CompletableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureCallback<T> implements Callback<T> {
    final CompletableFuture future;

    public FutureCallback(CompletableFuture completableFuture) {
        this.future = completableFuture;
    }

    public void onResponse(Call<T> call, Response<T> response) {
        if (!response.isSuccessful() || response.body() == null) {
            this.future.completeExceptionally(CloudConfigService.httpError(response));
        } else {
            this.future.complete(response.body());
        }
    }

    public void onFailure(Call<T> call, Throwable th) {
        this.future.completeExceptionally(th);
    }
}
