package com.renu.mail.controllers;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.renu.mail.models.ConfirmationToken;
import com.renu.mail.models.User;
import com.renu.mail.repositories.ConfirmationTokenRepository;
import com.renu.mail.repositories.UserRepository;

@Controller
public class UserAccountController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountController.class);
	@Autowired
	private JavaMailSender sender;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ConfirmationTokenRepository confirmationTokenRepository;

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView displayRegistration(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user", user);
		modelAndView.setViewName("register");
		return modelAndView;
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView registerUser(ModelAndView modelAndView, User user) throws Exception {
		LOGGER.info("From class UserAccountController--method: registerUser()--Email_Id = " + user.getEmailId());
		User existingUser = userRepository.findByEmailIdIgnoreCase(user.getEmailId());
		if (existingUser != null) {
			LOGGER.info("From class UserAccountController--method: registerUser()--user already exist !!");

			modelAndView.addObject("message", "This email already exists!");
			modelAndView.setViewName("error");
		} else {
			userRepository.save(user);

			ConfirmationToken confirmationToken = new ConfirmationToken(user);
			LOGGER.info("From class UserAccountController--method: registerUser()--user not exist");
			LOGGER.info("From class UserAccountController--method: registerUser()--Token : "
					+ confirmationToken.getConfirmationToken());

			confirmationTokenRepository.save(confirmationToken);

			LOGGER.info("From class UserAccountController--method: registerUser()--Email_Id = " + user.getEmailId());
			LOGGER.info("From class UserAccountController--method: registerUser()--Token = "
					+ confirmationToken.getConfirmationToken());

			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);

			helper.setTo(user.getEmailId());
			helper.setSubject("Complete Registration");
			helper.setText("To confirm your account, please click here : "
					+ "http://localhost:8082/confirm-account?token=" + confirmationToken.getConfirmationToken());
			sendMailAsynchronously(message);

			modelAndView.addObject("emailId", user.getEmailId());

			modelAndView.setViewName("successfulRegisteration");
		}

		return modelAndView;
	}

	@Async
	private void sendMailAsynchronously(MimeMessage message) {
		sender.send(message);

	}

	@RequestMapping(value = "/confirm-account", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView confirmUserAccount(ModelAndView modelAndView, @RequestParam("token") String confirmationToken) {
		LOGGER.info("From class UserAccountController--method: confirmUserAccount()--Token = " + confirmationToken);

		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);

		if (token != null) {
			User user = userRepository.findByEmailIdIgnoreCase(token.getUser().getEmailId());
			user.setEnabled(true);
			userRepository.save(user);
			modelAndView.setViewName("accountVerified");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("error");
		}

		return modelAndView;
	}

}