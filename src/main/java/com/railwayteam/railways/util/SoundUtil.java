package com.railwayteam.railways.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class SoundUtil {
    protected static final List<SoundContext> sounds = new ArrayList<>();

    public static class SoundContext {
        public final Predicate<SoundContext> predicate;
        public final Consumer<SoundContext> afterStopped;
        public final SimpleSound sound;
        public int ticks = 0;

        public SoundContext(Predicate<SoundContext> predicate, Consumer<SoundContext> afterStopped, SimpleSound sound) {
            this.predicate = predicate;
            this.afterStopped = afterStopped;
            this.sound = sound;
        }
    }

    public static void playSoundUntil(SoundEvent sound, SoundCategory category, float volume, float pitch, double x, double y, double z, Predicate<SoundContext> predicate, Consumer<SoundContext> afterStopped) {
        Minecraft mc = Minecraft.getInstance();
        double d0 = mc.gameRenderer.getActiveRenderInfo().getProjectedView().squareDistanceTo(x, y, z);
        SimpleSound simplesound = new SimpleSound(sound, category, volume, pitch, x, y, z);
        mc.getSoundHandler().play(simplesound);
        sounds.add(new SoundContext(predicate, afterStopped, simplesound));
    }

    @Deprecated() // dont call this method!
    public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) return;
        List<SoundContext> soundsCopy = new ArrayList<>(sounds);
        for(SoundContext ctx : soundsCopy) {
            ctx.ticks++;
            if(!ctx.predicate.test(ctx)) {
                Minecraft.getInstance().getSoundHandler().stop(ctx.sound);
                ctx.afterStopped.accept(ctx);
                sounds.remove(ctx);
            }
        }
    }
}
