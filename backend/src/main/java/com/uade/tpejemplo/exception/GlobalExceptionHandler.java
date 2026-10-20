package com.uade.tpejemplo.exception;

import com.uade.tpejemplo.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

// Traduce las excepciones a respuestas HTTP con un ErrorResponse uniforme.
// MVC: el controlador no arma errores; el handler los traduce a la vista (JSON)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "No encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleNegocio(BusinessException ex) {
        return error(HttpStatus.BAD_REQUEST, "Error de negocio", List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return error(HttpStatus.BAD_REQUEST, "Error de validación", errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleSolicitudInvalida(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida", List.of("Solicitud inválida"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDesactivado(DisabledException ex) {
        return error(HttpStatus.UNAUTHORIZED, "No autenticado", List.of("El usuario está desactivado"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleNoAutenticado(AuthenticationException ex) {
        return error(HttpStatus.UNAUTHORIZED, "No autenticado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Acceso denegado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaNoEncontrada(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "No encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", List.of(ex.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleTipoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNoAceptable(HttpMediaTypeNotAcceptableException ex) {
        // Con un Accept no JSON no se puede negociar el cuerpo: se fuerza JSON.
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON)
            .body(error(HttpStatus.NOT_ACCEPTABLE, "Formato no aceptable", List.of(ex.getMessage())).getBody());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", List.of("Error interno"));
    }

    // Unico punto que arma el ErrorResponse; el codigo sale del HttpStatus.
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String titulo, List<String> mensajes) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), titulo, mensajes, LocalDateTime.now()));
    }
}
