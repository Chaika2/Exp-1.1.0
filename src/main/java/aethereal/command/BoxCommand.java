package aethereal.command;
import aethereal.util.BoxEspManager;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Lang;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BoxCommand implements ClientCommand {
    public static List<String> blockNameCache;
    public static List<String> entityNameCache;
    public static final List<String> defaultColors = List.of("FF0000", "00FF00", "0000FF", "FFFF00", "FF00FF", "00FFFF", "FFFFFF", "FFA500", "FF4500", "9400D3");

    public static List<String> getBlockNames() {
        if (blockNameCache == null) {
            blockNameCache = Registries.BLOCK.stream().filter(block -> {
                return block != Blocks.AIR;
            }).map(block2 -> {
                return Registries.BLOCK.getId(block2).getPath();
            }).sorted().toList();
        }
        return blockNameCache;
    }

    public static List<String> getEntityNames() {
        if (entityNameCache == null) {
            entityNameCache = Registries.ENTITY_TYPE.stream().map(entityType -> {
                return Registries.ENTITY_TYPE.getId(entityType).getPath();
            }).sorted().toList();
        }
        return entityNameCache;
    }

    @Override
    public String getName() {
        return "box";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_BOX_DESC;
    }

    @Override
    public String getUsage() {
        return ".box <add|remove|list|clear> <block|entity> <name> [#AARRGGBB]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("boxesp");
    }

    @Override
    public void execute(CommandContext class392Var) {
        String[] strArrArgs = class392Var.args();
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_SUBCOMMAND_REQUIRED.effective().replace("{usage}", getUsage())));
        }
        BoxEspManager class204Var = BoxEspManager.INSTANCE;
        switch (strArrArgs[0].toLowerCase()) {
            case "add":
                handleAdd(class204Var, strArrArgs);
                return;
            case "remove":
                handleRemove(class204Var, strArrArgs);
                return;
            case "list":
                handleList(class204Var);
                return;
            case "clear":
                handleClear(class204Var);
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_UNKNOWN_SUBCOMMAND.effective().replace("{sub}", strArrArgs[0])));
        }
    }

    public void handleAdd(BoxEspManager class204Var, String[] strArr) {
        requireArgs(strArr, 3);
        String str = strArr[2];
        int iMethod007 = strArr.length >= 4 ? parseColor(strArr[3]) : 0;
        switch (strArr[1].toLowerCase()) {
            case "block":
                Block blockOrElseThrow = resolveBlock(str).orElseThrow(() -> {
                    return notFoundException(Lang.COMMAND_BOX_BLOCK_NOT_FOUND.effective(), str);
                });
                class204Var.getBlocks().put(blockOrElseThrow, Integer.valueOf(iMethod007));
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_ADDED.effective().replace("{type}", String.valueOf(Formatting.RED) + "block" + String.valueOf(Formatting.GRAY)).replace("{name}", String.valueOf(Formatting.RED) + blockOrElseThrow.getName().getString() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
                return;
            case "entity":
                EntityType<?> entityTypeOrElseThrow = resolveEntity(str).orElseThrow(() -> {
                    return notFoundException(Lang.COMMAND_BOX_ENTITY_NOT_FOUND.effective(), str);
                });
                class204Var.getEntities().put(entityTypeOrElseThrow, Integer.valueOf(iMethod007));
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_ADDED.effective().replace("{type}", String.valueOf(Formatting.RED) + "entity" + String.valueOf(Formatting.GRAY)).replace("{name}", String.valueOf(Formatting.RED) + entityTypeOrElseThrow.getName().getString() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_BOX_INVALID_TYPE.effective()));
        }
    }

    public void handleRemove(BoxEspManager class204Var, String[] strArr) {
        requireArgs(strArr, 3);
        String str = strArr[2];
        switch (strArr[1].toLowerCase()) {
            case "block":
                Block blockOrElseThrow = resolveBlock(str).orElseThrow(() -> {
                    return notFoundException(Lang.COMMAND_BOX_BLOCK_NOT_FOUND.effective(), str);
                });
                if (class204Var.getBlocks().remove(blockOrElseThrow) == null) {
                    throw new TranslatedException(Translation.clearText(Lang.COMMAND_BOX_NOT_TRACKED.effective().replace("{name}", blockOrElseThrow.getName().getString())));
                }
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_REMOVED.effective().replace("{name}", String.valueOf(Formatting.RED) + blockOrElseThrow.getName().getString() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
                return;
            case "entity":
                EntityType<?> entityTypeOrElseThrow = resolveEntity(str).orElseThrow(() -> {
                    return notFoundException(Lang.COMMAND_BOX_ENTITY_NOT_FOUND.effective(), str);
                });
                if (class204Var.getEntities().remove(entityTypeOrElseThrow) == null) {
                    throw new TranslatedException(Translation.clearText(Lang.COMMAND_BOX_NOT_TRACKED.effective().replace("{name}", entityTypeOrElseThrow.getName().getString())));
                }
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_REMOVED.effective().replace("{name}", String.valueOf(Formatting.RED) + entityTypeOrElseThrow.getName().getString() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_BOX_INVALID_TYPE.effective()));
        }
    }

    public void handleList(BoxEspManager class204Var) {
        if (class204Var.getBlocks().isEmpty() && class204Var.getEntities().isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_LIST_EMPTY.effective()).formatted(Formatting.GRAY));
            return;
        }
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_LIST_HEADER.effective()).formatted(Formatting.GRAY));
        class204Var.getBlocks().forEach((block, num) -> {
            ChatUtil.addChatMessage((Text) Text.literal("  " + String.valueOf(Formatting.YELLOW) + "block " + String.valueOf(Formatting.RED) + block.getName().getString() + (num.intValue() != 0 ? String.valueOf(Formatting.GRAY) + " #" + String.format("%08X", num) : "")));
        });
        class204Var.getEntities().forEach((entityType, num2) -> {
            ChatUtil.addChatMessage((Text) Text.literal("  " + String.valueOf(Formatting.YELLOW) + "entity " + String.valueOf(Formatting.RED) + entityType.getName().getString() + (num2.intValue() != 0 ? String.valueOf(Formatting.GRAY) + " #" + String.format("%08X", num2) : "")));
        });
    }

    public void handleClear(BoxEspManager class204Var) {
        class204Var.clearAll();
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BOX_CLEARED.effective()).formatted(Formatting.GRAY));
    }

    public static Optional<Block> resolveBlock(String str) {
        Block block;
        Identifier identifierTryParse = Identifier.tryParse(str.contains(":") ? str : "minecraft:" + str);
        if (identifierTryParse != null && Registries.BLOCK.containsId(identifierTryParse) && (block = (Block) Registries.BLOCK.get(identifierTryParse)) != Blocks.AIR) {
            return Optional.of(block);
        }
        String strReplace = str.toLowerCase().replace("_", " ");
        return Registries.BLOCK.stream().filter(block2 -> {
            return block2 != Blocks.AIR;
        }).filter(block3 -> {
            String lowerCase = block3.getName().getString().toLowerCase();
            return lowerCase.equals(strReplace) || lowerCase.replace(" ", "_").equals(str.toLowerCase());
        }).findFirst();
    }

    public static Optional<EntityType<?>> resolveEntity(String str) {
        Identifier identifierTryParse = Identifier.tryParse(str.contains(":") ? str : "minecraft:" + str);
        if (identifierTryParse != null && Registries.ENTITY_TYPE.containsId(identifierTryParse)) {
            return Optional.of((EntityType) Registries.ENTITY_TYPE.get(identifierTryParse));
        }
        String strReplace = str.toLowerCase().replace("_", " ");
        return Registries.ENTITY_TYPE.stream().filter(entityType -> {
            String lowerCase = entityType.getName().getString().toLowerCase();
            return lowerCase.equals(strReplace) || lowerCase.replace(" ", "_").equals(str.toLowerCase());
        }).findFirst();
    }

    public static int parseColor(String str) throws TranslatedException {
        try {
            String strSubstring = str.startsWith("#") ? str.substring(1) : str;
            long j = Long.parseLong(strSubstring, 16);
            if (strSubstring.length() <= 6) {
                j |= 4278190080L;
            }
            return (int) j;
        } catch (NumberFormatException e) {
            throw new TranslatedException(Translation.clearText("Invalid color '" + str + "'. Use hex like FF0000 or FFFF0000"));
        }
    }

    public static List<String> filterSuggestions(List<String> list, String str, int i) {
        if (str.isEmpty()) {
            return list.size() <= i ? list : list.subList(0, i);
        }
        int i2 = 0;
        int size = list.size();
        while (i2 < size) {
            int i3 = (i2 + size) >>> 1;
            if (list.get(i3).compareTo(str) < 0) {
                i2 = i3 + 1;
            } else {
                size = i3;
            }
        }
        ArrayList arrayList = new ArrayList();
        for (int i4 = i2; i4 < list.size() && arrayList.size() < i; i4++) {
            String str2 = list.get(i4);
            if (!str2.startsWith(str)) {
                break;
            }
            arrayList.add(str2);
        }
        return arrayList;
    }

    public static TranslatedException notFoundException(String str, String str2) {
        return new TranslatedException(Translation.clearText(str.replace("{name}", str2)));
    }

    public static void requireArgs(String[] strArr, int i) throws TranslatedException {
        if (strArr.length < i) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_ENOUGH_ARGS.effective()));
        }
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        String lowerCase = i < strArr.length ? strArr[i].toLowerCase() : "";
        String lowerCase2 = strArr.length > 0 ? strArr[0].toLowerCase() : "";
        if (i == 0) {
            return Stream.of(new String[]{"add", "remove", "list", "clear"}).filter(str -> {
                return str.startsWith(lowerCase);
            }).toList();
        }
        if (i == 1 && (lowerCase2.equals("add") || lowerCase2.equals("remove"))) {
            return Stream.of(new String[]{"block", "entity"}).filter(str2 -> {
                return str2.startsWith(lowerCase);
            }).toList();
        }
        String lowerCase3 = strArr.length > 1 ? strArr[1].toLowerCase() : "";
        if (i == 2) {
            if (lowerCase2.equals("add")) {
                if (lowerCase3.equals("block")) {
                    return filterSuggestions(getBlockNames(), lowerCase, 30);
                }
                if (lowerCase3.equals("entity")) {
                    return filterSuggestions(getEntityNames(), lowerCase, 30);
                }
            }
            if (lowerCase2.equals("remove")) {
                BoxEspManager class204Var = BoxEspManager.INSTANCE;
                if (lowerCase3.equals("block")) {
                    return class204Var.getBlocks().keySet().stream().map(block -> {
                        return Registries.BLOCK.getId(block).getPath();
                    }).filter(str3 -> {
                        return str3.startsWith(lowerCase);
                    }).sorted().toList();
                }
                if (lowerCase3.equals("entity")) {
                    return class204Var.getEntities().keySet().stream().map(entityType -> {
                        return Registries.ENTITY_TYPE.getId(entityType).getPath();
                    }).filter(str4 -> {
                        return str4.startsWith(lowerCase);
                    }).sorted().toList();
                }
            }
        }
        return (i == 3 && lowerCase2.equals("add")) ? defaultColors.stream().filter(str5 -> {
            return str5.toLowerCase().startsWith(lowerCase);
        }).toList() : List.of();
    }
}
