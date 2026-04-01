package com.githubzs.plataforma_reservas_medicas;

import org.springframework.boot.SpringApplication;

public class TestPlataformaReservasMedicasApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlataformaReservasMedicasApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
