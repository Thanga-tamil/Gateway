package com.mirrorfly.gateway.exception;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.mirrorfly.gateway.response.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * This Container class used to catch the Exceptions arise from the code. And to
 * Handle those Exceptions with proper error message and error code.
 */
@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(JDBCConnectionException.class)
	public Response<String> jdbcConnectionException(final JDBCConnectionException e) {
		printErrorStack(e.getStackTrace());
		return setStatusAndMessage(HttpStatus.NOT_FOUND.value(), e.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Response<String> httpMessageNotReadableException(final HttpMessageNotReadableException e) {
		printErrorStack(e.getStackTrace());
		return setStatusAndMessage(HttpStatus.NOT_FOUND.value(), e.getMessage());
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public Response<String> httpMediaTypeNotAcceptableException(final HttpMediaTypeNotAcceptableException e) {
		printErrorStack(e.getStackTrace());
		return setStatusAndMessage(HttpStatus.NOT_FOUND.value(), e.getMessage());
	}

	@ExceptionHandler(MismatchedInputException.class)
	public Response<String> mismatchedInputException(final MismatchedInputException e) {
		printErrorStack(e.getStackTrace());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	public Response<String> notFoundException(final NotFoundException e) {
		printErrorStack(e.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
	}

	/**
	 * Function to catch Exception by checking the argument is valid or not
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Response<String> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
		String errorMsg = ex.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage).findFirst().orElse(ex.getMessage());
		LOGGER.error(ex.getLocalizedMessage());
		LOGGER.error(errorMsg);
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), errorMsg);
	}

	/**
	 * This method used to catch NoDataFound Exception.
	 * 
	 * @param ex       exception
	 * @param response Http response
	 * @return response status and message
	 */
	@ExceptionHandler(value = BadDataException.class)
	public Response<String> noDataFoundException(BadDataException ex) {
		LOGGER.error(ex.getLocalizedMessage());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}

	/**
	 * This method used to catch NoDataFound Exception.
	 * 
	 * @param ex       exception
	 * @param response Http response
	 * @return response status and message
	 */
	@ExceptionHandler(value = NoDataFoundException.class)
	public Response<String> noDataFoundException(NoDataFoundException ex) {
		LOGGER.error(ex.getLocalizedMessage());
		return setStatusAndMessage(HttpStatus.NO_CONTENT.value(), ex.getMessage());
	}

	/**
	 * This method used to catch CommonException Exception.
	 * 
	 * @param ex       exception
	 * @param response Http response
	 * @return response status and message
	 */
	@ExceptionHandler(value = CommonException.class)
	public Response<String> commonException(CommonException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}

	/**
	 * Method to catch File Not Found exception on runtime execution
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = FileNotFoundException.class)
	public Response<String> fileNotFoundException(FileNotFoundException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.NOT_FOUND.value(), ex.getMessage());
	}

	/**
	 * Method to catch Array index bound exception on runtime
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = ArrayIndexOutOfBoundsException.class)
	public Response<String> arrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Class Cast Exception while converting DataTypes from one to
	 * other.
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = ClassCastException.class)
	public Response<String> classCastException(ClassCastException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Illegal Argument Exception it occurs when an illegal
	 * arguments passed.
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = IllegalArgumentException.class)
	public Response<String> illegalArgumentException(IllegalArgumentException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}

	/**
	 * Method to catch Null pointer exception when null value passed to the
	 * response.
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = NullPointerException.class)
	public Response<String> nullPointerException(NullPointerException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch number Format exception it occurs, when an input passed as a
	 * string to DataType integer format this will result this NumberFormatException
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = NumberFormatException.class)
	public Response<String> numberFormatException(NumberFormatException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}

	/**
	 * Method to catch Assertion Error it occurs, when an invalid assignment takes
	 * place.
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = AssertionError.class)
	public Response<String> assertionError(AssertionError ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Exception In Initializer Error it occurs, on Class
	 * initialization with constructors, variables and Functions
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = ExceptionInInitializerError.class)
	public Response<String> exceptionInInitializerError(ExceptionInInitializerError ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Stack Overflow Error on runtime
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = StackOverflowError.class)
	public Response<String> stackOverflowError(StackOverflowError ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch No Class Def Found Error on runtime
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = NoClassDefFoundError.class)
	public Response<String> noClassDefFoundError(NoClassDefFoundError ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Runtime Exception.
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = RuntimeException.class)
	public Response<String> resourceNotFoundException(RuntimeException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * Method to catch Constraint Violation Exception on runtime
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */
	@ExceptionHandler(value = ConstraintViolationException.class)
	public Response<String> resourceNotFoundException(ConstraintViolationException ex) {
		Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
		Set<String> messages = new HashSet<>(constraintViolations.size());
		messages.addAll(constraintViolations.stream()
				.map(constraintViolation -> String.format("%s value '%s' %s", constraintViolation.getPropertyPath(),
						constraintViolation.getInvalidValue(), constraintViolation.getMessage()))
				.collect(Collectors.toList()));
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), "sdsd");
	}

	@ExceptionHandler(value = WebExchangeBindException.class)
	public Response<String> webExchangeBindException(WebExchangeBindException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getFieldError().getDefaultMessage());
	}

	/**
	 * Method to catch Exception on runtime This Exception is parent of all above
	 * Exception classes
	 * 
	 * @param ex       exception message
	 * @param response HttpServletResponse
	 * @return Exception Status and Message
	 */

	@ExceptionHandler(value = Exception.class)
	public Response<String> exception(Exception ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * This method used to catch Validation Exception.
	 * 
	 * @param ex       exception
	 * @param response Http response
	 * @return response status and message
	 */
	@ExceptionHandler(value = ValidationException.class)
	public Response<String> validationException(ValidationException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
	}

	/**
	 * This method used to catch NoDataFound Exception.
	 * 
	 * @param ex       exception
	 * @param response Http response
	 * @return response status and message
	 */
	@ExceptionHandler(value = UserUnAuthorizedException.class)
	public Response<String> userUnAuthorizedException(UserUnAuthorizedException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
	}

	/**
	 * Method to set Status and Message in a response JSON format
	 * 
	 * @param status  Exception code
	 * @param message Exception description
	 * @return JSON format response
	 */
	@ExceptionHandler(value = ForbiddenException.class)
	public Response<String> forbiddenException(ForbiddenException ex) {
		printErrorStack(ex.getStackTrace());
		return setStatusAndMessage(HttpStatus.FORBIDDEN.value(), ex.getMessage());
	}
	
	@ExceptionHandler(value = DataSourceNotFoundException.class)
	public Response<String> noDataSourceFoundException(DataSourceNotFoundException ex) {
		LOGGER.error(ex.getLocalizedMessage());
		return setStatusAndMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}
	
	private void printErrorStack(StackTraceElement[] ste) {
		StringBuilder error = new StringBuilder();
		try {
			if (ste != null && ste.length > 0) {
				for (int i = 0; i < ste.length; i++) {
					error.append(ste[i].toString()).append(" ").append(System.lineSeparator()).append(" ");
				}
				log.error("Exception details ::: " + error.toString());
			}
		} catch (Exception e) {
			log.error("Could not print custom error stack");
		}
	}
	
	private Response<String> setStatusAndMessage(int status, String message) {
		Response<String> reponseMap = new Response<>();
		reponseMap.setStatus(status);
		reponseMap.setData("");
		reponseMap.setMessage(message);
		return reponseMap;
	}

}
