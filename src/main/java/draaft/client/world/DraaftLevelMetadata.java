package draaft.client.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DraaftLevelMetadata(String roomCode, String worldId, List<String> playerNames) {
    private static final int STRING_NBT_TYPE = 8;

    public void write(CompoundTag tag) {
        tag.put("draaft", toTag());
    }

    private CompoundTag toTag() {
        var tag = new CompoundTag();

        tag.putInt("DraaftDataVersion", 1);
        tag.putString("room", roomCode);
        tag.putString("worldId", worldId);

        var playersTag = new ListTag();

        for (var player : playerNames) {
            playersTag.add(StringTag.of(player));
        }

        tag.put("opponentNames", playersTag);

        return tag;
    }

    public static @Nullable DraaftLevelMetadata read(CompoundTag tag) {
        if (tag.contains("draaft")) {
            return fromTag(tag.getCompound("draaft"));
        } else {
            return null;
        }
    }

    private static DraaftLevelMetadata fromTag(CompoundTag tag) {
        assert tag.getInt("DraaftDataVersion") == 1;

        var playerNames = tag.getList("opponentNames", STRING_NBT_TYPE)
            .stream()
            .map(Tag::asString)
            .toList();

        return new DraaftLevelMetadata(
            tag.getString("room"),
            tag.getString("worldId"),
            playerNames
        );
    }
}
