package com.cliniccore.shared.api;

import com.cliniccore.shared.domain.ConflictException;
import com.cliniccore.shared.domain.ForbiddenException;
import com.cliniccore.shared.domain.NotFoundException;
import com.cliniccore.shared.domain.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	ProblemDetail notFound(NotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail conflict(ConflictException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(ForbiddenException.class)
	ProblemDetail forbidden(ForbiddenException exception) {
		return problem(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	ProblemDetail unauthorized(UnauthorizedException exception) {
		return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
			IllegalArgumentException.class })
	ProblemDetail badRequest(Exception exception) {
		return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(status.getReasonPhrase());
		problem.setDetail(detail);
		return problem;
	}
}
