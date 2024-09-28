package org.example

import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

/* El fichero calificaciones.csv contiene las calificaciones de un curso.
   Durante el curso se realizaron dos exámenes parciales de teoría y un
   examen de prácticas. Los alumnos que tuvieron menos de 4 en alguno de
   estos exámenes pudieron repetirlo al final del curso (convocatoria ordinaria). */

fun main() {

    // Definimos la ruta del archivo CSV que contiene las calificaciones
    val ruta = Path("src/main/resources/calificaciones.csv")
    // Leemos las calificaciones desde el archivo y obtenemos una lista de alumnos ordenada por apellidos
    val alumnos = leerCalificaciones(ruta)
    // Calculamos la nota final para cada alumno basada en sus exámenes y prácticas
    val alumnosNotaFinal = calcularNotaFinal(alumnos)
    // Clasificamos a los alumnos en dos listas: aprobados y suspensos
    val (aprobados, suspensos) = clasificarAlumnos(alumnosNotaFinal)

    // Imprimimos la lista de alumnos aprobados
    println("Aprobados:")
    aprobados.forEach { println(it) } // Iteramos sobre la lista de aprobados e imprimimos cada alumno

    // Imprimimos la lista de alumnos suspensos
    println("\nSuspensos:")
    suspensos.forEach { println(it) } // Iteramos sobre la lista de suspensos e imprimimos cada alumno
}

/* 1. Función que recibe la ruta del fichero de calificaciones y devuelve una lista de diccionarios.
   Cada diccionario contiene la información de los exámenes y la asistencia de un alumno.
   La lista está ordenada por apellidos. */

fun leerCalificaciones(ruta: Path): List<Map<String, String>> {

    /* Creamos una lista mutable para almacenar los diccionarios de cada alumno. */
    val lista = mutableListOf<Map<String, String>>()

    // Abrimos el archivo CSV para lectura usando BufferedReader.
    BufferedReader(Files.newBufferedReader(ruta)).use { reader ->
        // Leemos la primera línea del archivo para obtener los nombres de las columnas.
        val cabecera = reader.readLine()?.split(";") ?: return emptyList()

        // Leemos cada línea del archivo, excluyendo la cabecera.
        reader.forEachLine { line ->
            // Dividimos la línea en valores usando el punto y coma
            val valores = line.split(";")
            // Verificamos que el número de valores coincide con el número de cabeceras.
            if (valores.size == cabecera.size) {

                /* Creamos un diccionario combinando cabecera y valores con el metodo "zip". El metodo zip
                es una función que combina dos listas en pares, emparejando los elementos correspondientes de
                ambas listas por sus posiciones.*/

                val diccionario = cabecera.zip(valores).toMap()
                // Añadimos el diccionario a la lista.
                lista.add(diccionario)
            }
        }
    }

    // Ordenamos la lista de diccionarios por apellidos.
    return lista.sortedBy { it["Apellidos"] ?: "" }
}

/* 2. Función que recibe una lista de diccionarios y añade una nota final al diccionario de cada alumno.
   La nota final se calcula considerando las mejores notas entre las originales y las de recuperación. */

fun calcularNotaFinal(lista: List<Map<String, String>>): List<Map<String, Any>> {

    // Mapeamos cada diccionario de la lista para añadir la nota final
    return lista.map { alumno ->

        /* Obtenemos las notas de los exámenes y de prácticas, convirtiendo a Double. Si no se puede convertir, usamos 0.0
        Utilizamos el metodo replace para reemplazar las comas por puntos. El metodo replace en Kotlin se utiliza para
        reemplazar partes de una cadena (texto). Permite buscar un carácter o una subcadena específica dentro de la cadena
        original y reemplazarla por otro carácter o subcadena.*/

        val nota1 = alumno["Parcial1"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val nota2 = alumno["Parcial2"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val practicas = alumno["Practicas"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        // Obtenemos las notas de recuperación, si existen, convirtiendo a Double. Si no se puede convertir, usamos 0.0
        val ordinario1 = alumno["Ordinario1"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val ordinario2 = alumno["Ordinario2"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val ordinarioPracticas = alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        // Usamos las mejores notas disponibles entre originales y de recuperación
        val mejorNota1 = maxOf(nota1, ordinario1)
        val mejorNota2 = maxOf(nota2, ordinario2)
        val mejorPracticas = maxOf(practicas, ordinarioPracticas)

        // Calculamos la nota final usando los parámetros especificados en el enunciado.
        val notaFinal = (mejorNota1 * 0.3 + mejorNota2 * 0.3 + mejorPracticas * 0.4)

        // Creamos un nuevo diccionario con la nota final añadida
        alumno + mapOf("NotaFinal" to notaFinal)
    }
}

/* 3. Función que recibe una lista de diccionarios con las notas finales y clasifica a los alumnos en aprobados y suspensos.
   Para aprobar el curso, la asistencia debe ser al menos del 75%, todas las notas deben ser al menos 4 y la nota final debe ser al menos 5. */

fun clasificarAlumnos(lista: List<Map<String, Any>>): Pair<List<Map<String, Any>>, List<Map<String, Any>>> {

    /* Lista mutable para almacenar los alumnos aprobados y los suspensos. Una lista mutable es aquella que puede cambiar
    una vez ya está creada.*/
    val aprobados = mutableListOf<Map<String, Any>>()
    val suspensos = mutableListOf<Map<String, Any>>()

    // Clasificamos a cada alumno basado en sus notas y asistencia
    lista.forEach { alumno ->

        // Obtener asistencia
        val asistencia = (alumno["Asistencia"] as? String)?.replace("%", "")?.toDoubleOrNull() ?: 0.0

        // Obtener las notas originales
        var nota1 = (alumno["Parcial1"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        var nota2 = (alumno["Parcial2"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        var practicas = (alumno["Practicas"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        // Obtener las notas de recuperación
        val ordinario1 = (alumno["Ordinario1"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val ordinario2 = (alumno["Ordinario2"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val ordinarioPracticas = (alumno["OrdinarioPracticas"] as? String)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        /* Si las notas de parcial o prácticas están suspensas, usamos las notas de recuperación. Utilizamos el metodo
        "maxOf" que lo que hace es elegir el valor mayor entre dos parámetros */

        if (nota1 < 4.0) nota1 = maxOf(nota1, ordinario1)
        if (nota2 < 4.0) nota2 = maxOf(nota2, ordinario2)
        if (practicas < 4.0) practicas = maxOf(practicas, ordinarioPracticas)

        // Obtenemos la nota final calculada previamente
        val notaFinal = (alumno["NotaFinal"] as? Double) ?: 0.0

        // Verificamos si cumple con los requisitos para aprobar
        if (asistencia >= 75.0 && nota1 >= 4.0 && nota2 >= 4.0 && practicas >= 4.0 && notaFinal >= 5.0) {
            aprobados.add(alumno)
        } else {
            suspensos.add(alumno)
        }
    }

    // Devolvemos las listas de aprobados y suspensos
    return Pair(aprobados, suspensos)
}