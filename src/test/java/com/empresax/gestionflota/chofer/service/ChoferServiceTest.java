package com.empresax.gestionflota.chofer.service;

import com.empresax.gestionflota.chofer.dto.ChoferAltaDTO;
import com.empresax.gestionflota.chofer.model.Chofer;
import com.empresax.gestionflota.chofer.repository.ChoferRepository;
import com.empresax.gestionflota.documentacion.model.DocumentoDigital;
import com.empresax.gestionflota.documentacion.repository.DocumentoDigitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChoferServiceTest {

    @Mock
    private ChoferRepository choferRepository;

    @Mock
    private DocumentoDigitalRepository documentoDigitalRepository;

    @InjectMocks
    private ChoferService service;

    @Test
    void registraCarnetsCertificadoYArchivosEnDocumentacionDigital() {
        ChoferAltaDTO dto = ChoferAltaDTO.builder()
                .nombre("Ana")
                .apellido("Pérez")
                .dni("28450912")
                .cuil("20-28450912-4")
                .telefono("+54 9 11 1234-5678")
                .ambitoHabilitacion("Ambos")
                .nroCarnetProvincial("28450912")
                .categoriaProvincial("D2")
                .vencimientoProvincial(LocalDate.of(2027, 5, 14))
                .nroCarnetNacional("28450912")
                .categoriaNacional("D3")
                .vencimientoNacional(LocalDate.of(2027, 6, 14))
                .fechaEmisionDelitos(LocalDate.of(2026, 1, 15))
                .fechaVencimientoDelitos(LocalDate.of(2026, 7, 15))
                .estadoDelitos("En Trámite")
                .build();
        byte[] carnetBytes = {1, 2, 3};
        MockMultipartFile carnetProvincial = new MockMultipartFile(
                "carnetProvincial", "provincial.pdf", "application/pdf", carnetBytes);

        when(choferRepository.findByDni("28450912")).thenReturn(Optional.empty());
        when(choferRepository.save(any(Chofer.class))).thenAnswer(invocation -> {
            Chofer chofer = invocation.getArgument(0);
            chofer.setId(15L);
            return chofer;
        });
        when(documentoDigitalRepository
                .findFirstByCategoriaAndEntidadIdAndTipoIgnoreCaseOrderByIdDesc(
                        any(), any(), any()))
                .thenReturn(Optional.empty());

        Chofer guardado = service.registrarDesdeDTO(
                dto, carnetProvincial, null, null);

        assertEquals(2, guardado.getLicencias().size());
        assertEquals("D2", guardado.getLicencias().get(0).getCategoria());
        assertEquals(LocalDate.of(2027, 5, 14),
                guardado.getLicencias().get(0).getFechaVencimiento());

        ArgumentCaptor<DocumentoDigital> documentos = ArgumentCaptor.forClass(DocumentoDigital.class);
        verify(documentoDigitalRepository, org.mockito.Mockito.times(3)).save(documentos.capture());

        DocumentoDigital docProvincial = documentos.getAllValues().stream()
                .filter(doc -> "Licencia de Conducir Provincial".equals(doc.getTipo()))
                .findFirst()
                .orElseThrow();
        assertEquals(15L, docProvincial.getEntidadId());
        assertEquals(LocalDate.of(2027, 5, 14), docProvincial.getFechaVencimiento());
        assertEquals("provincial.pdf", docProvincial.getArchivoNombre());
        assertArrayEquals(carnetBytes, docProvincial.getContenido());

        DocumentoDigital certificado = documentos.getAllValues().stream()
                .filter(doc -> "Certificado de Delitos Sexuales".equals(doc.getTipo()))
                .findFirst()
                .orElseThrow();
        assertEquals("En Trámite", certificado.getEstado());
        assertEquals(LocalDate.of(2026, 7, 15), certificado.getFechaVencimiento());
    }
}
