package com.quarkus.api.resource.error;

import jakarta.ws.rs.ProcessingException; // Importe esta exceção
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Collections; // Para Collections.singletonList

@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException exception) {
        // Tenta extrair uma mensagem mais amigável
        String errorMessage = "Erro no processamento da requisição.";
        if (exception.getCause() != null && exception.getCause().getMessage() != null) {
            errorMessage = exception.getCause().getMessage();
            // Para o caso específico de enum, podemos tentar formatar melhor
            if (errorMessage.contains("No enum constant")) {
                errorMessage = "Valor inválido para campo de enum: " + extractEnumFieldName(errorMessage);
            } else if (errorMessage.contains("Unrecognized field")) {
                errorMessage = "Campo não reconhecido no JSON: " + extractUnrecognizedFieldName(errorMessage);
            }
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                Response.Status.BAD_REQUEST.getStatusCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                errorMessage,
                Collections.singletonList(Collections.singletonMap("detail", errorMessage))
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

    // Helper para extrair o nome do campo do enum da mensagem de erro
    private String extractEnumFieldName(String message) {
        // Ex: "No enum constant com.quarkus.api.domain.enums.Status.STATUS_INEXISTENTE"
        int lastDotIndex = message.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < message.length() - 1) {
            String enumValue = message.substring(lastDotIndex + 1);
            // Poderíamos tentar inferir o nome do campo DTO aqui se a mensagem fosse mais rica
            return enumValue; // Retorna o valor inválido do enum
        }
        return "valor desconhecido";
    }

    // Helper para extrair o nome do campo não reconhecido
    private String extractUnrecognizedFieldName(String message) {
        // Ex: "Unrecognized field "invalidField" (class com.quarkus.api.resource.dto.ServiceOrderCreateRequestDTO), not marked as ignorable"
        int start = message.indexOf('"');
        int end = message.indexOf('"', start + 1);
        if (start != -1 && end != -1) {
            return message.substring(start + 1, end);
        }
        return "campo desconhecido";
    }
}