package br.com.senai.controller;

import br.com.senai.Entity.Reserva;
import br.com.senai.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    private static final List<String> ESPACOS_VALIDOS = Arrays.asList(
            "Sala de reunião 1",
            "Sala de reunião 2",
            "Auditório",
            "Refeitório",
            "Espaço Externo"
    );

    @PostMapping
    public ResponseEntity<?> criarReserva(@RequestBody Reserva reserva) {
        if (!ESPACOS_VALIDOS.contains(reserva.getDescricaoEspaco())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Espaço não encontrado! Use um dos seguintes: " + ESPACOS_VALIDOS);
        }

        List<Reserva> conflitos = reservaRepository.findConflitos(
                reserva.getDescricaoEspaco(),
                reserva.getInicio(),
                reserva.getTermino()
        );

        if (!conflitos.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Conflito de horário! Já existe uma reserva no período solicitado.");
        }

        Reserva novaReserva = reservaRepository.save(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
    }


    @GetMapping
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarReserva(@PathVariable String id) {
        return reservaRepository.findById(id)
                .<ResponseEntity<Object>>map(reserva -> ResponseEntity.ok(reserva))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarReserva(@PathVariable String id, @RequestBody Reserva reservaAtualizada) {
        if (!ESPACOS_VALIDOS.contains(reservaAtualizada.getDescricaoEspaco())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Espaço não encontrado! Use um dos seguintes: " + ESPACOS_VALIDOS);
        }

        return reservaRepository.findById(id).map(reserva -> {
            List<Reserva> conflitos = reservaRepository.findConflitos(
                    reservaAtualizada.getDescricaoEspaco(),
                    reservaAtualizada.getInicio(),
                    reservaAtualizada.getTermino()
            );

            boolean existeConflito = conflitos.stream()
                    .anyMatch(conf -> !conf.getId().equals(id));

            if (existeConflito) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Conflito de horário! Já existe uma reserva no período solicitado.");
            }

            reserva.setNomeResponsavel(reservaAtualizada.getNomeResponsavel());
            reserva.setDescricaoEspaco(reservaAtualizada.getDescricaoEspaco());
            reserva.setInicio(reservaAtualizada.getInicio());
            reserva.setTermino(reservaAtualizada.getTermino());
            Reserva reservaAtualizadaSalva = reservaRepository.save(reserva);
            return ResponseEntity.ok(reservaAtualizadaSalva);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarReserva(@PathVariable String id) {
        if (reservaRepository.existsById(id)) {
            reservaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada!");
    }
}
