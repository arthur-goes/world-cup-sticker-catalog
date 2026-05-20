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
public class WorldCupNationalTeamsSchemaTest {

    private static final String ALBUM_ENTITY_INSERT_QUERY = """
        INSERT INTO album_entities (code, name, type)
        VALUES (?, ?, ?);
        """;

    private static final String WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY = """
        INSERT INTO world_cup_national_teams (national_team_code, group_letter, group_slot)
        VALUES (?, ?, ?);
        """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void insertsWorldCupNationalTeam_WhenDataIsValid() {
        insertNationalTeamAlbumEntity("BRA", "Brasil");

        int updatedRows = jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", 1);

        assertThat(updatedRows).isEqualTo(1);
    }

    @ParameterizedTest(name = "should reject row insertion when {3}")
    @MethodSource("worldCupNationalTeamsWithNullRequiredFields")
    public void rejectsWorldCupNationalTeam_WhenRequiredFieldIsNull(
        String nationalTeamCode,
        String groupLetter,
        Integer groupSlot,
        String description
    ) {
        insertNationalTeamAlbumEntity("BRA", "Brasil");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(
                WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY,
                nationalTeamCode,
                groupLetter,
                groupSlot
            ));
    }

    @Test
    public void rejectsWorldCupNationalTeam_WhenAlbumEntityDoesNotExist() {
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", 1));
    }

    @ParameterizedTest(name = "should reject reserved album entity {0} as national team")
    @MethodSource("reservedAlbumEntities")
    public void rejectsWorldCupNationalTeam_WhenAlbumEntityIsReserved(String code, String name, String type) {
        jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, code, "A", 1));
    }

    @ParameterizedTest(name = "should reject group letter {0}")
    @MethodSource("invalidGroupLetters")
    public void rejectsWorldCupNationalTeam_WhenGroupLetterIsInvalid(String groupLetter) {
        insertNationalTeamAlbumEntity("BRA", "Brasil");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", groupLetter, 1));
    }

    @ParameterizedTest(name = "should reject group slot {0}")
    @MethodSource("invalidGroupSlots")
    public void rejectsWorldCupNationalTeam_WhenGroupSlotIsInvalid(Integer groupSlot) {
        insertNationalTeamAlbumEntity("BRA", "Brasil");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", groupSlot));
    }

    @Test
    public void rejectsWorldCupNationalTeam_WhenNationalTeamCodeIsNotUnique() {
        insertNationalTeamAlbumEntity("BRA", "Brasil");
        jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", 1);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "B", 1));
    }

    @Test
    public void rejectsWorldCupNationalTeam_WhenGroupSlotIsAlreadyOccupied() {
        insertNationalTeamAlbumEntity("BRA", "Brasil");
        insertNationalTeamAlbumEntity("ARG", "Argentina");
        jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "BRA", "A", 1);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, "ARG", "A", 1));
    }

    private void insertNationalTeamAlbumEntity(String code, String name) {
        jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, "NATIONAL_TEAM");
    }

    private static Stream<Arguments> worldCupNationalTeamsWithNullRequiredFields() {
        return Stream.of(
            Arguments.of(null, "A", 1, "national team code is null"),
            Arguments.of("BRA", null, 1, "group letter is null"),
            Arguments.of("BRA", "A", null, "group slot is null")
        );
    }

    private static Stream<Arguments> reservedAlbumEntities() {
        return Stream.of(
            Arguments.of("CC", "Coca-Cola", "COCA_COLA"),
            Arguments.of("FWC", "Fifa World Cup", "FIFA_WORLD_CUP")
        );
    }

    private static Stream<String> invalidGroupLetters() {
        return Stream.of("M", "Z", "a", "AA", "1");
    }

    private static Stream<Integer> invalidGroupSlots() {
        return Stream.of(0, 5, -1);
    }
}
