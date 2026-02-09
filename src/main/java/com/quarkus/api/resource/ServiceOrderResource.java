package com.quarkus.api.resource;

import com.quarkus.api.domain.model.ServiceOrder;
import com.quarkus.api.resource.dto.ServiceOrderCreateRequestDTO;
import com.quarkus.api.resource.dto.ServiceOrderResponseDTO;
import com.quarkus.api.resource.dto.ServiceOrderStatusUpdateRequestDTO;
import com.quarkus.api.resource.error.ErrorResponseDTO;
import com.quarkus.api.resource.mapper.ServiceOrderMapper;
import com.quarkus.api.service.ServiceOrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/ordens-servico")
@Produces("application/json")
@Consumes("application/json")
@Tag(name = "Ordens de Serviço", description = "Operações relacionadas a Ordens de Serviço.")
public class ServiceOrderResource {

    @Inject
    ServiceOrderService service;

    @Inject
    ServiceOrderMapper mapper;

    @POST
    @Operation(summary = "Cria uma nova Ordem de Serviço", description = "Registra uma nova Ordem de Serviço com cliente e descrição.")
    @APIResponse(responseCode = "201", description = "Ordem de Serviço criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceOrderResponseDTO.class)))
    @APIResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    public Response createSO(@RequestBody(description = "Dados para criação da Ordem de Serviço", required = true, content = @Content(schema = @Schema(implementation = ServiceOrderCreateRequestDTO.class))) @Valid ServiceOrderCreateRequestDTO request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Corpo da requisição inválido ou vazio.").build();
        }

        ServiceOrder serviceOrder = service.createSO(mapper.toEntity(request));
        return Response.status(Response.Status.CREATED).entity(mapper.toDto(serviceOrder)).build();
    }

    @GET
    public List<ServiceOrderResponseDTO> listSO(@QueryParam("page") int page, @QueryParam("size") int size) {
        return mapper.toDtoList(service.listSO(page, size));
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") Long id) {
        ServiceOrder serviceOrder = service.findById(id);
        if (serviceOrder == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(serviceOrder).build();
    }

    @PUT
    @Path("{id}/status")
    public Response updateStatus(@PathParam("id") Long id, @Valid ServiceOrderStatusUpdateRequestDTO request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Corpo da requisição inválido ou vazio.").build();
        }
        try {
            service.updateStatus(id, request.getStatus());
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
