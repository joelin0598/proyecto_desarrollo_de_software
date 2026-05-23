#!/usr/bin/env pwsh
<# 
    Script para diagnosticar el estado de las citas en la base de datos PostgreSQL
    Uso: .\check_appointments.ps1
#>

param(
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "his",
    [string]$Username = "postgres",
    [string]$Password = "admin"
)

# Construir la conexión
$ConnectionString = "Host=$Host;Port=$Port;Database=$Database;Username=$Username;Password=$Password"

# Importar módulo PostgreSQL
try {
    # Intentar usar psql directamente si está disponible
    Write-Host "Intentando conectar a PostgreSQL..." -ForegroundColor Cyan
    
    $psqlPath = Get-Command psql -ErrorAction SilentlyContinue
    
    if ($psqlPath) {
        Write-Host "✅ psql encontrado en: $($psqlPath.Source)" -ForegroundColor Green
        
        $env:PGPASSWORD = $Password
        
        # Query 1: Resumen de citas por estado
        Write-Host "`n === RESUMEN DE CITAS ===" -ForegroundColor Yellow
        psql -h $Host -U $Username -d $Database -c "
            SELECT 
                estado_cita,
                estado_administrativo,
                COUNT(*) as cantidad
            FROM cita_medica
            WHERE is_active = true
            GROUP BY estado_cita, estado_administrativo
            ORDER BY estado_cita, estado_administrativo;
        "
        
        # Query 2: Citas disponibles para atención médica
        Write-Host "`n === CITAS DISPONIBLES PARA ATENCIÓN (cola del doctor) ===" -ForegroundColor Yellow
        psql -h $Host -U $Username -d $Database -c "
            SELECT 
                cm.cita_medica_id,
                p.nombre_completo as paciente,
                hs.nombre_completo as doctor,
                cm.fecha_cita,
                cm.hora_cita,
                cm.estado_cita,
                cm.estado_administrativo,
                cm.motivo_consulta
            FROM cita_medica cm
            JOIN paciente p ON cm.paciente_id = p.paciente_id
            JOIN personal_hospitalario hs ON cm.personal_id = hs.personal_id
            WHERE cm.estado_cita = 'PROGRAMADA'
                AND cm.estado_administrativo = 'PAGO_VALIDADO'
                AND cm.is_active = true
            ORDER BY cm.fecha_cita ASC, cm.hora_cita ASC;
        "
        
        # Query 3: Citas con pago pendiente
        Write-Host "`n⏳ === CITAS CON PAGO PENDIENTE ===" -ForegroundColor Yellow
        psql -h $Host -U $Username -d $Database -c "
            SELECT 
                cm.cita_medica_id,
                p.nombre_completo as paciente,
                hs.nombre_completo as doctor,
                cm.fecha_cita,
                cm.hora_cita,
                cm.estado_administrativo,
                cm.observacion_administrativa
            FROM cita_medica cm
            JOIN paciente p ON cm.paciente_id = p.paciente_id
            JOIN personal_hospitalario hs ON cm.personal_id = hs.personal_id
            WHERE cm.estado_cita = 'PROGRAMADA'
                AND cm.estado_administrativo = 'PAGO_PENDIENTE'
                AND cm.is_active = true
            ORDER BY cm.fecha_cita ASC;
        "
        
        # Query 4: Médicos disponibles
        Write-Host "`n‍⚕️ === DOCTORES REGISTRADOS ===" -ForegroundColor Yellow
        psql -h $Host -U $Username -d $Database -c "
            SELECT 
                ph.personal_id,
                ph.nombre_completo,
                ph.rol,
                me.nombre as especialidad,
                u.email
            FROM personal_hospitalario ph
            LEFT JOIN especialidad_medica me ON ph.especialidad_id = me.especialidad_medica_id
            LEFT JOIN usuario u ON ph.usuario_id = u.usuario_id
            WHERE ph.rol = 'DOCTOR'
            ORDER BY ph.nombre_completo;
        "
        
        # Query 5: Pacientes registrados
        Write-Host "`n === PACIENTES REGISTRADOS ===" -ForegroundColor Yellow
        psql -h $Host -U $Username -d $Database -c "
            SELECT 
                paciente_id,
                nombre_completo,
                dpi,
                usuario_id
            FROM paciente
            LIMIT 10;
        "
        
        Remove-Item env:PGPASSWORD
        
        Write-Host "`n✅ Diagnóstico completado" -ForegroundColor Green
    }
    else {
        Write-Host "❌ psql no está instalado o no está en PATH" -ForegroundColor Red
        Write-Host "Instala PostgreSQL Client o ejecuta las queries manualmente en pgAdmin/DBeaver" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
}
