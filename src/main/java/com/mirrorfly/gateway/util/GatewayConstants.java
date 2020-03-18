
package com.mirrorfly.gateway.util;

import java.util.Arrays;
import java.util.List;

/**
 * A container to store static constants Strings for proper code sequence. This
 * Constants are declared as final so it cannot be changed. These constants will
 * not be changed on the runtime.
 * 
 */

public class GatewayConstants {

	public static final List<String> ESC_PATH = Arrays.asList("swagger", "configuration", "webjars", "swagger-ui");

	public static final String AVAILABLE = "Data Retrieved Successfully";

	public static final String GIF_NOT_SUPPORTED = "Gif not supported";

	public static final String SUCCESS = "SUCCESS";

	public static final String NEW_USER_ADDED = "New User Added";

	public static final String USER = "User";

	public static final String USER_NAME_HEADER = "username";

	public static final String NOT_AVAILABLE = "No Records Found";

	public static final String USER_ADD_ERROR = "Error in Adding New User";

	public static final String BAD_CREDENTIALS = "Invalid Credentials";

	public static final String INVALID_APP_LOGIN = "User Not Having Access";

	public static final String USER_NEED_APPROVAL = "User Approval Still Pending";

	public static final String USER_REJECTED = "User Has Been Rejected";

	public static final String USER_DELETED = "User Deleted";

	public static final String USER_DISABLED = "User DeActivated";

	public static final String LOGIN_SUCCESS = "Logged In Successfully";

	public static final String OTP_SUCCESS = "Otp Sent Successfully";

	public static final String OTP_NOT_VALID = "Otp Not Valid";

	public static final String MOBILE_NOT_VALID = "Mobile Number Not Valid";

	public static final String MOBILE_NO_AVAIL = "Mobile Number Not Available";

	public static final String MOBILE_REQUIRED = "Mobile Number is Required";

	public static final String USER_BLOCK = "User Blocked,Please contact Admin";

	public static final String TOKEN_EXPIRED = "Token Expired";

	public static final String TOKEN_NOT_AVAILABLE = "Token Not Available";

	public static final String AUTHORIZATION = "Authorization";

	public static final String X_REQUEST_TYPE = "X-REQUEST-TYPE";

	public static final String MOBILE = "mobile";

	public static final String WEB = "web";

	public static final String CONTENT_TEXT_HTML = "text/html";

	public static final String CONTENT_TEXT_PLAIN = "text/plain";

	public static final String UNAUTHORIZED = "User UnAuthorized";

	public static final String INVALID_OTP = "Invalid OTP";

	public static final String FORGOT_PASS_SUCCESS = "Reset Password Email Sent Successfully";

	public static final String EMAIL_NOT_VALID = "Email Not Valid";

	public static final String USER_INACTIVE = "User Not Active , Contact Admin for More Details";

	public static final String PASS_CHANGE_SUCCESS = "Password Successfully Changed";

	public static final String PASS_NOT_MATCHED = "Enter Proper Current Password ";

	public static final String USER_ADD_SUCCESS = "User Registered Successfully";

	public static final String USER_UPDATE_SUCCESS = "User Successfully Updated";

	public static final String EMAIL_NOT_AVAIL = "Email Already Exist";

	public static final String OTP_EXPIRED = "Otp Expired";

	public static final String CONTACT_NOT_AVAIL = "Mobile No Already Exist";

	public static final String USER_NOT_AVAIL = "User Not Available";

	public static final String USER_NOT_FOUND = "User not found";

	public static final String FORGOT_PASS_LINK = "Reset password has been sent to your mail";

	public static final String PASS_CONFIRMATION = "Confirmation new password are not same";

	public static final String PASS_CONFIRMATION_SUCCESS = "Password changed successfully! Login to continue...";
	
	public static final String ACCOUNT_ACTIVATION_SUCCESS = "Account Activated successfully! Login to continue...";


	public static final String ADD = "Add";

	public static final String WELCOME_MAIL_TEMPLATE = "login-success";

	public static final String IMAGE_UPLOAD_SUCCESS = "Image Uploaded Successfully";

	public static final String WEB_LOGIN_TYPE = "WEB";

	public static final String MOBILE_LOGIN_TYPE = "MOBILE";

	public static final String EMAIL_EXIST = "Email Id already exist";

	public static final String MOBILE_NUM_EXIST = "Mobile Number already exist";

	public static final String DATA_RETRIEVED = "Data retrieved successfully";

	public static final String LOG_OUT = "Logout successfully";

	public static final String OTP_SENT = "OTP Sent successfully";

	public static final String FCM_SENT = "Mobile No successfully Registered";

	public static final String BAD_CREDENTIALS_TWO = "Entered password credentials are not match, Account will be locked after 3 rd attempt";

	public static final String BAD_CREDENTIALS_THREE = "Entered credentials are not match, Account has been locked";

	public static final String USER_NAME_NOT_AVAIL = "Username or EmailId Not Available";

	public static final String LICENSE_NOT_AVAIL = "License Not Available";

	public static final String LICENSE_IN_ACTIVE = "License is InActive";

	public static final String LICENSE_EXPIRED = "License has been Expired. Please contact the provider";

	public static final String USER_IN_ACTIVE = "User is InActive please contact admin";

	public static final String ACCOUNT_LOCKED = "Account has been locked, please contact adminstrator";

	public static final String LOGO_UPLOAD_SUCCESS = "Logo uploaded successfully";

	public static final String IMAGE = "image";

	public static final String IMAGE_GIF = "image/gif";

	public static final String UPLOAD_ERROR = "Upload Error: ";

	public static final String OPTIONS = "OPTIONS";

	public static final String LOGIN = "login";

	public static final String OK = "OK";

	public static final String APP_JSON = "application/json";

	public static final String TOKEN_EXPIRED_JSON = "{ \"status\" : 401, \"message\" : \"Token Expired\", \"error\" : true}";

	public static final String HOST_ERROR = "{ \"status\" : 401, \"message\" : \"Ip not Whitelisted\", \"error\" : true}";

	public static final String FILE_NOT_AVILABLE = "File Not Available";

	public static final String IV = "ddc0f15cc2c90fca";

	public static final String EMAILID_NOT_VALID = "Enter the Valid Email Address";

	public static final String PASS_NOT_VALID = "Enter the Valid Password";

	public static final String EMAIL_SENT_SUCCESSFULLY = "An email has been sent to your email address, please check your inbox";

	public static final String EMAIL_SENT_FAILED = "Email Sent Failed";

	public static final String PASWORD_PATTERN_MISMATCH = "Password must be atleast 8 characters with the combination of atleast one capital letter and a symbol";

	public static final String INVALID_CREDENTAILS = "Invalid credentials. Enter a valid email or username and password";

	public static final String WRONG_PASWORD = "Wrong password! Try again or click Forgot password to reset it";

	public static final String EMAILID_NOT_REGISTERED = "Couldn't find your account!";

	public static final String STATUS_PENDING = "PENDING";

	public static final String STATUS_EXPIRED = "EXPIRED";

	public static final String FORGOT_PASWORD = "Forgot Password";
	
	public static final String INACTIVE_ACCOUNT= "Your Account has been Disabled by Admin";

	
	private GatewayConstants() {
	}
}
