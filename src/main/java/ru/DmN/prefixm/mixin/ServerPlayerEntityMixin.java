package ru.DmN.prefixm.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.DmN.prefixm.Main;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow protected abstract boolean acceptsMessage(MessageType type);

    @Shadow public ServerPlayNetworkHandler networkHandler;

    /**
     * @author DomamaN202
     */
    @Overwrite
    public void sendMessage(Text message, MessageType type, UUID sender) {
        if (Main.prefixes.containsKey(sender))
            message = new LiteralText(Main.prefixes.get(sender)).append(message);

        if (this.acceptsMessage(type)) {
            Text finalMessage = message;
            this.networkHandler.sendPacket(new GameMessageS2CPacket(message, type, sender), (future) -> {
                if (!future.isSuccess() && (type == MessageType.GAME_INFO || type == MessageType.SYSTEM) && this.acceptsMessage(MessageType.SYSTEM)) {
                    Text text2 = (new LiteralText(finalMessage.asTruncatedString(256))).formatted(Formatting.YELLOW);
                    this.networkHandler.sendPacket(new GameMessageS2CPacket((new TranslatableText("multiplayer.message_not_delivered", text2)).formatted(Formatting.RED), MessageType.SYSTEM, sender));
                }
            });
        }
    }
}
