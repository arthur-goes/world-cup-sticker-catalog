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

import java.sql.Date;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DataJpaTest
@Import(TestcontainersConfig.class)
public class PlayersSchemaTest {

    private static final String ALBUM_ENTITY_INSERT_QUERY = """
        INSERT INTO album_entities (code, name, type)
        VALUES (?, ?, ?);
        """;

    private static final String WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY = """
        INSERT INTO world_cup_national_teams (national_team_code, group_letter, group_slot)
        VALUES (?, ?, ?);
        """;

    private static final String REGIONAL_TEAM_INSERT_QUERY = """
        INSERT INTO regional_teams (name, code)
        VALUES (?, ?)
        RETURNING id;
        """;

    private static final String PLAYER_INSERT_QUERY = """
        INSERT INTO players (full_name, birth_date, height_cm, weight_kg, regional_team_id, national_team_code)
        VALUES (?, ?, ?, ?, ?, ?);
        """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void insertsPlayer_WhenDataIsValid() {
        insertNationalTeam("BRA", "Brasil");
        Integer regionalTeamId = insertRegionalTeam("Flamengo", "FLA");
        String fullName = "Player One";
        Date birthDate = Date.valueOf(LocalDate.of(2000, 1, 1));
        Integer heightCm = 180;
        Integer weightKg = 75;
        String nationalTeamCode = "BRA";

        int updatedRows = insertPlayer(fullName, birthDate, heightCm, weightKg, regionalTeamId, nationalTeamCode);

        assertThat(updatedRows).isEqualTo(1);
    }

    @Test
    public void insertsPlayer_WhenRegionalTeamIsNull() {
        insertNationalTeam("BRA", "Brasil");
        String fullName = "Player One";
        Date birthDate = null;
        Integer heightCm = null;
        Integer weightKg = null;
        Integer regionalTeamId = null;
        String nationalTeamCode = "BRA";

        int updatedRows = insertPlayer(fullName, birthDate, heightCm, weightKg, regionalTeamId, nationalTeamCode);

        assertThat(updatedRows).isEqualTo(1);
    }

    @ParameterizedTest(name = "should reject row insertion when {6}")
    @MethodSource("playersWithInvalidData")
    public void rejectsPlayer_WhenDataIsInvalid(
        String fullName,
        Date birthDate,
        Integer heightCm,
        Integer weightKg,
        Integer regionalTeamId,
        String nationalTeamCode,
        String description
    ) {
        insertNationalTeam("BRA", "Brasil");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> insertPlayer(fullName, birthDate, heightCm, weightKg, regionalTeamId, nationalTeamCode));
    }

    @Test
    public void setsRegionalTeamToNull_WhenRegionalTeamIsDeleted() {
        insertNationalTeam("BRA", "Brasil");
        Integer regionalTeamId = insertRegionalTeam("Flamengo", "FLA");
        String fullName = "Player One";
        Date birthDate = null;
        Integer heightCm = null;
        Integer weightKg = null;
        String nationalTeamCode = "BRA";

        insertPlayer(fullName, birthDate, heightCm, weightKg, regionalTeamId, nationalTeamCode);

        jdbcTemplate.update("DELETE FROM regional_teams WHERE id = ?", regionalTeamId);
        Integer currentRegionalTeamId = jdbcTemplate.queryForObject(
            "SELECT regional_team_id FROM players WHERE full_name = ?",
            Integer.class,
            fullName
        );

        assertThat(currentRegionalTeamId).isNull();
    }

    private void insertNationalTeam(String code, String name) {
        jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, "NATIONAL_TEAM");
        jdbcTemplate.update(WORLD_CUP_NATIONAL_TEAM_INSERT_QUERY, code, "A", 1);
    }

    private Integer insertRegionalTeam(String name, String code) {
        return jdbcTemplate.queryForObject(REGIONAL_TEAM_INSERT_QUERY, Integer.class, name, code);
    }

    private int insertPlayer(
        String fullName,
        Date birthDate,
        Integer heightCm,
        Integer weightKg,
        Integer regionalTeamId,
        String nationalTeamCode
    ) {
        return jdbcTemplate.update(
            PLAYER_INSERT_QUERY,
            fullName,
            birthDate,
            heightCm,
            weightKg,
            regionalTeamId,
            nationalTeamCode
        );
    }

    private static Stream<Arguments> playersWithInvalidData() {
        return Stream.of(
            //Arguments order: fullName, birthDate, heightCm, weightKg, regionalTeamId, nationalTeamCode, testCaseDescription
            Arguments.of(null, null, null, null, null, "BRA", "full name is null"),
            Arguments.of("Player One", null, null, null, null, null, "national team code is null"),
            Arguments.of("Player One", null, null, null, null, "ARG", "national team does not exist"),
            Arguments.of("Player One", null, 0, null, null, "BRA", "height is zero"),
            Arguments.of("Player One", null, -1, null, null, "BRA", "height is negative"),
            Arguments.of("Player One", null, null, 0, null, "BRA", "weight is zero"),
            Arguments.of("Player One", null, null, -1, null, "BRA", "weight is negative"),
            Arguments.of("Player One", null, null, null, 999, "BRA", "regional team does not exist")
        );
    }
}
