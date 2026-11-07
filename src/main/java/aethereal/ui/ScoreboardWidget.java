package aethereal.ui;
import aethereal.Lang;
import aethereal.model.DragRenderContext;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemePalette;
import aethereal.type.Mc;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.SeparatorSetting;
import aethereal.util.DrawEngine;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

public class ScoreboardWidget extends Draggable {
    static final float cornerRadius = 8.0f;
    static final float headerHeight = 23.0f;
    static final float rowHeight = 11.0f;
    static final float innerPadding = 8.0f;
    static final float numberSeparatorWidth = 8.0f;
    static final int headerFontSize = 11;
    static final int rowFontSize = 9;

    public final MsdfFont headerFont;
    public final MsdfFont rowFont;
    public float layoutWidth;
    public float layoutHeight;
    public boolean pinned;

    public final NumberSetting scale;
    public final BooleanSetting showNumbers;
    public final NumberSetting maxRows;
    public final BooleanSetting useServerColors;
    public final ColorSetting nameColor;
    public final ColorSetting numberColor;
    public final ColorSetting headerColor;
    public final ColorSetting backgroundColor;

    public ScoreboardWidget(BooleanSupplier booleanSupplier) {
        super("Scoreboard", booleanSupplier);
        this.headerFont = Fonts.INTER_EXTRA_BOLD.get();
        this.rowFont = Fonts.INTER_SEMIBOLD.get();
        this.layoutWidth = 0.0f;
        this.layoutHeight = 0.0f;
        this.pinned = true;
        this.x = 240.0f;
        this.y = 10.0f;

        this.scale = new NumberSetting(Lang.WIDGET_SCOREBOARD_SCALE)
                .currentValue(1.0f)
                .range(0.5f, 2.0f)
                .step(0.05f);
        this.showNumbers = new BooleanSetting(Lang.WIDGET_SCOREBOARD_SHOW_NUMBERS).setValue(true);
        this.maxRows = new NumberSetting(Lang.WIDGET_SCOREBOARD_MAX_ROWS)
                .currentValue(15.0f)
                .range(5.0f, 30.0f)
                .step(1.0f);
        this.useServerColors = new BooleanSetting(Lang.WIDGET_SCOREBOARD_USE_SERVER_COLORS).setValue(true);
        this.nameColor = new ColorSetting(Lang.WIDGET_SCOREBOARD_NAME_COLOR)
                .value(0xFFFFFFFF)
                .visible(() -> Boolean.valueOf(!this.useServerColors.isValue()));
        this.numberColor = new ColorSetting(Lang.WIDGET_SCOREBOARD_NUMBER_COLOR)
                .value(0xA56CFF)
                .visible(() -> Boolean.valueOf(!this.useServerColors.isValue() && this.showNumbers.isValue()));
        this.headerColor = new ColorSetting(Lang.WIDGET_SCOREBOARD_HEADER_COLOR)
                .value(0xA56CFF)
                .visible(() -> Boolean.valueOf(!this.useServerColors.isValue()));
        this.backgroundColor = new ColorSetting(Lang.WIDGET_SCOREBOARD_BACKGROUND_COLOR).value(0xE616191D);

        addSettings(this.scale, this.showNumbers, this.maxRows, this.useServerColors, new SeparatorSetting(), this.backgroundColor, this.headerColor, this.nameColor, this.numberColor);
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        if (!isVisible()) return;
        ScoreboardObjective objective = getObjective();
        if (objective == null) {
            this.layoutWidth = 0.0f;
            this.layoutHeight = 0.0f;
            return;
        }
        List<ScoreboardLine> lines = collectLines(objective, Math.round(this.maxRows.currentValue()));
        float maxTextWidth = this.headerFont.getWidthWithStyles(objective.getDisplayName(), headerFontSize);
        float maxFullWidth = 0.0f;
        for (ScoreboardLine line : lines) {
            float fNameWidth = this.rowFont.getWidthWithStyles(line.name(), rowFontSize);
            float fNumberWidth = this.showNumbers.isValue() && !line.number().getString().isEmpty() ? this.rowFont.getWidthWithStyles(line.number(), rowFontSize) : 0.0f;
            maxTextWidth = Math.max(maxTextWidth, fNameWidth);
            maxFullWidth = Math.max(maxFullWidth, fNameWidth + (fNumberWidth > 0.0f ? numberSeparatorWidth + fNumberWidth : 0.0f));
        }
        float fContentWidth = Math.max(maxTextWidth, maxFullWidth);
        this.layoutWidth = fContentWidth + (innerPadding * 2.0f);
        this.layoutHeight = headerHeight + ((float) lines.size() * rowHeight) + innerPadding;
        ScreenResolution res = class809Var.resolution();
        if (this.pinned && res != null) {
            this.x = res.screenWidth() - width() - innerPadding;
            this.y = innerPadding;
        }
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (!isVisible()) return;
        ScoreboardObjective objective = getObjective();
        if (objective == null) return;
        List<ScoreboardLine> lines = collectLines(objective, Math.round(this.maxRows.currentValue()));
        float fCurrentValue = this.scale.currentValue();
        float w = this.layoutWidth;
        float h = this.layoutHeight;
        if (w <= 0.0f || h <= 0.0f) return;
        boolean serverColors = this.useServerColors.isValue();

        DrawEngine engine = class809Var.drawEngine();
        MatrixStack matrixStack = class809Var.matrixStack();
        ColorStack colorStack = engine.colorStack();
        ThemePalette palette = class809Var.theme().palette();

        // Фон и обводка
        int iOutline = colorStack.computeColor(palette.surfaceOutline().tone(600).argb());
        int iBackground = colorStack.computeColor(this.backgroundColor.getColor());
        int iBackgroundAlt = colorStack.computeColor(this.backgroundColor.getColor(), 0.9f);

        matrixStack.push();
        matrixStack.translate(this.x, this.y, 0.0f);
        matrixStack.scale(fCurrentValue, fCurrentValue, 1.0f);

        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, w, h, cornerRadius, 2.5f, iOutline, iBackgroundAlt, iBackgroundAlt, iBackground, iBackground);
        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), 1.0f, 1.0f, w - 2.0f, headerHeight - 1.0f, 7.0f, colorStack.computeColor(palette.accent().argb(), 0.16f));
        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), 1.0f, 1.0f, w - 2.0f, headerHeight - 7.0f, 7.0f, colorStack.computeColor(palette.accent().argb(), 0.10f));

        // Заголовок
        Text textDisplay = objective.getDisplayName();
        float fHeaderY = (headerHeight - this.headerFont.getHeight(headerFontSize)) / 2.0f;
        if (serverColors) {
            float fHeaderTextWidth = this.headerFont.getWidthWithStyles(textDisplay, headerFontSize);
            engine.msdfText(matrixStack.peek().getPositionMatrix(), this.headerFont, textDisplay, (w - fHeaderTextWidth) / 2.0f, fHeaderY, headerFontSize, colorStack.computeColor(palette.text().tone(200).argb()));
        } else {
            String strHeader = StringHelper.stripTextFormat(textDisplay.getString());
            float fHeaderTextWidth = this.headerFont.getWidth(strHeader, headerFontSize);
            engine.msdfFont(matrixStack.peek().getPositionMatrix(), this.headerFont, strHeader, (w - fHeaderTextWidth) / 2.0f, fHeaderY, headerFontSize, 0.0f, colorStack.computeColor(this.headerColor.getColor()));
        }

        // Строки
        float fRowY = headerHeight + 3.0f;
        float fRight = w - innerPadding;
        int iNameColor = colorStack.computeColor(serverColors ? palette.text().tone(400).argb() : this.nameColor.getColor());
        int iNumberColor = colorStack.computeColor(serverColors ? palette.accent().argb() : this.numberColor.getColor());
        for (ScoreboardLine line : lines) {
            if (fRowY + rowHeight > h) break;
            float fCenterY = fRowY - (this.rowFont.getHeight(rowFontSize) / 2.0f);
            if (serverColors) {
                engine.msdfText(matrixStack.peek().getPositionMatrix(), this.rowFont, line.name(), innerPadding, fCenterY, rowFontSize, iNameColor);
            } else {
                engine.msdfFont(matrixStack.peek().getPositionMatrix(), this.rowFont, StringHelper.stripTextFormat(line.name().getString()), innerPadding, fCenterY, rowFontSize, 0.0f, iNameColor);
            }
            if (this.showNumbers.isValue() && !line.number().getString().isEmpty()) {
                float fNumberWidth = this.rowFont.getWidthWithStyles(line.number(), rowFontSize);
                if (serverColors) {
                    engine.msdfText(matrixStack.peek().getPositionMatrix(), this.rowFont, line.number(), fRight - fNumberWidth, fCenterY, rowFontSize, iNumberColor);
                } else {
                    String strNumber = StringHelper.stripTextFormat(line.number().getString());
                    float fStrippedNumberWidth = this.rowFont.getWidth(strNumber, rowFontSize);
                    engine.msdfFont(matrixStack.peek().getPositionMatrix(), this.rowFont, strNumber, fRight - fStrippedNumberWidth, fCenterY, rowFontSize, 0.0f, iNumberColor);
                }
            }
            fRowY += rowHeight;
        }
        matrixStack.pop();
    }

    @Override
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        if (class807Var.isLeftButtonPressed() && class807Var.press() && z && !class807Var.intercepted()) {
            this.pinned = false;
        }
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        return z && !class808Var.intercepted();
    }

    @Override
    public void animate(WeightedEngine class141Var) {
    }

    @Override
    public void update() {
    }

    @Override
    public float width() {
        return this.layoutWidth * this.scale.currentValue();
    }

    @Override
    public float height() {
        return this.layoutHeight * this.scale.currentValue();
    }

    public Scoreboard getScoreboard() {
        if (!Mc.INSTANCE.isWorldLoaded() || Mc.INSTANCE.getWorld().getScoreboard() == null) {
            return null;
        }
        return Mc.INSTANCE.getWorld().getScoreboard();
    }

    public ScoreboardObjective getObjective() {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard == null || Mc.INSTANCE.getPlayer() == null) {
            return null;
        }
        Team team = scoreboard.getScoreHolderTeam(Mc.INSTANCE.getPlayer().getNameForScoreboard());
        ScoreboardDisplaySlot slot = team != null ? ScoreboardDisplaySlot.fromFormatting(team.getColor()) : null;
        ScoreboardObjective objective = slot != null ? scoreboard.getObjectiveForSlot(slot) : null;
        if (objective == null) {
            objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        }
        return objective;
    }

    public List<ScoreboardLine> collectLines(ScoreboardObjective objective, int iMaxRows) {
        ArrayList<ScoreboardLine> list = new ArrayList();
        Scoreboard scoreboard = objective.getScoreboard();
        ArrayList<ScoreboardEntry> entries = new ArrayList();
        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
            if (!entry.hidden()) {
                entries.add(entry);
            }
        }
        entries.sort((entry1, entry2) -> {
            return Integer.compare(entry2.value(), entry1.value());
        });
        int count = Math.min(iMaxRows, entries.size());
        for (int i = 0; i < count; i++) {
            ScoreboardEntry entry = entries.get(i);
            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            Text decorated = Team.decorateName(team, entry.name());
            Text number = entry.formatted(objective.getNumberFormatOr(StyledNumberFormat.RED));
            list.add(new ScoreboardLine(decorated, number));
        }
        return list;
    }

    @Override
    public boolean hasTooltip() {
        return false;
    }

    public static final class ScoreboardLine {
        final Text name;
        final Text number;

        ScoreboardLine(Text text, Text text2) {
            this.name = text;
            this.number = text2;
        }

        public Text name() {
            return this.name;
        }

        public Text number() {
            return this.number;
        }
    }
}