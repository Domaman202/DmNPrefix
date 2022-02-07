package ru.DmN.prefixm;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static Map<UUID, String> prefixes = new HashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            load();
            dispatcher.register(literal("prefix")
                    .then(argument("player", EntityArgumentType.player()).then(argument("prefix", StringArgumentType.greedyString()).executes(context -> {
                        prefixes.put(context.getArgument("player", EntitySelector.class).getPlayer(context.getSource()).getUuid(), context.getArgument("prefix", String.class).replace('#', '§'));
                        save();
                        return 1;
                    }))));
        });
    }

    public static void save() {
        try (var file = new ObjectOutputStream(new FileOutputStream("prefix_hash.data"))) {
            file.writeObject(prefixes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try (var file = new ObjectInputStream(new FileInputStream("prefix_hash.data"))) {
            prefixes = (Map<UUID, String>) file.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
