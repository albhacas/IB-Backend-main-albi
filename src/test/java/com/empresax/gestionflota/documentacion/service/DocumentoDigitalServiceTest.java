package com.empresax.gestionflota.documentacion.service;

import com.empresax.gestionflota.documentacion.repository.DocumentoDigitalRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DocumentoDigitalServiceTest {

    private final DocumentoDigitalService service = new DocumentoDigitalService(null);

    @Test
    void noCompletaVencimientoAutomaticoParaPoliza() {
        LocalDate emision = LocalDate.of(2026, 1, 15);
        LocalDate vencimientoIngresado = LocalDate.of(2026, 12, 31);

        assertNull(service.resolverVencimientoSegunRegla("Póliza", emision, null));
        assertEquals(vencimientoIngresado,
                service.resolverVencimientoSegunRegla("Póliza", emision, vencimientoIngresado));
    }

    @Test
    void mantieneVencimientoAutomaticoParaCertificadoSeguro() {
        LocalDate emision = LocalDate.of(2026, 1, 15);

        assertEquals(LocalDate.of(2026, 4, 30),
                service.resolverVencimientoSegunRegla("Certificado seguro", emision, null));
    }

    @Test
    void noEliminaDocumentoInexistente() {
        DocumentoDigitalRepository repository = mock(DocumentoDigitalRepository.class);
        when(repository.existsById(42L)).thenReturn(false);
        DocumentoDigitalService serviceConRepositorio = new DocumentoDigitalService(repository);

        assertThrows(IllegalArgumentException.class, () -> serviceConRepositorio.eliminar(42L));
        verify(repository, never()).deleteById(42L);
    }

    @Test
    void eliminaDocumentoExistente() {
        DocumentoDigitalRepository repository = mock(DocumentoDigitalRepository.class);
        when(repository.existsById(42L)).thenReturn(true);
        DocumentoDigitalService serviceConRepositorio = new DocumentoDigitalService(repository);

        assertDoesNotThrow(() -> serviceConRepositorio.eliminar(42L));
        verify(repository).deleteById(42L);
    }
}
