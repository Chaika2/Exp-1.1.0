package aethereal.net;
import aethereal.model.AccountProfile;
import aethereal.util.AtomicFileWriter;
import aethereal.model.ConfigReference;
import aethereal.config.ConfigSerializer;
import aethereal.event.FutureCallback;
import aethereal.Lang;
import aethereal.config.LoadedConfig;
import aethereal.config.ModuleRepository;
import aethereal.model.Translation;
import aethereal.event.VoidFutureCallback;
import aethereal.ui.WidgetStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudConfigService {
    public static final String baseUrl = "http://127.0.0.1:1/";
    public static final String configRoot = "expensive/config/configs";
    public final ConfigSerializer serializer = new ConfigSerializer();

    public final AtomicReference<ConfigReference> activeConfigRef = new AtomicReference<>();

    public volatile CloudConfigApi api;

    public volatile AccountProfile accountProfile;

    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final Path configDir = Path.of(configRoot);

    public CompletableFuture<Void> initialize(String str, String str2, String str3, String str4, String str5) {
        return CompletableFuture.runAsync(() -> {
            this.accountProfile = new AccountProfile(str, str2, str3, str4, str5);
            ensureConfigDir();
        });
    }

    public CloudConfigApi buildApi(AccountProfile class013Var) {
        return (CloudConfigApi) new Retrofit.Builder().baseUrl(baseUrl).client(new OkHttpClient.Builder().connectTimeout(10L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS).addInterceptor(new AuthHeaderInterceptor(class013Var)).build()).addConverterFactory(GsonConverterFactory.create()).build().create(CloudConfigApi.class);
    }

    public boolean initialized() {
        return (this.accountProfile == null || this.accountProfile.signature() == null) ? false : true;
    }

    public boolean configActive(String str) {
        ConfigReference class304Var = this.activeConfigRef.get();
        return (class304Var == null || str == null || !str.equals(class304Var.id())) ? false : true;
    }

    public Optional<ConfigReference> activeConfig() {
        return Optional.ofNullable(this.activeConfigRef.get());
    }

    public CompletableFuture<List<CloudConfigMetadata>> listConfigs() {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<CloudConfigMetadata> arrayList = new ArrayList<>();
            ensureConfigDir();
            try (Stream<Path> streamList = Files.list(this.configDir)) {
                streamList.filter(path -> {
                    return path.getFileName().toString().endsWith(".json");
                }).forEach(path2 -> {
                    CloudConfigDto class376VarRead = readDto(path2);
                    if (class376VarRead != null) {
                        CloudConfigMetadata class163Var = new CloudConfigMetadata();
                        class163Var.id = class376VarRead.id();
                        class163Var.name = class376VarRead.name();
                        class163Var.author = class376VarRead.author();
                        class163Var.createdAt = class376VarRead.createdAt();
                        class163Var.updatedAt = class376VarRead.updatedAt();
                        class163Var.authorAvatarUrl = null;
                        class163Var.localOverride = class376VarRead.isLocalOverride();
                        arrayList.add(class163Var);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to list configs", e);
            }
            return arrayList;
        });
    }

    public CompletableFuture<CloudConfigDto> fetchConfig(String str) {
        return CompletableFuture.supplyAsync(() -> {
            CloudConfigDto class376VarRead = readDto(pathFor(str));
            if (class376VarRead == null) {
                throw new RuntimeException("Config not found");
            }
            return class376VarRead;
        });
    }

    public CompletableFuture<String> createConfig(String str, ModuleRepository class795Var, WidgetStack class814Var) {
        return CompletableFuture.supplyAsync(() -> {
            ensureConfigDir();
            if (nameExists(str)) {
                throw new RuntimeException("name already exists");
            }
            String strUuid = UUID.randomUUID().toString();
            String strNow = Instant.now().toString();
            CloudConfigDto class376Var = new CloudConfigDto();
            class376Var.id = strUuid;
            class376Var.name = str;
            class376Var.base64 = serializeToBase64(class795Var, class814Var);
            class376Var.author = currentLogin();
            class376Var.createdAt = strNow;
            class376Var.updatedAt = strNow;
            class376Var.localOverride = true;
            writeDto(class376Var);
            return strUuid;
        });
    }

    public CompletableFuture<Void> saveConfig(String str, ModuleRepository class795Var, WidgetStack class814Var) {
        return CompletableFuture.runAsync(() -> {
            CloudConfigDto class376VarRead = readDto(pathFor(str));
            if (class376VarRead == null) {
                throw new RuntimeException("Config not found");
            }
            class376VarRead.base64 = serializeToBase64(class795Var, class814Var);
            class376VarRead.updatedAt = Instant.now().toString();
            writeDto(class376VarRead);
        });
    }

    public CompletableFuture<Void> renameConfig(String str, String str2) {
        return CompletableFuture.runAsync(() -> {
            CloudConfigDto class376VarRead = readDto(pathFor(str));
            if (class376VarRead == null) {
                throw new RuntimeException("Config not found");
            }
            if (nameExists(str2)) {
                throw new RuntimeException("name already exists");
            }
            class376VarRead.name = str2;
            class376VarRead.updatedAt = Instant.now().toString();
            writeDto(class376VarRead);
        });
    }

    public CompletableFuture<Void> deleteConfig(String str) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.deleteIfExists(pathFor(str));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete config", e);
            }
        });
    }

    public CompletableFuture<Void> importConfig(String str) {
        return CompletableFuture.runAsync(() -> {
            if (readDto(pathFor(str)) == null) {
                throw new RuntimeException("Config not found");
            }
        });
    }

    public CompletableFuture<LoadedConfig> downloadConfig(String str) {
        return fetchConfig(str).thenApply(class376Var -> {
            try {
                return new LoadedConfig(class376Var, this.serializer.deserialize(class376Var.decodeData()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to deserialize config", e);
            }
        });
    }

    public void applyConfig(LoadedConfig class089Var) {
        try {
            this.serializer.applyConfig(class089Var);
            this.activeConfigRef.set(new ConfigReference(class089Var.details().id(), class089Var.details().name()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to apply config", e);
        }
    }

    public Translation errorMessage(Throwable th) {
        String strMethod007 = extractErrorMessage(th);
        return strMethod007 == null ? Lang.CLOUD_ERROR_GENERIC : mapErrorMessage(strMethod007);
    }

    public String extractErrorMessage(Throwable th) {
        if (th.getMessage() != null) {
            return th.getMessage();
        }
        if (th.getCause() != null) {
            return th.getCause().getMessage();
        }
        return null;
    }

    public Translation mapErrorMessage(String str) {
        if (str.contains("name already exists") || str.contains("name already named")) {
            return Lang.CLOUD_ERROR_NAME_EXISTS;
        }
        if (str.contains("not found")) {
            return Lang.CLOUD_ERROR_NOT_FOUND;
        }
        if (str.contains("already in user list")) {
            return Lang.CLOUD_ERROR_ALREADY_LISTED;
        }
        if (str.contains("unauthorized")) {
            return Lang.CLOUD_ERROR_UNAUTHORIZED;
        }
        if (str.contains("Failed to create")) {
            return Lang.CLOUD_ERROR_CREATE_FAILED;
        }
        return str.contains("Failed to get") ? Lang.CLOUD_ERROR_GET_FAILED : Lang.CLOUD_ERROR_GENERIC;
    }

    public String serializeToBase64(ModuleRepository class795Var, WidgetStack class814Var) {
        try {
            return Base64.getEncoder().encodeToString(this.serializer.serialize(class795Var, class814Var));
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize modules config", e);
        }
    }

    public String currentLogin() {
        AccountProfile class013Var = this.accountProfile;
        return class013Var == null ? "" : class013Var.login();
    }

    public void ensureConfigDir() {
        try {
            Files.createDirectories(this.configDir, new FileAttribute[0]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory", e);
        }
    }

    public Path pathFor(String str) {
        return this.configDir.resolve(str + ".json");
    }

    public CloudConfigDto readDto(Path path) {
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        }
        try {
            String strRead = Files.readString(path, StandardCharsets.UTF_8);
            return (CloudConfigDto) this.gson.fromJson(strRead, CloudConfigDto.class);
        } catch (IOException e) {
            return null;
        }
    }

    public void writeDto(CloudConfigDto class376Var) {
        ensureConfigDir();
        byte[] bytes = this.gson.toJson(class376Var).getBytes(StandardCharsets.UTF_8);
        try {
            AtomicFileWriter.writeBytes(pathFor(class376Var.id()), bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config", e);
        }
    }

    public boolean nameExists(String str) {
        try (Stream<Path> streamList = Files.list(this.configDir)) {
            return streamList.filter(path -> {
                return path.getFileName().toString().endsWith(".json");
            }).map(this::readDto).filter(class376Var -> {
                return class376Var != null;
            }).anyMatch(class376Var2 -> {
                return str.equals(class376Var2.name());
            });
        } catch (IOException e) {
            return false;
        }
    }

    public static <T> CompletableFuture<T> toFuture(Call<T> call) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        call.enqueue(new FutureCallback(completableFuture));
        return completableFuture;
    }

    public static CompletableFuture<Void> toVoidFuture(Call<Void> call) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        call.enqueue(new VoidFutureCallback(completableFuture));
        return completableFuture;
    }

    public static RuntimeException httpError(Response<?> response) {
        String strString;
        try {
            strString = response.errorBody() != null ? response.errorBody().string() : "";
        } catch (IOException e) {
            strString = "";
        }
        return new RuntimeException("HTTP " + response.code() + ": " + strString);
    }
}
