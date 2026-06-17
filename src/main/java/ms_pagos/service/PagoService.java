package ms_pagos.service;

import ms_pagos.config.IzipayClient;
import ms_pagos.model.PagoRechazo;
import ms_pagos.model.PagoRequest;
import ms_pagos.model.PagoResponse;
import ms_pagos.repository.PagoRechazoRepository;
import ms_pagos.repository.PagoRequestRepository;
import ms_pagos.repository.PagoResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PagoService {

    @Autowired
    private PagoRequestRepository pagoRequestRepository;

    @Autowired
    private PagoResponseRepository pagoResponseRepository;

    @Autowired
    private PagoRechazoRepository pagoRechazoRepository;

    @Autowired
    private IzipayClient izipayClient;

    @Value("${izipay.merchant-code}")
    private String merchantCode;

    public PagoResponse procesarPago(PagoRequest pagoRequest) {
        PagoRequest pagoGuardado = pagoRequestRepository.save(pagoRequest);

        Map<String, Object> requestIzipay = new HashMap<>();
        requestIzipay.put("merchantCode", merchantCode);
        requestIzipay.put("orderNumber", pagoGuardado.getReference());
        requestIzipay.put("amount", pagoGuardado.getTotalamount());
        requestIzipay.put("currency", pagoGuardado.getCurrency());
        requestIzipay.put("cardNumber", pagoGuardado.getCardnumber());
        requestIzipay.put("cardHolderName", pagoGuardado.getCardholdername());
        requestIzipay.put("cvv", pagoGuardado.getCvv());
        requestIzipay.put("cardExpiry", pagoGuardado.getCardexpiry());
        requestIzipay.put("email", pagoGuardado.getEmail());
        requestIzipay.put("clientIp", pagoGuardado.getClientip());
        requestIzipay.put("clientCountry", pagoGuardado.getClientcountry());

        Map<String, Object> respuestaIzipay = izipayClient.procesarPago(requestIzipay);

        String estado = String.valueOf(respuestaIzipay.get("status"));

        if (estado.equals("RECHAZADO") || estado.equals("DECLINED")) {
            PagoRechazo rechazo = new PagoRechazo();
            rechazo.setPagoRequest(pagoGuardado);
            rechazo.setCodigoRechazo(String.valueOf(respuestaIzipay.get("code")));
            rechazo.setMotivoRechazo(String.valueOf(respuestaIzipay.get("message")));
            rechazo.setFechaRechazo(String.valueOf(respuestaIzipay.get("dateTime")));
            rechazo.setEstado("RECHAZADO");
            pagoRechazoRepository.save(rechazo);
        }

        PagoResponse pagoResponse = new PagoResponse();
        pagoResponse.setPagoRequest(pagoGuardado);
        pagoResponse.setCodigoRespuesta(String.valueOf(respuestaIzipay.get("code")));
        pagoResponse.setEstado(estado);
        pagoResponse.setMensaje(String.valueOf(respuestaIzipay.get("message")));
        pagoResponse.setFechaRespuesta(String.valueOf(respuestaIzipay.get("dateTime")));

        return pagoResponseRepository.save(pagoResponse);
    }

    public PagoRechazo registrarRechazo(PagoRechazo pagoRechazo) {
        return pagoRechazoRepository.save(pagoRechazo);
    }
}