package com.kodehaus.plaza.controller.mocks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class ParqueaderoMockController {

    @GetMapping("/api/parqueadero")
    public Map<String, Object> getParqueadero() {
        Random rnd = new Random();
        int cuposTotales = 120;
        int ocupados = 45 + rnd.nextInt(30);
        double ocupacion = (ocupados * 100.0) / cuposTotales;

        // Tarifas por tipo de vehículo (base empleado)
        Map<String, Integer> tarifaBaseEmpleado = Map.of(
            "carro", 3300,
            "moto", 1500,
            "camion", 6000
        );
        Map<String, Double> factorPorCliente = Map.of(
            "empleado", 1.0,
            "duenoLocal", 1.2,
            "visitante", 2.0
        );

        // Generación de movimientos y actualmente dentro
        List<Map<String, Object>> movimientosHoy = new ArrayList<>();
        List<Map<String, Object>> vehiculosDentro = new ArrayList<>();

        for (int i = 0; i < ocupados; i++) {
            String placa = generarPlaca(rnd);
            String tipoVehiculo = elegir(rnd, "carro", "moto", "camion");
            String tipoCliente = elegir(rnd, "empleado", "duenoLocal", "visitante");

            int hEntrada = 6 + rnd.nextInt(10); // 06:00 a 15:59
            int mEntrada = rnd.nextInt(60);
            String entrada = String.format("%02d:%02d", hEntrada, mEntrada);

            boolean salio = rnd.nextBoolean(); // algunos aún adentro
            String salida = null;
            int horas = 1 + rnd.nextInt(5);
            if (salio) {
                int hSalida = Math.min(23, hEntrada + horas);
                int mSalida = rnd.nextInt(60);
                salida = String.format("%02d:%02d", hSalida, mSalida);
            }

            int base = tarifaBaseEmpleado.getOrDefault(tipoVehiculo, 3000);
            double factor = factorPorCliente.getOrDefault(tipoCliente, 1.0);
            int tarifaHora = (int) Math.round(base * factor);
            int monto = tarifaHora * horas;

            Map<String, Object> v = new LinkedHashMap<>();
            v.put("placa", placa);
            v.put("tipoVehiculo", tipoVehiculo);
            v.put("tipoCliente", tipoCliente);
            v.put("entrada", entrada);
            v.put("salida", salida); // null = aún dentro
            v.put("horas", horas);
            v.put("tarifaHora", tarifaHora);
            v.put("monto", monto);

            if (salida == null) vehiculosDentro.add(v); else movimientosHoy.add(v);
        }

        // Ingresos mensuales (últimos 6 meses)
        List<Map<String, Object>> ingresosMensuales = new ArrayList<>();
        String[] labels = etiquetasMeses();
        for (int i = 0; i < labels.length; i++) {
            int ingresos = 2_000_000 + rnd.nextInt(3_000_000);
            ingresosMensuales.add(Map.of("mes", labels[i], "ingresos", ingresos));
        }

        Map<String, Object> tarifas = new LinkedHashMap<>();
        tarifas.put("baseEmpleadoPorHora", tarifaBaseEmpleado);
        tarifas.put("factorPorCliente", factorPorCliente);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("cuposTotales", cuposTotales);
        resp.put("ocupados", ocupados);
        resp.put("ocupacion", ocupacion);
        resp.put("vehiculos", movimientosHoy); // histórico de hoy (salidos)
        resp.put("vehiculosDentro", vehiculosDentro); // aún adentro
        resp.put("ingresosMensuales", ingresosMensuales);
        resp.put("tarifas", tarifas);
        resp.put("ingresosHoy", movimientosHoy.stream().mapToInt(m -> (int)m.get("monto")).sum());
        return resp;
    }

    private static String generarPlaca(Random rnd) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 3; j++) sb.append((char) ('A' + rnd.nextInt(26)));
        sb.append(String.format("%03d", rnd.nextInt(1000)));
        return sb.toString();
    }

    private static String elegir(Random rnd, String... opciones) {
        return opciones[rnd.nextInt(opciones.length)];
    }

    private static String[] etiquetasMeses() {
        String[] meses = {"May", "Jun", "Jul", "Ago", "Sep", "Oct"};
        return meses;
    }
}
