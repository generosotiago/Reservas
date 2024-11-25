package br.com.senai.Repository;

import br.com.senai.Entity.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ReservaRepository extends MongoRepository<Reserva, String> {

    @Query("{ 'descricaoEspaco': ?0, $or: [ { 'inicio': { $lt: ?2 }, 'termino': { $gt: ?1 } } ] }")
    List<Reserva> findConflitos(String descricaoEspaco, String inicio, String termino);
}
