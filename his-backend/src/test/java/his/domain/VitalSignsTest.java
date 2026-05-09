package his.domain;

import his.domain.models.Priority;
import his.domain.models.VitalSigns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class VitalSignsTest {

	@Test
	void calculatePriority_setsRojo_whenSaturationIsCritical() {
		// Arrange
		VitalSigns vitalSigns = VitalSigns.builder()
				.saturacionOxigeno(80)
				.temperatura(37.0)
				.build();

		// Act
		vitalSigns.calculatePriority();

		// Assert
		assertEquals(Priority.ROJO, vitalSigns.getPriority());
	}

	@Test
	void calculatePriority_setsNaranja_whenTemperatureIsHigh() {
		// Arrange
		VitalSigns vitalSigns = VitalSigns.builder()
				.saturacionOxigeno(96)
				.temperatura(38.7)
				.build();

		// Act
		vitalSigns.calculatePriority();

		// Assert
		assertEquals(Priority.NARANJA, vitalSigns.getPriority());
	}

	@Test
	void calculatePriority_setsVerde_whenValuesAreStable() {
		// Arrange
		VitalSigns vitalSigns = VitalSigns.builder()
				.saturacionOxigeno(98)
				.temperatura(36.8)
				.build();

		// Act
		vitalSigns.calculatePriority();

		// Assert
		assertEquals(Priority.VERDE, vitalSigns.getPriority());
	}
}
