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
public class AlbumEntitiesSchemaTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String ALBUM_ENTITY_INSERT_QUERY =
    """
        INSERT INTO album_entities (code, name, type)
        VALUES (?, ?, ?);
    """;

    private static final String DEFAULT_CODE = "BRA";
    private static final String DEFAULT_NAME = "Brasil";
    private static final String DEFAULT_TYPE = "NATIONAL_TEAM";

    @ParameterizedTest(name = "should add {1} entity")
    @MethodSource("validAlbumEntitiesData")
    public void insertsAlbumEntity_WhenDataIsValid(String code, String name, String type ){
        Integer updatedLinesAmount = jdbcTemplate.update(
            ALBUM_ENTITY_INSERT_QUERY,
            code,
            name,
            type
        );

        assertThat(updatedLinesAmount).isEqualTo(1);
    }

    @ParameterizedTest(name = "should reject row insertion when {3}", quoteTextArguments = false)
    @MethodSource("albumEntitiesWithNullRequiredFields")
    public void rejectsAlbumEntity_WhenAnyColumnIsNull(String code, String name, String type, String description){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type));
    }

    @Test
    public void rejectsAlbumEntity_WhenCodeNotCapitalized(){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, DEFAULT_CODE.toLowerCase(), DEFAULT_NAME, DEFAULT_TYPE));
    }

    @Test
    public void rejectsAlbumEntity_WhenTypeNotCapitalized(){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, DEFAULT_CODE, DEFAULT_NAME, DEFAULT_TYPE.toLowerCase()));
    }

    @Test
    public void rejectsAlbumEntity_WhenTypeIsInvalid(){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, "CC", "Coca-Cola", "COCA"));
    }

    @Test
    public void rejectsAlbumEntity_WhenCodeIsNotUnique(){
        String code = "BRA";
        String name = "Brasil";
        String type = "NATIONAL_TEAM";

        jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type));
    }

    @ParameterizedTest(name = "should reject invalid {0} code for {2} type")
    @MethodSource("albumEntitiesWithInvalidCodeForGivenType")
    public void rejectsAlbumEntity_WhenCodeDoesNotMatchType(String code, String name, String type){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type));
    }

    @ParameterizedTest(name = "should reject row insertion of not capitalized code {0}")
    @MethodSource("albumEntitiesWithNotCapitalizedCodes")
    public void rejectsAlbumEntity_whenCodeIsNotCapitalized(String code, String name, String type){
        assertThatExceptionOfType(DataIntegrityViolationException.class)
            .isThrownBy(() -> jdbcTemplate.update(ALBUM_ENTITY_INSERT_QUERY, code, name, type));
    }

    private static Stream<Arguments> validAlbumEntitiesData(){
        return Stream.of(
            Arguments.of("CC", "Coca-Cola","COCA_COLA"),
            Arguments.of("FWC", "Fifa World Cup", "FIFA_WORLD_CUP"),
            Arguments.of("BRA", "Brasil", "NATIONAL_TEAM")
        );
    }

    private static Stream<Arguments> albumEntitiesWithNullRequiredFields(){
        return Stream.of(
            Arguments.of(null, "Brasil", "NATIONAL_TEAM", "code is null"),
            Arguments.of("BRA", null,"NATIONAL_TEAM", "name is null"),
            Arguments.of("BRA", "Brasil", null, "type is null")
        );
    }

    private static Stream<Arguments> albumEntitiesWithInvalidCodeForGivenType(){
        return Stream.of(
            Arguments.of("CCO", "Coca-Cola", "COCA_COLA"),
            Arguments.of("CA", "Coca-Cola", "COCA_COLA"),
            Arguments.of("FFA", "Fifa World Cup", "FIFA_WORLD_CUP"),
            Arguments.of("FF", "Fifa World Cup", "FIFA_WORLD_CUP"),
            Arguments.of("BR", "Brasil", "NATIONAL_TEAM"),
            Arguments.of("BRAS", "Brasil", "NATIONAL_TEAM")
        );
    }

    private static Stream<Arguments> albumEntitiesWithNotCapitalizedCodes(){
        return Stream.of(
            Arguments.of("cC", "Coca-Cola", "COCA_COLA"),
            Arguments.of("cc", "Coca-Cola", "COCA_COLA"),
            Arguments.of("fWC", "Fifa World Cup", "FIFA_WORLD_CUP"),
            Arguments.of("fwc", "Fifa World Cup", "FIFA_WORLD_CUP"),
            Arguments.of("bRA", "Brasil", "NATIONAL_TEAM"),
            Arguments.of("bra", "Brasil", "NATIONAL_TEAM")
        );
    }

}
