package com.empresax.gestionflota.documentacion.controller;

import com.empresax.gestionflota.documentacion.dto.DocumentoDigitalDTO;
import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import com.empresax.gestionflota.documentacion.service.DocumentoDigitalService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoDigitalController {

    private final DocumentoDigitalService service;

    public DocumentoDigitalController(DocumentoDigitalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DocumentoDigital>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long entidadId) {
        return ResponseEntity.ok(service.listar(categoria, entidadId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registrar(@RequestBody DocumentoDigitalDTO dto) {
        try {
            return new ResponseEntity<>(service.registrar(dto), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarConArchivo(
            @RequestParam("categoria") String categoria,
            @RequestParam("entidadId") Long entidadId,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "fechaEmision", required = false) LocalDate fechaEmision,
            @RequestParam(value = "fechaVencimiento", required = false) LocalDate fechaVencimiento,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) throws IOException {
        try {
            DocumentoDigital creado = service.registrarConArchivo(
                    categoria, entidadId, tipo, fechaEmision, fechaVencimiento, archivo
            );
            return new ResponseEntity<>(creado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<?> obtenerArchivo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean download) {
        DocumentoDigital doc;
        try {
            doc = service.buscarPorId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }

        if (doc.getContenido() == null || doc.getContenido().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Este documento no tiene un archivo adjunto guardado."));
        }

        MediaType mediaType = (doc.getTipoContenido() != null && !doc.getTipoContenido().isBlank())
                ? MediaType.parseMediaType(doc.getTipoContenido())
                : MediaType.APPLICATION_OCTET_STREAM;

        String nombre = doc.getArchivoNombre() != null ? doc.getArchivoNombre() : "documento_" + id;
        ContentDisposition disposition = ContentDisposition
                .builder(download ? "attachment" : "inline")
                .filename(nombre, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(doc.getContenido());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}