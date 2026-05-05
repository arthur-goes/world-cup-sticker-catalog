package com.arthurgoes.world_cup_sticker_catalog;

import org.springframework.boot.SpringApplication;

public class TestWorldCupStickerCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.from(WorldCupStickerCatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
