package aethereal.net;
import aethereal.model.ConfigIdStub;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CloudConfigApi {
    @GET("configs/")
    Call<List<CloudConfigMetadata>> listConfigs();

    @GET("configs/{id}")
    Call<CloudConfigDto> fetchConfig(@Path("id") String str);

    @POST("configs/")
    Call<ConfigIdStub> createConfig(@Body CreateConfigRequest class366Var);

    @PUT("configs/")
    Call<Void> updateConfig(@Body UpdateConfigRequest class364Var);

    @PATCH("configs/")
    Call<Void> renameConfig(@Body RenameConfigRequest class367Var);

    @HTTP(method = "DELETE", path = "configs/", hasBody = true)
    Call<Void> deleteConfig(@Body DeleteConfigRequest class368Var);

    @POST("configs/import")
    Call<Void> importConfig(@Body ImportConfigRequest class365Var);
}
