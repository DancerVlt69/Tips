package net.darkhax.tipsmod.api;

import net.darkhax.bookshelf.Constants;
import net.darkhax.tipsmod.api.resources.ITip;
import net.darkhax.tipsmod.api.resources.ITipSerializer;
import net.darkhax.tipsmod.impl.TipsModCommon;
import net.darkhax.tipsmod.impl.resources.SimpleTip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TipsAPI {

    public static final ResourceLocation DEFAULT_SERIALIZER = new ResourceLocation("tips", "simple_tip");
    public static final Component DEFAULT_TITLE = Component.translatable("tipsmod.title.default").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.YELLOW);
    public static final ITip EMPTY = new SimpleTip(new ResourceLocation(Constants.MOD_ID, "empty"), DEFAULT_TITLE, Component.literal("No tips loaded. Please review your config options!"), Optional.of(999999));
    private static Map<ResourceLocation, ITipSerializer<?>> SERIALIZERS = new HashMap<>();
    private static Set<Class<? extends Screen>> SCREENS = new HashSet<>();

    public static void registerTipSerializer(ResourceLocation id, ITipSerializer<?> serializer) {

        SERIALIZERS.put(id, serializer);
    }

    public static void registerTipScreen(Class<? extends Screen> screenClass) {

        SCREENS.add(screenClass);
    }

    public static boolean canRenderOnScreen(Screen screen) {

        return SCREENS.stream().filter(clazz -> clazz.isInstance(screen)).count() > 0;
    }

    public static ITip getRandomTip() {

        final List<ITip> filteredTips = getLoadedTips().stream().filter(TipsAPI::canDisplayTip).toList();

        if (!filteredTips.isEmpty()) {

            return filteredTips.get(Constants.RANDOM.nextInt(filteredTips.size()));
        }

        return EMPTY;
    }

    public static ITipSerializer<?> getTipSerializer(ResourceLocation id) {

        return SERIALIZERS.get(id);
    }

    public static List<ITip> getLoadedTips() {

        return TipsModCommon.TIP_MANAGER.getTips();
    }

    public static boolean canDisplayTip(ITip tip) {

        final ResourceLocation id = tip.getId();

        if (TipsModCommon.CONFIG.ignoredNamespaces.contains(id.getNamespace())) {

            return false;
        }

        if (TipsModCommon.CONFIG.ignoredTips.contains(id.toString())) {

            return false;
        }

        final ComponentContents contents = tip.getText().getContents();

        if (contents instanceof TranslatableContents) {

            final String key = ((TranslatableContents) contents).getKey();

            // Ignore tips that don't have a localization in the current language.
            if (!I18n.exists(key)) {

                return false;
            }
        }

        return true;
    }
}