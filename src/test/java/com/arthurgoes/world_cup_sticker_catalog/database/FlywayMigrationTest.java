package com.arthurgoes.world_cup_sticker_catalog.database;

import com.arthurgoes.world_cup_sticker_catalog.configuration.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfig.class)
public class FlywayMigrationTest {

    private static final String TABLE_EXISTS_QUERY = """
        SELECT COUNT(*)
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = ?
        """;

    static String[] migrationTableNames(){
        return new String[]{
            "flyway_schema_history",
            "album_entities",
            "world_cup_national_teams",
            "regional_teams",
            "players",
            "stickers"
        };
    };

    @Autowired
    JdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "should create {0} table")
    @MethodSource("migrationTableNames")
    public void shouldCreateExpectedTable(String tableName) {
        Integer count = jdbcTemplate.queryForObject(TABLE_EXISTS_QUERY, Integer.class, tableName);

        assertThat(count).isEqualTo(1);
    }
}
