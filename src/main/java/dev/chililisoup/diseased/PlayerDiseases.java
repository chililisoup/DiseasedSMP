package dev.chililisoup.diseased;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.diseased.reg.ModItems;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class PlayerDiseases {
    public static final HashMap<String, PlayerDisease> DISEASE_MAP = new HashMap<>();
    public static final AttachmentType<PlayerDiseaseStatus> PLAYER_DISEASE_STATUS = AttachmentRegistry.create(
            Diseased.loc("player_disease_status"),
            builder -> builder
                    .initializer(PlayerDiseaseStatus::new)
                    .persistent(PlayerDiseaseStatus.CODEC)
                    .copyOnDeath()
                    .syncWith(PlayerDiseaseStatus.STREAM_CODEC, AttachmentSyncPredicate.all())
    );

    public static final PlayerDisease NONE = new PlayerDisease("none");
    public static final PlayerDisease SHORT = PlayerDisease.of(
            "short", Attributes.SCALE, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
    public static final PlayerDisease TALL = new PlayerDisease("tall");
    public static final PlayerDisease CRICK = new PlayerDisease("crick");
    public static final PlayerDisease SLIPPERY = new PlayerDisease("slippery");
    public static final PlayerDisease UNDEAD = new PlayerDisease("undead");
    public static final PlayerDisease PACIFIST = PlayerDisease.of(
            "pacifist", Attributes.ATTACK_DAMAGE, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
    public static final PlayerDisease BOUNCY = new PlayerDisease("bouncy");
    public static final PlayerDisease GILLED = PlayerDisease.of(
            "gilled", Attributes.OXYGEN_BONUS, 3, AttributeModifier.Operation.ADD_VALUE
    );
    public static final PlayerDisease POCKETLESS = new PlayerDisease(
            "pocketless",
            player -> PlayerMod.replaceSlots(player, ModItems.SLOT_BLOCKER.getDefaultInstance(), 9, 36),
            player -> PlayerMod.clearSlots(player, 9, 36)
    );
    public static final PlayerDisease ENDER = new PlayerDisease("ender");

    public static Optional<PlayerDisease> find(String name) {
        if (name.equals("none")) return Optional.empty();

        for (PlayerDisease disease : DISEASE_MAP.values())
            if (disease.name.equals(name)) return Optional.of(disease);

        return Optional.empty();
    }

    public static class PlayerDisease {
        public final String name;
        private final Consumer<Player> setup;
        private final Consumer<Player> cleanup;

        public PlayerDisease(String name, @Nullable Consumer<Player> setup, @Nullable Consumer<Player> cleanup) {
            this.name = name;
            this.setup = setup;
            this.cleanup = cleanup;

            DISEASE_MAP.put(name, this);
        }

        public static PlayerDisease of(String name, Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
            return new PlayerDisease(
                    name,
                    player -> PlayerMod.addAttribute(player, attribute, name, value, operation),
                    player -> PlayerMod.removeAttribute(player, attribute, name)
            );
        }

        public PlayerDisease(String name) {
            this(name, null, null);
        }

        public void setup(Player player) {
            if (this.setup != null) this.setup.accept(player);
        }

        public void cleanup(Player player) {
            if (this.cleanup != null) this.cleanup.accept(player);
        }

        public boolean foundOn(PlayerDiseaseStatus status) {
            return status.getPerm().equals(this)|| status.getTemp().equals(this);
        }

        public boolean foundOn(Player player) {
            return this.foundOn(PlayerDiseaseStatus.get(player));
        }

        public boolean equals(PlayerDisease other) {
            return other == this || other.name.equals(this.name);
        }
    }

    public record PlayerDiseaseStatus(String perm, String temp) {
        public static final Codec<PlayerDiseaseStatus> CODEC;
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDiseaseStatus> STREAM_CODEC;

        public PlayerDiseaseStatus(String perm) {
            this(perm, NONE.name);
        }

        public PlayerDiseaseStatus() {
            this(NONE.name);
        }

        public static PlayerDiseaseStatus get(Player player) {
            return player.getAttachedOrElse(PLAYER_DISEASE_STATUS, new PlayerDiseaseStatus());
        }

        public PlayerDisease getPerm() {
            return DISEASE_MAP.getOrDefault(perm, NONE);
        }

        public PlayerDisease getTemp() {
            return DISEASE_MAP.getOrDefault(temp, NONE);
        }

        static {
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("perm").forGetter(PlayerDiseaseStatus::perm),
                    Codec.STRING.fieldOf("temp").forGetter(PlayerDiseaseStatus::temp)
            ).apply(instance, PlayerDiseaseStatus::new));

            STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    PlayerDiseaseStatus::perm,
                    ByteBufCodecs.STRING_UTF8,
                    PlayerDiseaseStatus::temp,
                    PlayerDiseaseStatus::new
            );
        }
    }

    public static boolean infectPlayer(Player player, PlayerDisease disease, boolean perm) {
        PlayerDiseaseStatus status = PlayerDiseaseStatus.get(player);

        if (!disease.equals(NONE)) {
            PlayerDisease other = perm ? status.getTemp() : status.getPerm();
            if (other.equals(disease)) return false;
        }

        PlayerDisease current = perm ? status.getPerm() : status.getTemp();
        if (current.equals(disease)) return false;

        current.cleanup(player);
        disease.setup(player);

        PlayerDiseaseStatus newStatus = perm ?
                new PlayerDiseaseStatus(disease.name, status.temp) :
                new PlayerDiseaseStatus(status.perm, disease.name);

        player.setAttached(PLAYER_DISEASE_STATUS, newStatus);

        return true;
    }

    public static boolean curePlayer(Player player, boolean perm) {
        return infectPlayer(player, NONE, perm);
    }

    public static void noop() {}
}
