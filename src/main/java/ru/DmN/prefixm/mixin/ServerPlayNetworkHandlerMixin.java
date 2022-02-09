package ru.DmN.prefixm.mixin;

import net.minecraft.client.option.ChatVisibility;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.DmN.prefixm.Main;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    protected abstract void executeCommand(String input);

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private int messageCooldown;

    @Shadow
    public abstract void disconnect(Text reason);

    /**
     * @author DomamaN202
     */
    @Overwrite
    private void handleMessage(TextStream.Message message) {
        if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.disabled.options")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
        } else {
            this.player.updateLastActionTime();
            String string = message.getRaw();
            if (string.startsWith("/")) {
                this.executeCommand(string);
            } else {
                String string2 = message.getFiltered();

                Text text = string2.isEmpty() ? null : new TranslatableText("chat.type.text", this.player.getDisplayName(), string2);
                Text text2 = new TranslatableText("chat.type.text", this.player.getDisplayName(), string);
                if (Main.prefixes.containsKey(this.player.getUuid())) {
                    text = new LiteralText(Main.prefixes.get(this.player.getUuid())).append(text);
                    text2 = new LiteralText(Main.prefixes.get(this.player.getUuid())).append(text2);
                }
                Text finalText = text2;
                Text finalText1 = text;
                this.server.getPlayerManager().broadcast(text2, (player) -> this.player.shouldFilterMessagesSentTo(player) ? finalText1 : finalText, MessageType.CHAT, this.player.getUuid());
            }

            this.messageCooldown += 20;
            if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                this.disconnect(new TranslatableText("disconnect.spam"));
            }

        }
    }
}
