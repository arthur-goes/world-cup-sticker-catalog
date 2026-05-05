package com.arthurgoes.world_cup_sticker_catalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WorldCupStickerCatalogApplicationTests {

	@Test
	void contextLoads() {
	}

}
