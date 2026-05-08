package edu.cit.chan.unilost;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity test for the application module.
 *
 * The original {@code @SpringBootTest contextLoads()} required live MongoDB Atlas /
 * Gmail SMTP credentials at test time, so it could not be run in CI without secrets.
 * The actual integration coverage is provided by per-slice {@code @WebMvcTest}s
 * (e.g. {@link edu.cit.chan.unilost.features.auth.AuthControllerTest}); the bulk of
 * behaviour is covered by Mockito-based unit tests on each service.
 */
class UniLostApplicationTests {

	@Test
	void mainClassExists() {
		assertThat(UniLostApplication.class).isNotNull();
	}

}
