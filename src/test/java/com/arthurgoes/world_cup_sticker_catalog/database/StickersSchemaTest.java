package com.arthurgoes.world_cup_sticker_catalog.database;

import com.arthurgoes.world_cup_sticker_catalog.configuration.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DataJpaTest
@Import(TestcontainersConfig.class)
public class StickersSchemaTest {

    private static final String ALBUM_ENTITY_INSERT_QUERY = """
        INSERT INTO album_entities (code, name, type)
        VALUES (?, ?, ?);
        """;

    private static final String WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY = """
        INSERT INTO world_cup_national_teams (national_team_code, group_letter, group_slot)
        VALUES (?, ?, ?);
        """;

    private static final String PLAYER_INSERT_QUERY = """
        INSERT INTO players (full_name, birth_date, height_cm, weight_kg, regional_team_id, national_team_code)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id;
        """;

    private static final String STICKER_INSERT_QUERY = """
        INSERT INTO stickers (
            code,
            sticker_prefix_code,
            sticker_number,
            sticker_type,
            player_id,
            player_type,
            sticker_rarity,
            stock_available
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void insertsPlayerSticker_WhenDataIsValid() {
        Integer playerId = insertPlayerFixture();
        String code = "BRA-1";
        String stickerPrefixCode = "BRA";
        Integer stickerNumber = 1;
        String stickerType = "PLAYER";
        String playerType = "STANDARD";
        String stickerRarity = "BASE";
        Integer stockAvailable = 0;

        int updatedRows = insertSticker(
            code,
            stickerPrefixCode,
            stickerNumber,
            stickerType,
            playerId,
            playerType,
            stickerRarity,
            stockAvailable
        );

        assertThat(updatedRows).isEqualTo(1);
    }

    @ParameterizedTest(name = "should add {3} sticker")
    @MethodSource("validNonPlayerStickers")
    public void insertsNonPlayerSticker_WhenDataIsValid(
        String code,
        String stickerPrefixCode,
        Integer stickerNumber,
        String stickerType,
        String stickerRarity
    ) {
        insertAlbumEntity("BRA", "Brasil", "NATIONAL_TEAM");
        Integer playerId = null;
        String playerType = null;
        Integer stockAvailable = 0;

        int updatedRows = insertSticker(
            code,
            stickerPrefixCode,
            stickerNumber,
            stickerType,
            playerId,
            playerType,
            stickerRarity,
            stockAvailable
        );

        assertThat(updatedRows).isEqualTo(1);
    }

    @ParameterizedTest(name = "should reject row insertion when {8}")
    @MethodSource("stickersWithInvalidData")
    public void rejectsSticker_WhenDataIsInvalid(
        String code,
        String stickerPrefixCode,
        Integer stickerNumber,
        String stickerType,
        Integer playerId,
        String playerType,
        String stickerRarity,
        Integer stockAvailable,
        String description
    ) {
        Integer validPlayerId = insertPlayerFixture();
        Integer actualPlayerId = playerId == null ? null : validPlayerId;

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> insertSticker(
                code,
                stickerPrefixCode,
                stickerNumber,
                stickerType,
                actualPlayerId,
                playerType,
                stickerRarity,
                stockAvailable
            ));
    }

    @Test
    public void rejectsSticker_WhenStickerPrefixDoesNotExist() {
        Integer playerId = insertPlayerFixture();
        String code = "ARG-1";
        String stickerPrefixCode = "ARG";
        Integer stickerNumber = 1;
        String stickerType = "PLAYER";
        String playerType = "STANDARD";
        String stickerRarity = "BASE";
        Integer stockAvailable = 0;

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> insertSticker(
                code,
                stickerPrefixCode,
                stickerNumber,
                stickerType,
                playerId,
                playerType,
                stickerRarity,
                stockAvailable
            ));
    }

    @Test
    public void rejectsSticker_WhenPlayerDoesNotExist() {
        insertAlbumEntity("BRA", "Brasil", "NATIONAL_TEAM");
        String code = "BRA-1";
        String stickerPrefixCode = "BRA";
        Integer stickerNumber = 1;
        String stickerType = "PLAYER";
        Integer playerId = 999;
        String playerType = "STANDARD";
        String stickerRarity = "BASE";
        Integer stockAvailable = 0;

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> insertSticker(
                code,
                stickerPrefixCode,
                stickerNumber,
                stickerType,
                playerId,
                playerType,
                stickerRarity,
                stockAvailable
            ));
    }

    @Test
    public void rejectsSticker_WhenNaturalKeyIsNotUnique() {
        Integer playerId = insertPlayerFixture();
        String code = "BRA-1";
        String stickerPrefixCode = "BRA";
        Integer stickerNumber = 1;
        String stickerType = "PLAYER";
        String playerType = "STANDARD";
        String stickerRarity = "BASE";
        Integer stockAvailable = 0;

        insertSticker(
            code,
            stickerPrefixCode,
            stickerNumber,
            stickerType,
            playerId,
            playerType,
            stickerRarity,
            stockAvailable
        );

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> insertSticker(
                code,
                stickerPrefixCode,
                stickerNumber,
                stickerType,
                playerId,
                playerType,
                stickerRarity,
                stockAvailable
            ));
    }

    private Integer insertPlayerFixture() {
        insertAlbumEntity("BRA", "Brasil", "NATIONAL_TEAM");
        jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", 1);
        return jdbcTemplate.queryForObject(
            PLAYER_INSERT_QUERY,
            Integer.class,
            "Player One",
            null,
            null,
            null,
            null,
            "BRA"
        );
    }

    private void insertAlbumEntity(String code, String name, String type) {
        jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type);
    }

    private int insertSticker(
        String code,
        String stickerPrefixCode,
        Integer stickerNumber,
        String stickerType,
        Integer playerId,
        String playerType,
        String stickerRarity,
        Integer stockAvailable
    ) {
        return jdbcTemplate.update(
            STICKER_INSERT_QUERY,
            code,
            stickerPrefixCode,
            stickerNumber,
            stickerType,
            playerId,
            playerType,
            stickerRarity,
            stockAvailable
        );
    }

    private static Stream<Arguments> validNonPlayerStickers() {
        return Stream.of(
            Arguments.of("BRA-1", "BRA", 1, "BADGE", "BASE"),
            Arguments.of("BRA-2", "BRA", 2, "TEAM_PHOTO", "BASE")
        );
    }

    private static Stream<Arguments> stickersWithInvalidData() {
        return Stream.of(
            Arguments.of(null, "BRA", 1, "PLAYER", 1, "STANDARD", "BASE", 0, "code is null"),
            Arguments.of("BRA-1", null, 1, "PLAYER", 1, "STANDARD", "BASE", 0, "sticker prefix code is null"),
            Arguments.of("BRA-1", "BRA", null, "PLAYER", 1, "STANDARD", "BASE", 0, "sticker number is null"),
            Arguments.of("BRA-1", "BRA", 1, null, 1, "STANDARD", "BASE", 0, "sticker type is null"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, "STANDARD", null, 0, "sticker rarity is null"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, "STANDARD", "BASE", -1, "stock is negative"),
            Arguments.of("BRA-0", "BRA", 0, "PLAYER", 1, "STANDARD", "BASE", 0, "sticker number is zero"),
            Arguments.of("BRA--1", "BRA", -1, "PLAYER", 1, "STANDARD", "BASE", 0, "sticker number is negative"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, "CAPTAIN", "BASE", 0, "player type is invalid"),
            Arguments.of("BRA-1", "BRA", 1, "CARD", 1, "STANDARD", "BASE", 0, "sticker type is invalid"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, "STANDARD", "DIAMOND", 0, "sticker rarity is invalid"),
            Arguments.of("BRA-1G", "BRA", 1, "PLAYER", 1, "STANDARD", "GOLD", 0, "standard player has special rarity"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", null, "STANDARD", "BASE", 0, "player sticker has no player id"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, null, "BASE", 0, "player sticker has no player type"),
            Arguments.of("BRA-1", "BRA", 1, "BADGE", 1, null, "BASE", 0, "badge has player id"),
            Arguments.of("BRA-1", "BRA", 1, "TEAM_PHOTO", 1, null, "BASE", 0, "team photo has player id"),
            Arguments.of("BRA-1G", "BRA", 1, "BADGE", null, null, "GOLD", 0, "badge has special rarity"),
            Arguments.of("BRA-1G", "BRA", 1, "TEAM_PHOTO", null, null, "GOLD", 0, "team photo has special rarity"),
            Arguments.of("BRA-2", "BRA", 1, "PLAYER", 1, "STANDARD", "BASE", 0, "base code does not match fields"),
            Arguments.of("BRA-1", "BRA", 1, "PLAYER", 1, "LEGEND", "GOLD", 0, "special rarity code does not match fields")
        );
    }
}
