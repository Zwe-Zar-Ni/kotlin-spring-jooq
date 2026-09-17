package com.vaddshah.ktjooq.common.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Invalid value")
        }

        val response = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            message = "Validation failed.",
            errors = errors
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Not found."
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Resource conflict occurred"
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Requested resource not found"
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = "Access denied"
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleAccessDenied(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.METHOD_NOT_ALLOWED.value(),
            message = "Method Not Allowed for this endpoint"
        )

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val rootMessage = ex.rootCause?.message ?: ex.message ?: "Request body is invalid"
        val friendlyMessage = when {
            rootMessage.contains("which is a non-nullable type") -> {
                val fieldName = Regex("JSON property (\\w+)").find(rootMessage)?.groupValues?.get(1)
                if (fieldName != null) {
                    "Missing required field: $fieldName"
                } else {
                    "Request body is missing required fields"
                }
            }

            rootMessage.contains("Unexpected end-of-input") -> "Request body is empty"
            else -> "Request body is invalid: $rootMessage"
        }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = friendlyMessage
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            message = "Invalid email or password"
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(ex: UsernameNotFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            message = "Invalid email or password"
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

}