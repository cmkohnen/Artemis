/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import com.wynntils.wc.utils.scoreboard.objectives.WynnObjective;
import java.util.List;

public class ObjectivesOverlayFeature extends UserFeature {

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay guildObjectiveOverlay = new GuildObjectiveOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final Overlay dailyObjectiveOverlay = new DailyObjectiveOverlay();

    public static class GuildObjectiveOverlay extends ObjectiveOverlayBase {

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColour")
        public CustomColor textColor = CommonColors.LIGHT_BLUE;

        public GuildObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -5.5f,
                            -5.0f,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 30),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            WynnObjective guildObjective = ObjectiveManager.getGuildObjective();

            if (guildObjective == null) {
                return;
            }

            if (this.hideOnInactivity) {
                final int maxInactivityMs = 3000;
                if (guildObjective.getUpdatedAt() + maxInactivityMs < System.currentTimeMillis()) {
                    return;
                }
            }

            final float SPACE_BETWEEN = 10;

            final int barHeight = this.enableProgressBar ? 5 : 0;
            final int barWidth = 182;
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeight = 9 + actualBarHeight;

            float renderY = this.getRenderY()
                    + switch (this.getRenderVerticalAlignment()) {
                        case Top -> 0;
                        case Middle -> (this.getHeight() - renderedHeight) / 2f;
                        case Bottom -> this.getHeight() - renderedHeight;
                    };

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            guildObjective.getGoal() + ": " + guildObjective.getScore() + "/"
                                    + guildObjective.getMaxScore(),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor,
                            FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                            this.textShadow);

            if (this.enableProgressBar) {
                RenderUtils.drawProgressBar(
                        poseStack,
                        Texture.BUBBLE_BAR,
                        this.getRenderX(),
                        renderY + SPACE_BETWEEN,
                        this.getRenderX() + this.getWidth(),
                        renderY + SPACE_BETWEEN + actualBarHeight,
                        0,
                        objectivesTexture.yOffset,
                        barWidth,
                        objectivesTexture.yOffset + 10,
                        guildObjective.getProgress());
            }
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            render(poseStack, partialTicks, window);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public static class DailyObjectiveOverlay extends ObjectiveOverlayBase {

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textColour")
        public CustomColor textColor = CommonColors.GREEN;

        public DailyObjectiveOverlay() {
            super(
                    new OverlayPosition(
                            -35.5f,
                            -5.0f,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(150, 100),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Bottom);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            List<WynnObjective> objectives = ObjectiveManager.getObjectives();

            final float SPACE_BETWEEN = 10;

            final int barHeight = this.enableProgressBar ? 5 : 0;
            final int barWidth = 182;
            final float actualBarHeight = barHeight * (this.getWidth() / barWidth);
            final float renderedHeight = SPACE_BETWEEN + 9 + actualBarHeight;
            final float fullHeight = renderedHeight * objectives.size() - SPACE_BETWEEN;

            float offsetY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> 0;
                        case Middle -> (this.getHeight() - fullHeight) / 2f;
                        case Bottom -> this.getHeight() - fullHeight;
                    };

            for (WynnObjective objective : objectives) {
                if (this.hideOnInactivity) {
                    final int maxInactivityMs = 3000;
                    if (objective.getUpdatedAt() + maxInactivityMs < System.currentTimeMillis()) {
                        continue;
                    }
                }

                float renderY = offsetY + this.getRenderY();

                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                objective.getGoal() + ": " + objective.getScore() + "/" + objective.getMaxScore(),
                                this.getRenderX(),
                                this.getRenderX() + this.getWidth(),
                                renderY,
                                0,
                                this.textColor,
                                FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                                this.textShadow);

                if (this.enableProgressBar) {
                    RenderUtils.drawProgressBar(
                            poseStack,
                            Texture.EXPERIENCE_BAR,
                            this.getRenderX(),
                            renderY + SPACE_BETWEEN,
                            this.getRenderX() + this.getWidth(),
                            renderY + SPACE_BETWEEN + actualBarHeight,
                            0,
                            objectivesTexture.yOffset,
                            barWidth,
                            objectivesTexture.yOffset + 10,
                            objective.getProgress());
                }

                offsetY += renderedHeight;
            }
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            render(poseStack, partialTicks, window);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public abstract static class ObjectiveOverlayBase extends Overlay {

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
        public boolean hideOnInactivity = false;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
        public boolean enableProgressBar = true;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.objectivesTexture")
        public ObjectivesTextures objectivesTexture = ObjectivesTextures.a;

        @Config(key = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        public ObjectiveOverlayBase(
                OverlayPosition position,
                OverlaySize size,
                HorizontalAlignment horizontalAlignmentOverride,
                VerticalAlignment verticalAlignmentOverride) {
            super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
        }
    }

    public enum ObjectivesTextures {
        Wynn(0),
        Liquid(40),
        Emerald(50),
        a(10),
        b(20),
        c(30);

        private final int yOffset;

        ObjectivesTextures(int yOffset) {
            this.yOffset = yOffset;
        }

        public int getTextureYOffset() {
            return yOffset;
        }
    }
}