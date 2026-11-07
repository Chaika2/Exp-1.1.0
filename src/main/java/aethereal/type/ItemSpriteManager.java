package aethereal.type;
import aethereal.accessor.ArmorItemAccessor;
import aethereal.Expensive;
import aethereal.render.GlTexture;
import aethereal.util.GrimDelayHandler;
import aethereal.resource.IdentifierResource;
import aethereal.render.ItemSpriteTextures;
import aethereal.event.PlayerInitEvent;
import aethereal.model.SpriteRegion;
import aethereal.util.StaffDetector;
import aethereal.resource.TextureResource;
import aethereal.model.UvBounds;
import aethereal.event.WorldLoadEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public enum ItemSpriteManager {
    INSTANCE;

    public final UvBounds defaultFaceUv = new UvBounds(0.125f, 0.125f, 0.25f, 0.25f);
    public final Map<Object, GlTexture> textureCache = new HashMap();
    public final Map<ItemStack, ItemSpriteTextures> spriteCache = new HashMap();

    public final ConcurrentMap<GameProfile, Identifier> skinCache = new ConcurrentHashMap();
    public final Set<GameProfile> pendingProfiles = ConcurrentHashMap.newKeySet();
    public final Map<Item, int[]> tintCache = new HashMap();
    public Map<Identifier, Resource> itemResources = null;

    ItemSpriteManager() {
        Expensive.INSTANCE.eventDispatcher().register(WorldLoadEvent.class, class086Var -> {
            clearCaches();
            StaffDetector.clearCaches();
            StaffDetector.clearStaffMemory();
            GrimDelayHandler.onWorldChange();
        });
        Expensive.INSTANCE.eventDispatcher().register(PlayerInitEvent.class, class125Var -> {
            if (Mc.INSTANCE.isWorldLoaded()) {
                clearCaches();
                StaffDetector.clearCaches();
                GrimDelayHandler.onWorldChange();
            }
        });
    }

    public boolean canRender(ItemStack itemStack) {
        ItemSpriteTextures spriteTextures;
        return (itemStack == null || itemStack.isEmpty() || (spriteTextures = getSpriteTextures(itemStack)) == null || spriteTextures.sprites() == null || spriteTextures.sprites().isEmpty() || itemStack.getItem() == Items.AIR) ? false : true;
    }

    public UvBounds getFaceUV(LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
        if (livingEntityRenderer == null) {
            return this.defaultFaceUv;
        }
        EntityModel model = livingEntityRenderer.getModel();
        if (model instanceof EntityModel) {
            Iterator it = model.getParts().iterator();
            while (it.hasNext()) {
                UvBounds class222VarMethod014 = computePartFaceUv((ModelPart) it.next());
                if (class222VarMethod014 != null) {
                    return class222VarMethod014;
                }
            }
        }
        return this.defaultFaceUv;
    }

    public GlTexture getOrCreateTexture(Object obj) {
        return this.textureCache.computeIfAbsent(obj, obj2 -> {
            if (obj2 instanceof Sprite) {
                return new GlTexture(new IdentifierResource(((Sprite) obj2).getAtlasId()));
            }
            if (!(obj2 instanceof AbstractTexture)) {
                return null;
            }
            GlTexture class073Var = new GlTexture(new TextureResource((AbstractTexture) obj2));
            class073Var.minFilter(9728);
            class073Var.magFilter(9728);
            return class073Var;
        });
    }

    static int typeSwitch00421a(Object o) {
        if (o == null) return -1;
        if (o instanceof TippedArrowItem) return 0;
        if (o instanceof PotionItem) return 1;
        if (o instanceof ShieldItem) return 2;
        if (o instanceof SpawnEggItem) return 3;
        if (o instanceof PlayerHeadItem) return 4;
        if (o instanceof ArmorItemAccessor) return 5;
        if (o instanceof BlockItem) return 6;
        return 7;
    }

    static int typeSwitch00421b(Object o) {
        if (o == null) return -1;
        if (o instanceof ShulkerBoxBlock) return 0;
        return 1;
    }

    public ItemSpriteTextures getSpriteTextures(ItemStack itemStack) {
        return this.spriteCache.computeIfAbsent(itemStack, itemStack2 -> {
            Sprite particleSprite;
            String str;
            BakedModel bakedModelMethod011 = getBakedModel(itemStack);
            if (bakedModelMethod011 == null || (particleSprite = bakedModelMethod011.getParticleSprite()) == null) {
                return null;
            }
            boolean z = itemStack.hasGlint() || !((ItemEnchantmentsComponent) itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)).isEmpty();
            int color = DyedColorComponent.getColor(itemStack, -1);
            Item item = itemStack.getItem();
            Objects.requireNonNull(item);
            switch (typeSwitch00421a(item)) {
                case 0:
                    return new ItemSpriteTextures("Default", Map.of(Direction.UP, SpriteRegion.of(getBakedModel(new ItemStack(Items.ARROW)).getParticleSprite(), -1), Direction.DOWN, SpriteRegion.of(particleSprite, ((PotionContentsComponent) itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)).getColor())), false);
                case 1:
                    Function spriteAtlas = Mc.INSTANCE.getMinecraft().getSpriteAtlas(particleSprite.getAtlasId());
                    if (item instanceof LingeringPotionItem) {
                        str = "lingering_";
                    } else {
                        str = item instanceof SplashPotionItem ? "splash_" : "";
                    }
                    return new ItemSpriteTextures("Default", Map.of(Direction.UP, SpriteRegion.of((Sprite) spriteAtlas.apply(Identifier.ofVanilla("item/" + str + "potion")), -1), Direction.DOWN, SpriteRegion.of(particleSprite, ((PotionContentsComponent) itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)).getColor())), false);
                case 2:
                    Sprite sprite = (Sprite) Mc.INSTANCE.getMinecraft().getSpriteAtlas(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE).apply(Identifier.ofVanilla("entity/shield_base_nopattern"));
                    return new ItemSpriteTextures("Shield", Map.of(Direction.UP, new SpriteRegion(sprite.getAtlasId(), sprite.getMinU() + 0.0025f, sprite.getMinV() + 0.0025f, sprite.getMaxU() - 0.1f, sprite.getMaxV() - 0.08f, -1)), z);
                case 3:
                    int[] iArrComputeIfAbsent = this.tintCache.computeIfAbsent(item, item2 -> {
                        try {
                            JsonElement jsonElement = JsonParser.parseReader(getItemResources().get(Identifier.of("items/" + Registries.ITEM.getId(item2).getPath() + ".json")).getReader()).getAsJsonObject().get("model").getAsJsonObject().get("tints");
                            return new int[]{jsonElement.getAsJsonArray().get(1).getAsJsonObject().get("value").getAsInt(), jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsInt()};
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return new ItemSpriteTextures("Default", Map.of(Direction.DOWN, SpriteRegion.of((Sprite) Mc.INSTANCE.getMinecraft().getSpriteAtlas(particleSprite.getAtlasId()).apply(Identifier.ofVanilla("item/spawn_egg_overlay")), iArrComputeIfAbsent[0]), Direction.UP, SpriteRegion.of(particleSprite, iArrComputeIfAbsent[1])), z);
                case 4:
                    ProfileComponent profileComponent = (ProfileComponent) itemStack.get(DataComponentTypes.PROFILE);
                    HashMap map = new HashMap();
                    SkinTextures steve = profileComponent == null ? DefaultSkinHelper.getSteve() : (SkinTextures) ((Optional) Mc.INSTANCE.getSkinProvider().fetchSkinTextures(profileComponent.gameProfile()).getNow(Optional.empty())).orElse(null);
                    if (steve == null) {
                        return null;
                    }
                    Identifier identifierTexture = steve.texture();
                    map.put(Direction.UP, SpriteRegion.of(identifierTexture, 0.125f, 0.0f, 0.25f, 0.125f, color));
                    map.put(Direction.WEST, SpriteRegion.of(identifierTexture, 0.0f, 0.125f, 0.125f, 0.25f, color));
                    map.put(Direction.EAST, SpriteRegion.of(identifierTexture, 0.125f, 0.125f, 0.25f, 0.25f, color));
                    return new ItemSpriteTextures("3D", map, z);
                case 5:
                    ArmorItemAccessor class327Var = (ArmorItemAccessor) item;
                    if (class327Var.expensive_ru$getMaterial().repairIngredient().id().getPath().equals("repairs_leather_armor")) {
                        return new ItemSpriteTextures("Default", Map.of(Direction.DOWN, SpriteRegion.of((Sprite) Mc.INSTANCE.getMinecraft().getSpriteAtlas(particleSprite.getAtlasId()).apply(Identifier.ofVanilla("item/leather_" + class327Var.expensive_ru$getType().getName() + "_overlay")), -1), Direction.UP, SpriteRegion.of(particleSprite, color)), z);
                    }
                    ArmorTrim armorTrim = (ArmorTrim) itemStack.get(DataComponentTypes.TRIM);
                    if (armorTrim != null) {
                        return new ItemSpriteTextures("Default", Map.of(Direction.DOWN, SpriteRegion.of(particleSprite, -1), Direction.UP, SpriteRegion.of(Mc.INSTANCE.getMinecraft().getBakedModelManager().getAtlas(particleSprite.getAtlasId()).getSprite(Identifier.ofVanilla("trims/items/" + class327Var.expensive_ru$getType().getName() + "_trim_" + ((ArmorTrimMaterial) armorTrim.material().value()).assetName())), -1)), z);
                    }
                    break;
                case 6:
                    BlockItem blockItem = (BlockItem) item;
                    Block block = blockItem.getBlock();
                    Objects.requireNonNull(block);
                    switch (typeSwitch00421b(block)) {
                        case 0:
                            return ItemSpriteTextures.of("3D", Map.of(Direction.UP, particleSprite, Direction.WEST, particleSprite, Direction.EAST, particleSprite), z, color);
                        default:
                            return ItemSpriteTextures.of("Default", Map.of(Direction.DOWN, particleSprite), z, color);
                    }
            }
            return ItemSpriteTextures.of("Default", Map.of(Direction.DOWN, particleSprite), z, color);
        });
    }

    public Map<Identifier, Resource> getItemResources() {
        if (this.itemResources == null) {
            this.itemResources = ResourceFinder.json("items").findResources(Mc.INSTANCE.getResourceManager());
        }
        return this.itemResources;
    }

    public BakedModel getBakedModel(ItemStack itemStack) {
        ItemRenderState itemRenderState = new ItemRenderState();
        Mc.INSTANCE.getItemModelManager().update(itemRenderState, itemStack, ModelTransformationMode.GUI, Mc.INSTANCE.getWorld(), (LivingEntity) null, 0);
        return getModelFromState(itemRenderState);
    }

    public BakedModel getModelFromState(ItemRenderState itemRenderState) {
        return (BakedModel) Arrays.stream(itemRenderState.layers).map(layerRenderState -> {
            return layerRenderState.model;
        }).filter((v0) -> {
            return Objects.nonNull(v0);
        }).findFirst().orElse(null);
    }

    public UvBounds computePartFaceUv(ModelPart modelPart) {
        UvBounds class222VarMethod004 = null;
        for (ModelPart.Cuboid cuboid : modelPart.cuboids) {
            class222VarMethod004 = pickLargerUv(class222VarMethod004, selectMedianUv(computeSideUv(cuboid, Direction.NORTH), computeSideUv(cuboid, Direction.SOUTH), computeSideUv(cuboid, Direction.WEST), computeSideUv(cuboid, Direction.EAST)));
        }
        return class222VarMethod004;
    }

    public UvBounds computeSideUv(ModelPart.Cuboid cuboid, Direction direction) {
        ModelPart.Quad quad;
        int id = direction.getId();
        if (id < 0 || id >= cuboid.sides.length || (quad = cuboid.sides[id]) == null || quad.vertices() == null || quad.vertices().length == 0) {
            return null;
        }
        float fMin = Float.POSITIVE_INFINITY;
        float fMin2 = Float.POSITIVE_INFINITY;
        float fMax = Float.NEGATIVE_INFINITY;
        float fMax2 = Float.NEGATIVE_INFINITY;
        for (ModelPart.Vertex vertex : quad.vertices()) {
            fMin = Math.min(fMin, vertex.u());
            fMin2 = Math.min(fMin2, vertex.v());
            fMax = Math.max(fMax, vertex.u());
            fMax2 = Math.max(fMax2, vertex.v());
        }
        if (fMin == Float.POSITIVE_INFINITY || fMin2 == Float.POSITIVE_INFINITY) {
            return null;
        }
        return new UvBounds(fMin, fMin2, fMax, fMax2);
    }

    public UvBounds selectMedianUv(UvBounds... class222VarArr) {
        ArrayList<UvBounds> arrayList = new ArrayList<>();
        for (UvBounds class222Var : class222VarArr) {
            if (class222Var != null) {
                arrayList.add(class222Var);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        if (arrayList.size() == 1) {
            return (UvBounds) arrayList.get(0);
        }
        arrayList.sort(Comparator.comparing((v0) -> {
            return v0.u0();
        }));
        return arrayList.size() >= 4 ? (UvBounds) arrayList.get(1) : (UvBounds) arrayList.get(arrayList.size() / 2);
    }

    public UvBounds pickLargerUv(UvBounds class222Var, UvBounds class222Var2) {
        if (class222Var2 == null) {
            return class222Var;
        }
        if (class222Var == null) {
            return class222Var2;
        }
        float fU1 = (class222Var.u1() - class222Var.u0()) * (class222Var.v1() - class222Var.v0());
        float fU2 = (class222Var2.u1() - class222Var2.u0()) * (class222Var2.v1() - class222Var2.v0());
        if (fU2 != fU1 && fU2 <= fU1) {
            return class222Var;
        }
        return class222Var2;
    }

    public Sprite getFirstParticleSprite(ItemRenderState itemRenderState) {
        if (itemRenderState.layerCount == 0 || itemRenderState.getFirstLayer().model == null) {
            return null;
        }
        return itemRenderState.getFirstLayer().model.getParticleSprite();
    }

    public void clearCaches() {
        this.textureCache.values().forEach((v0) -> {
            v0.reload();
        });
        this.textureCache.clear();
        this.spriteCache.clear();
        this.tintCache.clear();
        this.itemResources = null;
    }
}
