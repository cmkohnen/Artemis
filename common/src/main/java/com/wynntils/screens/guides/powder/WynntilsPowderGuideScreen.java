/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.PowderProfile;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public final class WynntilsPowderGuideScreen
        extends WynntilsListScreen<GuidePowderItemStack, GuidePowderItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuidePowderItemStack> parsedItemCache;

    private WynntilsPowderGuideScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.powder.name"));
    }

    public static Screen create() {
        return new WynntilsPowderGuideScreen();
    }

    @Override
    protected void doInit() {
        if (parsedItemCache == null) {
            parsedItemCache = PowderProfile.getAllPowderProfiles().stream()
                    .map(GuidePowderItemStack::new)
                    .toList();
        }

        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.powder.name"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hovered instanceof GuidePowderItemStackButton guidePowderItemStack) {
            GuidePowderItemStack itemStack = guidePowderItemStack.getItemStack();

            List<Component> tooltipLines = itemStack.getTooltipLines(McUtils.player(), TooltipFlag.NORMAL);
            tooltipLines.add(Component.empty());
            if (Models.Favorites.isFavorite(itemStack)) {
                tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                        .withStyle(ChatFormatting.GREEN));
            }

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    ComponentUtils.wrapTooltips(tooltipLines, 200),
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available"),
                        Texture.QUEST_BOOK_BACKGROUND.width() * 0.75f,
                        30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top,
                        TextShadow.NONE);
    }

    @Override
    protected GuidePowderItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuidePowderItemStackButton(
                xOffset + Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 13, yOffset + 43, 18, 18, elements.get(i), this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(parsedItemCache.stream()
                .filter(itemStack ->
                        StringUtils.partialMatch(ComponentUtils.getUnformatted(itemStack.getHoverName()), searchTerm))
                .toList());
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}