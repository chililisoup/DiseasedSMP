package dev.chililisoup.diseased;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModCommands {
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(
                Diseased.loc("disease_type"),
                DiseaseTypeArgument.class,
                SingletonArgumentInfo.contextFree(DiseaseTypeArgument::new)
        );

        ArgumentTypeRegistry.registerArgumentType(
                Diseased.loc("disease"),
                DiseaseArgument.class,
                SingletonArgumentInfo.contextFree(DiseaseArgument::new)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) ->
            dispatcher.register(Commands.literal("disease")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.literal("check")
                            .executes(commandContext -> checkDisease(
                                    commandContext.getSource(),
                                    commandContext.getSource().getPlayerOrException()
                            ))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .executes(commandContext -> checkDisease(
                                            commandContext.getSource(),
                                            EntityArgument.getPlayer(commandContext, "target")
                                    ))
                            )
                    )
                    .then(Commands.literal("cure")
                            .executes(commandContext -> cureDisease(
                                    commandContext.getSource(),
                                    ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                    false
                            ))
                            .then(Commands.argument("type", DiseaseTypeArgument.type())
                                    .executes(commandContext -> cureDisease(
                                            commandContext.getSource(),
                                            ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                            DiseaseTypeArgument.isPerm(commandContext, "type")
                                    ))
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .executes(commandContext -> cureDisease(
                                                    commandContext.getSource(),
                                                    EntityArgument.getPlayers(commandContext, "targets"),
                                                    DiseaseTypeArgument.isPerm(commandContext, "type")
                                            ))
                                    )
                            )
                    )
                    .then(Commands.literal("infect")
                            .then(Commands.argument("disease", DiseaseArgument.disease())
                                    .executes(commandContext -> infectDisease(
                                            commandContext.getSource(),
                                            ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                            DiseaseArgument.getDisease(commandContext, "disease"),
                                            false
                                    ))
                                    .then(Commands.argument("type", DiseaseTypeArgument.type())
                                            .executes(commandContext -> infectDisease(
                                                    commandContext.getSource(),
                                                    ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                                    DiseaseArgument.getDisease(commandContext, "disease"),
                                                    DiseaseTypeArgument.isPerm(commandContext, "type")
                                            ))
                                            .then(Commands.argument("targets", EntityArgument.players())
                                                    .executes(commandContext -> infectDisease(
                                                            commandContext.getSource(),
                                                            EntityArgument.getPlayers(commandContext, "targets"),
                                                            DiseaseArgument.getDisease(commandContext, "disease"),
                                                            DiseaseTypeArgument.isPerm(commandContext, "type")
                                                    ))
                                            )
                                    )
                            )
                    )
            )
        );
    }

    private static class DiseaseArgument implements ArgumentType<PlayerDiseases.PlayerDisease> {
        private static final DynamicCommandExceptionType ERROR_DISEASE_NOT_FOUND = new DynamicCommandExceptionType(
                object -> Component.literal(String.format("Unknown disease '%s'", object))
        );

        public static DiseaseArgument disease() {
            return new DiseaseArgument();
        }

        public static PlayerDiseases.PlayerDisease getDisease(CommandContext<CommandSourceStack> commandContext, String string) {
            return commandContext.getArgument(string, PlayerDiseases.PlayerDisease.class);
        }

        public PlayerDiseases.PlayerDisease parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readString();
            return PlayerDiseases.find(string).orElseThrow(() -> ERROR_DISEASE_NOT_FOUND.create(string));
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
            return SharedSuggestionProvider.suggest(
                    PlayerDiseases.DISEASE_MAP.keySet().stream().filter(name -> !name.equals("none")),
                    suggestionsBuilder
            );
        }
    }


    private static class DiseaseTypeArgument implements ArgumentType<Boolean> {
        private static final DynamicCommandExceptionType ERROR_DISEASE_TYPE_NOT_FOUND = new DynamicCommandExceptionType(
                object -> Component.literal(String.format("Unknown disease type '%s'", object))
        );

        public static DiseaseTypeArgument type() {
            return new DiseaseTypeArgument();
        }

        public static boolean isPerm(CommandContext<CommandSourceStack> commandContext, String string) {
            return commandContext.getArgument(string, Boolean.class);
        }

        public Boolean parse(StringReader stringReader) throws CommandSyntaxException {
            String string = stringReader.readString();

            if (string.equals("perm")) return true;
            if (string.equals("temp")) return false;

            throw ERROR_DISEASE_TYPE_NOT_FOUND.create(string);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
            return SharedSuggestionProvider.suggest(Stream.of("temp", "perm"), suggestionsBuilder);
        }
    }


    private static int cureDisease(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, boolean perm) {
        int successes = Math.toIntExact(collection.stream().map(player -> PlayerDiseases.curePlayer(player, perm)).filter(result -> result).count());

        if (successes <= 0) {
            if (collection.size() == 1) commandSourceStack.sendFailure(
                    Component.empty().append(collection.iterator().next().getDisplayName())
                            .append(Component.literal(String.format(" has no %s disease", perm ? "perm" : "temp")))
            );
            else commandSourceStack.sendFailure(Component.literal(String.format(
                    "None of the players have a %s disease",
                    perm ? "perm" : "temp"
            )));

            return successes;
        }

        if (collection.size() == 1) commandSourceStack.sendSuccess(
                () -> Component.literal(String.format("Cured the %s disease on ", perm ? "perm" : "temp"))
                        .append(collection.iterator().next().getDisplayName()),
                true
        );
        else commandSourceStack.sendSuccess(
                () -> Component.literal(String.format(
                        "Cured the %s disease on %d players",
                        perm ? "perm" : "temp",
                        collection.size())),
                true
        );

        return successes;
    }

    private static int infectDisease(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerDiseases.PlayerDisease disease, boolean perm) {
        int successes = Math.toIntExact(collection.stream().map(player -> PlayerDiseases.infectPlayer(player, disease, perm)).filter(result -> result).count());

        if (successes <= 0) {
            if (collection.size() == 1) commandSourceStack.sendFailure(
                    Component.literal("Failed to infect ")
                            .append(collection.iterator().next().getDisplayName())
                            .append(String.format(" with %s %s", perm ? "perm" : "temp", disease.name))
            );
            else commandSourceStack.sendFailure(Component.literal(String.format(
                    "Failed to infect any players with %s %s",
                    perm ? "perm" : "temp",
                    disease.name
            )));

            return successes;
        }

        collection.forEach(player -> PlayerDiseases.infectPlayer(player, disease, perm));

        if (collection.size() == 1) commandSourceStack.sendSuccess(
                () -> Component.literal("Infected ")
                        .append(collection.iterator().next().getDisplayName())
                        .append(String.format(" with %s %s", perm ? "perm" : "temp", disease.name)),
                true
        );
        else commandSourceStack.sendSuccess(
                () -> Component.literal(String.format(
                        "Infected %d players with %s %s",
                        successes,
                        perm ? "perm" : "temp",
                        disease.name)),
                true
        );

        return successes;
    }

    private static int checkDisease(CommandSourceStack commandSourceStack, ServerPlayer player) {
        PlayerDiseases.PlayerDiseaseStatus status = PlayerDiseases.PlayerDiseaseStatus.get(player);

        commandSourceStack.sendSuccess(
                () -> Component.empty()
                        .append(player.getDisplayName())
                        .append(String.format(
                                " has perm '%s' and temp '%s'",
                                status.getPerm().name,
                                status.getTemp().name)
                        ),
                true
        );

        return 1;
    }
}
