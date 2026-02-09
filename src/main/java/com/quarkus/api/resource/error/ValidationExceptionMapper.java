package com.quarkus.api.resource.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider // Anotação para registrar o ExceptionMapper no JAX-RS
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Coleta os detalhes de cada violação da constraint
        List<Map<String, String>> details = exception.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "field", extractFieldName(violation), // Extrai o nome do campo
                        "message", violation.getMessage()    // Mensagem de erro da validação
                ))
                .collect(Collectors.toList());

        // Cria a resposta de erro personalizada
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                Response.Status.BAD_REQUEST.getStatusCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                "Erros de validação na requisição.",
                details
        );

        // Retorna a resposta HTTP 400 Bad Request com o corpo JSON personalizado
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

    // Helper para extrair o nome do campo de forma mais limpa
    // O path pode vir como "createSO.request.description" ou "updateStatus.request.status"
    // Queremos apenas "description" ou "status"
    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1);
        }
        return path; // Retorna o path completo se não conseguir extrair o nome do campo
    }
}