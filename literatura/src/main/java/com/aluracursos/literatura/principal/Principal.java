package com.aluracursos.literatura.principal;

import com.aluracursos.literatura.model.*;
import com.aluracursos.literatura.repository.LibroRepository;
import com.aluracursos.literatura.service.ConsumoAPI;
import com.aluracursos.literatura.service.ConvierteDatos;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private final String API_KEY = "TU-APIKEY-OMDB";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositorio;

    private List<Libro> libros;
    private Optional<Libro> libroBuscado;

    public Principal(LibroRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar libros por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma 
                    6 - Los 10 libros más descargados
                    7 - Buscar libros por Autor
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroweb();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosPorAno();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    lostop10libros();
                    break;
                case 7:
                    buscarLibroPorAutor();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }


    private DatosLibro getDatosLibro() {
        // Busqueda de libros por nombre
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()) {
            return libroBuscado.get();
        } else {
            throw new NoSuchElementException("El libro '" + tituloLibro + "' no fue encontrado.");
        }
    }
    private void buscarLibroweb() {
        try {
            DatosLibro datos = getDatosLibro();

            Optional<Libro> libroExistente = repositorio.findByTituloContainsIgnoreCase(datos.titulo());

            if (libroExistente.isPresent()) {
                System.out.println("El libro '" + datos.titulo() + "' ya está en el repositorio.");
            } else {
                System.out.println("Libro encontrado:");
                System.out.println(datos);

                Libro libro = new Libro();
                libro.setTitulo(datos.titulo());
                List<Autor> autores = new ArrayList<>();
                for (DatosAutor datosAutor : datos.autor()) {
                    autores.add(new Autor(datosAutor));
                }
                libro.setAutor(autores);
                libro.setIdiomas(datos.idiomas());
                libro.setNumeroDeDescargas(datos.numeroDeDescargas());

                repositorio.save(libro);

                System.out.println("Libro guardado en el repositorio.");
            }
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var nombreLibro = teclado.nextLine();
        libroBuscado = repositorio.findByTituloContainsIgnoreCase(nombreLibro);

        if(libroBuscado.isPresent()){
            System.out.println("El libro buscado es: " + libroBuscado.get());
        } else {
            System.out.println("Libro no encontrado");
        }

    }

    private void listarLibrosRegistrados() {
        libros = repositorio.findAll();

        System.out.println("*".repeat(50)); // Línea decorativa antes de la lista

        libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(libro -> {
                    System.out.println("*".repeat(6) + " LIBRO " + "*".repeat(6)); // Línea decorativa "****** LIBRO ******"
                    System.out.println(libro.toString()); // Utiliza el método toString de la clase Libro para imprimir los detalles del libro
                    System.out.println();
                });

        System.out.println("*".repeat(50)); // Línea decorativa después de la lista
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = repositorio.findAllAutores();

        System.out.println("*".repeat(50)); // Línea decorativa antes de la lista

        for (Autor autor : autores) {
            System.out.println("*".repeat(6) + " AUTOR " + "*".repeat(6)); // Línea decorativa "****** AUTOR ******"
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
            System.out.println();
        }

        System.out.println("*".repeat(50)); // Línea decorativa después de la lista
    }

    private void listarAutoresVivosPorAno() {

        System.out.println("Ingrese el año para listar autores vivos:");
        int year = Integer.parseInt(teclado.nextLine());

        // Buscar autores vivos en el año especificado
        List<Autor> autoresVivos = repositorio.findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThanEqual(year, year);

        if (!autoresVivos.isEmpty()) {
            System.out.println("Autores vivos en el año " + year + ":");
            for (Autor autor : autoresVivos) {
                System.out.println(autor.getNombre());
            }
        } else {
            System.out.println("No hay autores vivos en el año " + year);
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese la sigla del idioma en que desea buscar los libros:");
        System.out.println("es- español");
        System.out.println("en- ingles");
        var idiomaBuscado = teclado.nextLine().toLowerCase().trim();

        // Validar entrada del usuario
        if (idiomaBuscado.isEmpty()) {
            System.out.println("Idioma no ingresado.");
            return;
        }

        // Buscar libros por idioma
        List<Libro> libros = repositorio.findByIdiomasContaining(idiomaBuscado);

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma especificado.");
        } else {
            String nombreIdioma;
            switch (idiomaBuscado) {
                case "es":
                    nombreIdioma = "español";
                    break;
                case "en":
                    nombreIdioma = "inglés";
                    break;
                // Agregar más casos según sea necesario
                default:
                    nombreIdioma = idiomaBuscado;
                    break;
            }

            System.out.println("Libros en " + nombreIdioma + ":");
            libros.forEach(libro -> System.out.println(libro.getTitulo()));
        }
    }

    private void lostop10libros() {
        System.out.println("Top 10 libros más descargados");

        var json = consumoApi.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json,Datos.class);

        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibro::numeroDeDescargas).reversed())
                .limit(10)
                .map(l -> l.titulo().toUpperCase())
                .forEach(System.out::println);

        //Trabajando con estadisticas
        DoubleSummaryStatistics est = datos.resultados().stream()
                .filter(d -> d.numeroDeDescargas() >0 )
                .collect(Collectors.summarizingDouble(DatosLibro::numeroDeDescargas));
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad máxima de descargas: "+ est.getMax());
        System.out.println("Cantidad mínima de descargas: " + est.getMin());
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());

    }

    public void buscarLibroPorAutor() {
        System.out.println("Ingrese el nombre del autor:");
        try {
            var nombreAutor = teclado.nextLine();
            if (nombreAutor.trim().isEmpty()) {
                System.out.println("No se ingresó ningún nombre de autor.");
                return;
            }
            // Aquí debería estar el bloque try para manejar la llamada a getDatosLibroPorAutor
            DatosLibro datos = getDatosLibroPorAutor(nombreAutor);

            Optional<Libro> libroExistente = repositorio.findByTituloContainsIgnoreCase(datos.titulo());

            if (libroExistente.isPresent()) {
                System.out.println("El libro '" + datos.titulo() + "' ya está en el repositorio.");
            } else {
                System.out.println("Libro encontrado:");
                System.out.println(datos);

                Libro libro = new Libro();
                libro.setTitulo(datos.titulo());
                List<Autor> autores = new ArrayList<>();
                for (DatosAutor datosAutor : datos.autor()) {
                    autores.add(new Autor(datosAutor));
                }
                libro.setAutor(autores);
                libro.setIdiomas(datos.idiomas());
                libro.setNumeroDeDescargas(datos.numeroDeDescargas());

                repositorio.save(libro);

                System.out.println("Libro guardado en el repositorio.");
            }
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
    }

    private DatosLibro getDatosLibroPorAutor(String nombreAutor) {
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + nombreAutor.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .filter(libro -> libro.autor().stream().anyMatch(autor -> autor.nombre().toUpperCase().contains(nombreAutor.toUpperCase())))
                .findFirst();
        if (libroBuscado.isPresent()) {
            return libroBuscado.get();
        } else {
            throw new NoSuchElementException("No se encontró ningún libro del autor '" + nombreAutor + "'.");
        }
    }

}
