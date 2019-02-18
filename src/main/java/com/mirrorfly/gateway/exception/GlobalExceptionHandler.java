package com.mirrorfly.gateway.exception;

import java.io.FileNotFoundException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.response.Response;

/**
 * This Container class used to catch the Exceptions arise from the code. And to
 * Handle those Exceptions with proper error message and error code.
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

	/*
	 * @ExceptionHandler(NoDataFoundException.class) public Response<String>
	 * notFoundException(final NoDataFoundException e) { return
	 * setStatusAndMessage(HttpStatus.NOT_FOUND.value(), e.getMessage()); }
	 */
	/*
	 * private Response<String> error(final Exception exception, final HttpStatus
	 * httpStatus, final String logRef) { final String message =
	 * Optional.of(exception.getMessage()).orElse(exception.getClass().getSimpleName
	 * ()); return new ResponseEntity<>(new VndErrors(logRef, message), httpStatus);
	 * }
	 */

	/*
	 * @ExceptionHandler(IllegalArgumentException.class) public
	 * ResponseEntity<VndErrors> assertionException(final IllegalArgumentException
	 * e) { return error(e, HttpStatus.NOT_FOUND, e.getLocalizedMessage()); }
	 */

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
		return setStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
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
		LOGGER.error(ex.getLocalizedMessage());
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
	public Response<String> validationException(ConstraintViolationException ex) {
		LOGGER.error(ex.getLocalizedMessage());
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
	public Response<String> userUnAuthorizedException(NoDataFoundException ex) {
		LOGGER.error(ex.getLocalizedMessage());
		return setStatusAndMessage(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
	}

	/**
	 * Method to set Status and Message in a response JSON format
	 * 
	 * @param status  Exception code
	 * @param message Exception description
	 * @return JSON format response
	 */

	private Response<String> setStatusAndMessage(int status, String message) {
		Response<String> reponseMap = new Response<>();
		reponseMap.setStatus(status);
		reponseMap.setData("");
		reponseMap.setMessage(message);
		return reponseMap;
	}

}
