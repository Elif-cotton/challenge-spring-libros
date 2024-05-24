package com.aluracursos.literatura.repository;

import com.aluracursos.literatura.model.Autor;
import com.aluracursos.literatura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByTituloContainsIgnoreCase(String nombreLibro);

    @Query(value = "SELECT * FROM libros WHERE idiomas @> ARRAY[?1]", nativeQuery = true)
    List<Libro> findByIdiomasContaining(String idioma);

    @Query(value = "SELECT COUNT(*) FROM libros WHERE idiomas @> ARRAY[:idioma]", nativeQuery = true)
    long countByIdioma(@Param("idioma") String idioma);

    @Query("SELECT a FROM Autor a")
    List<Autor> findAllAutores();

    @Query("SELECT a FROM Autor a WHERE CAST(a.fechaDeNacimiento AS integer) <= :year1 AND (CAST(a.fechaDeFallecimiento AS integer) IS NULL OR CAST(a.fechaDeFallecimiento AS integer) >= :year2)")
    List<Autor> findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThanEqual(@Param("year1") int year1, @Param("year2") int year2);


}
