package com.mirrorfly.gateway.service;

import java.util.Arrays;
import java.util.List;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

	public String generateRandomPassword() {
		List<CharacterRule> rules = Arrays.asList(new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), new CharacterRule(EnglishCharacterData.Digit, 1));
		PasswordGenerator generator = new PasswordGenerator();
 		return generator.generatePassword(15, rules);
	}

	public String generateOtp() {
		List<CharacterRule> rules = Arrays.asList(new CharacterRule(EnglishCharacterData.Digit, 6));
		PasswordGenerator generator = new PasswordGenerator();
 		return generator.generatePassword(6, rules);
	}

	public boolean isPasswordValid(String password) {
		boolean isPasswordValid = false;
		PasswordValidator validator = new PasswordValidator(
				Arrays.asList(new LengthRule(8, 16), new CharacterRule(EnglishCharacterData.UpperCase, 1),
						new CharacterRule(EnglishCharacterData.LowerCase, 1),
						new CharacterRule(EnglishCharacterData.Digit, 1)));
		RuleResult result = validator.validate(new PasswordData(password));
		if (result.isValid()) {
			isPasswordValid = true;
		} else {
			isPasswordValid = false;
			final StringBuilder message = new StringBuilder();
			validator.getMessages(result).stream().forEach(message::append);
		}
		return isPasswordValid;
	}

}
