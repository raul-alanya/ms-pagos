package ms_pagos.controller;

import ms_pagos.model.PagoRechazo;
import ms_pagos.model.PagoRequest;
import ms_pagos.model.PagoResponse;
import ms_pagos.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping("/procesar")
    public ResponseEntity<PagoResponse> procesarPago(@RequestBody PagoRequest pagoRequest) {
        PagoResponse resultado = pagoService.procesarPago(pagoRequest);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/rechazo")
    public ResponseEntity<PagoRechazo> registrarRechazo(@RequestBody PagoRechazo pagoRechazo) {
        PagoRechazo resultado = pagoService.registrarRechazo(pagoRechazo);
        return ResponseEntity.ok(resultado);
    }
}