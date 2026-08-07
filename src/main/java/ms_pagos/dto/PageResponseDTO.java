package ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PERF-IZI-001: Wrapper de paginación para todos los listados.
 * Devuelve los datos junto con metadatos de paginación:
 * página actual, tamaño, total de elementos y total de páginas.
 */
@Data
@Schema(description = "Respuesta paginada con metadatos")
public class PageResponseDTO<T> {

    @Schema(description = "Contenido de la página actual")
    private List<T> contenido;

    @Schema(description = "Número de página actual (0-indexed)", example = "0")
    private int paginaActual;

    @Schema(description = "Cantidad de elementos por página", example = "10")
    private int tamanioPagina;

    @Schema(description = "Total de elementos en todas las páginas", example = "42")
    private long totalElementos;

    @Schema(description = "Total de páginas disponibles", example = "5")
    private int totalPaginas;

    @Schema(description = "Indica si es la última página", example = "false")
    private boolean esUltimaPagina;

    /**
     * Construye un PageResponseDTO a partir de un Page de Spring Data.
     */
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        PageResponseDTO<T> dto = new PageResponseDTO<>();
        dto.setContenido(page.getContent());
        dto.setPaginaActual(page.getNumber());
        dto.setTamanioPagina(page.getSize());
        dto.setTotalElementos(page.getTotalElements());
        dto.setTotalPaginas(page.getTotalPages());
        dto.setEsUltimaPagina(page.isLast());
        return dto;
    }
}
