package com.arthurgoes.world_cup_sticker_catalog.database;

import com.arthurgoes.world_cup_sticker_catalog.configuration.TestcontainersConfig;
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
public class RegionalTeamsSchemaTest {

    private static final String REGIONAL_TEAM_INSERT_QUERY = """
        INSERT INTO regional_teams (name, code)
        VALUES (?, ?);
        """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "should add {0} regional team")
    @MethodSource("validRegionalTeams")
    public void insertsRegionalTeam_WhenDataIsValid(String name, String code) {
        int updatedRows = jdbcTemplate.update(REGIONAL_TEAM_INSERT_QUERY, name, code);

        assertThat(updatedRows).isEqualTo(1);
    }

    @ParameterizedTest(name = "should reject row insertion when {2}")
    @MethodSource("regionalTeamsWithInvalidData")
    public void rejectsRegionalTeam_WhenDataIsInvalid(String name, String code, String description) {
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(REGIONAL_TEAM_INSERT_QUERY, name, code));
    }

    private static Stream<Arguments> validRegionalTeams() {
        return Stream.of(
            Arguments.of("Flamengo", "FLA"),
            Arguments.of("PSG", "PS"),
            Arguments.of("Free Agent", null)
        );
    }

    private static Stream<Arguments> regionalTeamsWithInvalidData() {
        return Stream.of(
            Arguments.of(null, "FLA", "name is null"),
            Arguments.of("Flamengo", "F", "code has less than two letters"),
            Arguments.of("Flamengo", "FLAM", "code has more than three letters"),
            Arguments.of("Flamengo", "fla", "code is not uppercase"),
            Arguments.of("Flamengo", "FL1", "code contains number")
        );
    }
}
