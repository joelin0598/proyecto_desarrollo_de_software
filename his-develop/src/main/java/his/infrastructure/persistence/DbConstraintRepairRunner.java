package his.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Repara restricciones heredadas de cita_medica que no se actualizan con ddl-auto=update.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbConstraintRepairRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        if (!isPostgreSql()) {
            return;
        }

        try {
            jdbcTemplate.execute("ALTER TABLE cita_medica DROP CONSTRAINT IF EXISTS cita_medica_estado_cita_check");
            jdbcTemplate.execute(
                    "ALTER TABLE cita_medica ADD CONSTRAINT cita_medica_estado_cita_check " +
                            "CHECK (estado_cita IN ('PROGRAMADA','EN_CURSO','CANCELADA','ATENDIDA'))");
            log.info("DB fix aplicado: cita_medica_estado_cita_check actualizado");

            jdbcTemplate.execute("ALTER TABLE orden_laboratorio DROP CONSTRAINT IF EXISTS orden_laboratorio_estado_check");
            jdbcTemplate.execute(
                    "ALTER TABLE orden_laboratorio ADD CONSTRAINT orden_laboratorio_estado_check " +
                            "CHECK (estado IN ('PENDIENTE_PAGO','PENDIENTE_MUESTRA','MUESTRA_RECIBIDA','EN_PROCESO','MUESTRA_RECHAZADA','COMPLETADO','FINALIZADO'))");
            log.info("DB fix aplicado: orden_laboratorio_estado_check actualizado");
        } catch (Exception ex) {
            log.warn("No se pudo aplicar fix de constraints: {}", ex.getMessage());
        }
    }

    private boolean isPostgreSql() {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgresql");
        } catch (Exception ex) {
            log.warn("No se pudo identificar motor de BD para fix de constraints: {}", ex.getMessage());
            return false;
        }
    }
}
